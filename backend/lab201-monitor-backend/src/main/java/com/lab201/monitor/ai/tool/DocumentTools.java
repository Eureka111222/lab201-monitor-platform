package com.lab201.monitor.ai.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab201.monitor.ai.agent.AgentTraceContext;
import com.lab201.monitor.entity.SysDocument;
import com.lab201.monitor.service.AgentTraceService;
import com.lab201.monitor.service.ISysDocumentService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DocumentTools {

    private final ISysDocumentService documentService;

    private final AgentTraceService agentTraceService;

    public DocumentTools(ISysDocumentService documentService, AgentTraceService agentTraceService) {
        this.documentService = documentService;
        this.agentTraceService = agentTraceService;
    }

    @Tool(name = "search_lab_documents", description = "按文档标题或摘要检索当前用户有权限访问的 Lab201 知识库文档")
    public String searchLabDocuments(
            @ToolParam(description = "检索关键词，例如 CUDA、serverG1、Ubuntu 换源") String keyword) {
        return agentTraceService.traceTool(
                "search_lab_documents",
                "检索知识库文档",
                args("keyword", keyword),
                () -> doSearchLabDocuments(keyword)
        );
    }

    private String doSearchLabDocuments(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return "检索关键词不能为空。";
        }

        AgentTraceContext.Context context = AgentTraceContext.current();
        Long userId = context == null ? null : context.getUserId();
        String role = context == null ? null : context.getRole();
        LambdaQueryWrapper<SysDocument> wrapper = new LambdaQueryWrapper<SysDocument>()
                .eq(SysDocument::getIsRag, 1)
                .eq(SysDocument::getParseStatus, 2)
                .and(w -> w.like(SysDocument::getTitle, keyword.trim())
                        .or()
                        .like(SysDocument::getSummary, keyword.trim()));

        if ("admin".equals(role)) {
            // 管理员可以检索所有已加入 RAG 且解析成功的文档。
        } else if (userId != null) {
            wrapper.and(w -> w.eq(SysDocument::getRagScope, 1)
                    .or()
                    .eq(SysDocument::getUploaderId, userId));
        } else {
            wrapper.eq(SysDocument::getRagScope, 1);
        }

        List<SysDocument> documents = documentService.list(wrapper.orderByDesc(SysDocument::getUpdateTime).last("limit 5"));
        String result = documents.stream()
                .map(doc -> String.format("- 文档ID: %s, 标题: %s, 文件名: %s, 摘要: %s",
                        doc.getId(), doc.getTitle(), doc.getFileName(), safe(doc.getSummary())))
                .collect(Collectors.joining("\n"));
        return result.isBlank() ? "没有检索到匹配的可访问知识库文档。" : result;
    }

    private Map<String, Object> args(Object... keyValues) {
        Map<String, Object> args = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            args.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return args;
    }

    private String safe(String value) {
        return StringUtils.hasText(value) ? value : "无";
    }
}
