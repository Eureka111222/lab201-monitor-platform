package com.lab201.monitor.service;

import com.lab201.monitor.entity.AiConversation;
import com.lab201.monitor.entity.AiMessage;

import java.util.List;

public interface AiChatMemoryService {

    AiConversation createConversation(Long userId, String title, String modelProvider, String modelName);

    AiConversation ensureConversation(String conversationId, Long userId, String firstQuery, String modelProvider, String modelName);

    List<AiConversation> listConversations(Long userId);

    List<AiMessage> listMessages(Long userId, String conversationId);

    List<AiMessage> listRecentContext(Long userId, String conversationId, int maxMessages);

    AiMessage saveUserMessage(String conversationId, Long userId, String content);

    AiMessage createAssistantMessage(String conversationId, Long userId, String modelProvider, String modelName);

    void markAssistantSuccess(Long messageId, String conversationId, Long userId, String content, long latencyMs);

    void markAssistantFailed(Long messageId, String conversationId, Long userId, String errorMessage, long latencyMs);

    void deleteConversation(Long userId, String conversationId);
}
