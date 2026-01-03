package com.lab201.monitor.controller;

import com.lab201.monitor.common.result.Result;
import com.lab201.monitor.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@Tag(name = "07. AI 知识库问答")
@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    @Autowired
    private RagService ragService;

    @Operation(summary = "知识库对话", description = "系统会根据上传的文档内容回答问题")
    @GetMapping("/chat")
    public Result<String> chat(@RequestParam(value = "query",defaultValue = "你好") String query) {
        // 1. 校验参数
        if (query == null || query.trim().isEmpty()) {
            return Result.fail(400, "问题不能为空");
        }

        // 2. 调用 Service 进行 RAG 检索生成
        try {
            String answer = ragService.chat(query);
            return Result.success(answer);
        } catch (Exception e) {
            log.error("RAG 对话失败: {}", e.getMessage(), e);
            return Result.fail(500, "AI 响应失败，请稍后重试");
        }
    }

    @Operation(summary = "流式知识库对话", description = "像ChatGPT一样逐字返回结果")
// 💡 加上 charset=UTF-8 防止某些浏览器乱码
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> chatStream(@RequestParam(value = "query", defaultValue = "你好") String query) {
        if (query == null || query.trim().isEmpty()) {
            return Flux.just("问题不能为空");
        }

        return ragService.chatStream(query)
                .onErrorResume(e -> {
                    log.error("流式对话异常", e);
                    // 发生错误时，返回一个友好的错误提示流
                    return Flux.just("抱歉，AI 响应出现异常：" + e.getMessage());
                });
    }
}