package com.lab201.monitor.controller;

import com.lab201.monitor.common.login.LoginUser;
import com.lab201.monitor.common.login.LoginUserHolder;
import com.lab201.monitor.common.result.Result;
import com.lab201.monitor.entity.AiConversation;
import com.lab201.monitor.entity.AiMessage;
import com.lab201.monitor.entity.dto.AiConversationCreateDto;
import com.lab201.monitor.service.AiChatMemoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AI 会话记忆")
@RestController
@RequestMapping("/api/v1/ai/conversations")
public class AiConversationController {

    @Autowired
    private AiChatMemoryService aiChatMemoryService;

    @Operation(summary = "创建 AI 会话")
    @PostMapping
    public Result<AiConversation> create(@RequestBody(required = false) AiConversationCreateDto dto) {
        LoginUser loginUser = requireLoginUser();
        String title = dto == null ? null : dto.getTitle();
        AiConversation conversation = aiChatMemoryService.createConversation(loginUser.getUserId(), title, null, null);
        return Result.success(conversation);
    }

    @Operation(summary = "查询当前用户 AI 会话列表")
    @GetMapping
    public Result<List<AiConversation>> list() {
        LoginUser loginUser = requireLoginUser();
        return Result.success(aiChatMemoryService.listConversations(loginUser.getUserId()));
    }

    @Operation(summary = "查询 AI 会话消息")
    @GetMapping("/{conversationId}/messages")
    public Result<List<AiMessage>> messages(@PathVariable String conversationId) {
        LoginUser loginUser = requireLoginUser();
        if (!StringUtils.hasText(conversationId)) {
            return Result.fail(400, "会话 ID 不能为空");
        }
        return Result.success(aiChatMemoryService.listMessages(loginUser.getUserId(), conversationId));
    }

    @Operation(summary = "删除 AI 会话")
    @DeleteMapping("/{conversationId}")
    public Result<Void> delete(@PathVariable String conversationId) {
        LoginUser loginUser = requireLoginUser();
        aiChatMemoryService.deleteConversation(loginUser.getUserId(), conversationId);
        return Result.success();
    }

    private LoginUser requireLoginUser() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new IllegalArgumentException("请先登录后再使用 AI 助手");
        }
        return loginUser;
    }
}
