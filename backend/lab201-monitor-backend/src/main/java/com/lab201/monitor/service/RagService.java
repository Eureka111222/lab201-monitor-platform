package com.lab201.monitor.service;

import com.lab201.monitor.common.event.RagParseEvent;
import com.lab201.monitor.common.utils.MinioUtil;
import com.lab201.monitor.entity.SysDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RagService {

    @Autowired
    private ISysDocumentService documentService;

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private VectorStore vectorStore;

    //@Autowired
    //@Qualifier("deepseekChatClient")
    //private ChatClient deepseekChatClient;

    @Autowired
    @Qualifier("ollamaChatClient")
    private ChatClient ollamaChatClient;

    /**
     * 1. 异步监听文档解析事件 (入库流程)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRagParse(RagParseEvent event) {
        Long docId = event.getDocumentId();
        log.info(">>> [RAG] 开始解析文档 ID: {}", docId);

        SysDocument doc = documentService.getById(docId);
        if (doc == null) return;

        try {
            // 1.1 更新状态：解析中
            doc.setParseStatus(1);
            documentService.updateById(doc);

            // 1.2 从 MinIO 获取文件流
            InputStream inputStream = minioUtil.getObject(doc.getBucketName(), doc.getObjectName());

            // 1.3 【Extract】读取并解析文档
            TikaDocumentReader reader = new TikaDocumentReader(new InputStreamResource(inputStream));
            List<Document> documents = reader.get();

            // 1.4 【Transform】文本切片
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> splitDocuments = splitter.apply(documents);

            // 1.5 【Metadata】补充元数据
            for (Document splitDoc : splitDocuments) {
                splitDoc.getMetadata().put("doc_id", docId);
                splitDoc.getMetadata().put("title", doc.getTitle());
                splitDoc.getMetadata().put("view_scope", doc.getViewScope());
            }

            // 1.6 【Load】一键存入
            vectorStore.add(splitDocuments);

            // 1.7 更新状态：成功
            doc.setParseStatus(2);
            // 🔥🔥🔥 修正点 1：使用 getText() 获取内容 🔥🔥🔥
            if (!splitDocuments.isEmpty()) {
                String content = splitDocuments.get(0).getText(); // 改为 getText()
                // 增加空指针保护，防止内容为空
                if (content != null) {
                    doc.setSummary(content.substring(0, Math.min(content.length(), 200)));
                } else {
                    doc.setSummary("无文本内容");
                }
            }
            documentService.updateById(doc);

            log.info(">>> [RAG] 文档解析完成 ID: {}, 切片数: {}", docId, splitDocuments.size());

        } catch (Exception e) {
            log.error(">>> [RAG] 文档解析失败 ID: {}", docId, e);
            doc.setParseStatus(3);
            documentService.updateById(doc);
        }
    }

    /**
     * 2. 对话流程 (检索 + 生成)
     */
    public String chat(String query) {
        // 2.1 【Retrieve】检索
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(5).build()
        );

        // 2.2 【Context】组装上下文
        // 🔥🔥🔥 修正点 2：使用 Document::getText 🔥🔥🔥
        String context = similarDocuments.stream()
                .map(Document::getText) // 改为 getText
                .collect(Collectors.joining("\n\n"));

        // 2.3 【Generate】生成回答
        //String prompt = """
        //        基于以下已知信息回答用户的问题。如果无法从信息中得到答案，请说“不知道”。
        //
        //        --- 已知信息 ---
        //        {context}
        //        --- 信息结束 ---
        //
        //        问题：{query}
        //        """;
        String prompt = """
            请回答用户的问题。
            
            参考信息如下（如果有）：
            --- 上下文开始 ---
            {context}
            --- 上下文结束 ---
            
            回答要求：
            1. 如果参考信息中有答案，请直接基于参考信息回答。
            2. 如果参考信息中没有答案，请使用你自己的通用知识来回答。
            3. 请保持语气亲切、专业。
            
            用户问题：{query}
            """;

        return ollamaChatClient.prompt()
                .user(u -> u.text(prompt)
                        .param("context", context)
                        .param("query", query))
                .call()
                .content();
    }

    public Flux<String> chatStream(String query) {
        // 1. 【检索】(跟之前一样，先查资料)
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(5).build()
        );

        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // 2. 【生成】(使用 stream() 而不是 call())
        String prompt = """
            请回答用户的问题。
            
            参考信息如下（如果有）：
            --- 上下文开始 ---
            {context}
            --- 上下文结束 ---
            
            回答要求：
            1. 如果参考信息中有答案，请直接基于参考信息回答。
            2. 如果参考信息中没有答案，请使用你自己的通用知识来回答。
            3. 请保持语气亲切、专业。
            
            用户问题：{query}
            """;

        // 🔥 核心区别：使用 .stream().content() 返回 Flux<String>
        return ollamaChatClient.prompt()
                .user(u -> u.text(prompt)
                        .param("context", context)
                        .param("query", query))
                .stream()
                .content();
    }
}