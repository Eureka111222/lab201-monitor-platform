package com.lab201.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab201.monitor.entity.AiConversation;
import com.lab201.monitor.entity.AiMessage;
import com.lab201.monitor.service.AiChatMemoryService;
import com.lab201.monitor.service.IAiConversationService;
import com.lab201.monitor.service.IAiMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class AiChatMemoryServiceImpl implements AiChatMemoryService {

    private static final String DEFAULT_TITLE = "新会话";
    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_STREAMING = "STREAMING";
    private static final String STATUS_FAILED = "FAILED";
    private static final int MAX_TITLE_LENGTH = 30;
    private static final int MAX_ERROR_LENGTH = 1000;

    @Autowired
    private IAiConversationService aiConversationService;

    @Autowired
    private IAiMessageService aiMessageService;

    @Override
    public AiConversation createConversation(Long userId, String title, String modelProvider, String modelName) {
        requireUser(userId);

        AiConversation conversation = new AiConversation();
        conversation.setConversationId("conv_" + UUID.randomUUID().toString().replace("-", ""));
        conversation.setUserId(userId);
        conversation.setTitle(StringUtils.hasText(title) ? trimToLength(title.trim(), MAX_TITLE_LENGTH) : DEFAULT_TITLE);
        conversation.setModelProvider(modelProvider);
        conversation.setModelName(modelName);
        aiConversationService.save(conversation);
        return conversation;
    }

    @Override
    public AiConversation ensureConversation(String conversationId, Long userId, String firstQuery, String modelProvider, String modelName) {
        requireUser(userId);

        if (!StringUtils.hasText(conversationId)) {
            return createConversation(userId, buildTitle(firstQuery), modelProvider, modelName);
        }

        AiConversation conversation = getOwnedConversation(userId, conversationId.trim());
        if (conversation == null) {
            throw new IllegalArgumentException("AI 会话不存在或无权限访问");
        }

        boolean changed = false;
        if (!StringUtils.hasText(conversation.getModelProvider()) && StringUtils.hasText(modelProvider)) {
            conversation.setModelProvider(modelProvider);
            changed = true;
        }
        if (!StringUtils.hasText(conversation.getModelName()) && StringUtils.hasText(modelName)) {
            conversation.setModelName(modelName);
            changed = true;
        }
        if (DEFAULT_TITLE.equals(conversation.getTitle()) && StringUtils.hasText(firstQuery)) {
            conversation.setTitle(buildTitle(firstQuery));
            changed = true;
        }
        if (changed) {
            aiConversationService.updateById(conversation);
        }
        return conversation;
    }

    @Override
    public List<AiConversation> listConversations(Long userId) {
        requireUser(userId);
        return aiConversationService.list(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .inSql(AiConversation::getConversationId,
                        "select distinct conversation_id from ai_message where user_id = "
                                + userId + " and deleted = 0")
                .orderByDesc(AiConversation::getUpdateTime)
                .orderByDesc(AiConversation::getId));
    }

    @Override
    public List<AiMessage> listMessages(Long userId, String conversationId) {
        requireOwnedConversation(userId, conversationId);
        return aiMessageService.list(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getUserId, userId)
                .eq(AiMessage::getConversationId, conversationId.trim())
                .orderByAsc(AiMessage::getCreateTime)
                .orderByAsc(AiMessage::getId));
    }

    @Override
    public List<AiMessage> listRecentContext(Long userId, String conversationId, int maxMessages) {
        requireOwnedConversation(userId, conversationId);
        int safeLimit = Math.max(1, Math.min(maxMessages, 40));
        Page<AiMessage> page = new Page<>(1, safeLimit, false);
        List<AiMessage> messages = aiMessageService.page(page, new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getUserId, userId)
                .eq(AiMessage::getConversationId, conversationId.trim())
                .in(AiMessage::getRole, ROLE_USER, ROLE_ASSISTANT)
                .eq(AiMessage::getStatus, STATUS_SUCCESS)
                .orderByDesc(AiMessage::getCreateTime)
                .orderByDesc(AiMessage::getId)).getRecords();
        Collections.reverse(messages);
        return messages;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiMessage saveUserMessage(String conversationId, Long userId, String content) {
        requireOwnedConversation(userId, conversationId);

        AiMessage message = new AiMessage();
        message.setConversationId(conversationId.trim());
        message.setUserId(userId);
        message.setRole(ROLE_USER);
        message.setContent(content);
        message.setStatus(STATUS_SUCCESS);
        aiMessageService.save(message);
        touchConversation(conversationId, userId, content, true);
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiMessage createAssistantMessage(String conversationId, Long userId, String modelProvider, String modelName) {
        requireOwnedConversation(userId, conversationId);

        AiMessage message = new AiMessage();
        message.setConversationId(conversationId.trim());
        message.setUserId(userId);
        message.setRole(ROLE_ASSISTANT);
        message.setContent("");
        message.setModelProvider(modelProvider);
        message.setModelName(modelName);
        message.setStatus(STATUS_STREAMING);
        aiMessageService.save(message);
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAssistantSuccess(Long messageId, String conversationId, Long userId, String content, long latencyMs) {
        updateAssistantMessage(messageId, content, STATUS_SUCCESS, null, latencyMs);
        touchConversation(conversationId, userId, null, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAssistantFailed(Long messageId, String conversationId, Long userId, String errorMessage, long latencyMs) {
        updateAssistantMessage(
                messageId,
                StringUtils.hasText(errorMessage) ? "抱歉，本次回答生成失败，请稍后重试。" : "",
                STATUS_FAILED,
                trimToLength(errorMessage, MAX_ERROR_LENGTH),
                latencyMs
        );
        touchConversation(conversationId, userId, null, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long userId, String conversationId) {
        requireOwnedConversation(userId, conversationId);
        aiConversationService.remove(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getConversationId, conversationId.trim()));
        aiMessageService.remove(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getUserId, userId)
                .eq(AiMessage::getConversationId, conversationId.trim()));
    }

    private void updateAssistantMessage(Long messageId, String content, String status, String errorMessage, long latencyMs) {
        if (messageId == null) {
            return;
        }

        AiMessage message = new AiMessage();
        message.setId(messageId);
        message.setContent(content);
        message.setStatus(status);
        message.setErrorMessage(errorMessage);
        message.setLatencyMs(latencyMs);
        aiMessageService.updateById(message);
    }

    private void touchConversation(String conversationId, Long userId, String firstQuery, boolean allowTitleUpdate) {
        if (!StringUtils.hasText(conversationId) || userId == null) {
            return;
        }

        String title = null;
        if (allowTitleUpdate && StringUtils.hasText(firstQuery)) {
            AiConversation conversation = getOwnedConversation(userId, conversationId.trim());
            if (conversation != null && DEFAULT_TITLE.equals(conversation.getTitle())) {
                title = buildTitle(firstQuery);
            }
        }

        LambdaUpdateWrapper<AiConversation> wrapper = new LambdaUpdateWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getConversationId, conversationId.trim())
                .set(AiConversation::getUpdateTime, LocalDateTime.now());

        if (StringUtils.hasText(title)) {
            wrapper.set(AiConversation::getTitle, title);
        }
        aiConversationService.update(wrapper);
    }

    private AiConversation requireOwnedConversation(Long userId, String conversationId) {
        requireUser(userId);
        if (!StringUtils.hasText(conversationId)) {
            throw new IllegalArgumentException("AI 会话 ID 不能为空");
        }
        AiConversation conversation = getOwnedConversation(userId, conversationId.trim());
        if (conversation == null) {
            throw new IllegalArgumentException("AI 会话不存在或无权限访问");
        }
        return conversation;
    }

    private AiConversation getOwnedConversation(Long userId, String conversationId) {
        return aiConversationService.getOne(new LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getConversationId, conversationId)
                .last("limit 1"));
    }

    private void requireUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("请先登录后再使用 AI 助手");
        }
    }

    private String buildTitle(String text) {
        if (!StringUtils.hasText(text)) {
            return DEFAULT_TITLE;
        }
        return trimToLength(text.replaceAll("\\s+", " ").trim(), MAX_TITLE_LENGTH);
    }

    private String trimToLength(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String trimmed = text.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
