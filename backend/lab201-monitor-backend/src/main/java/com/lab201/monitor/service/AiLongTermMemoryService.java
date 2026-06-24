package com.lab201.monitor.service;

public interface AiLongTermMemoryService {

    String buildLongTermMemoryContext(Long userId);

    String buildConversationSummaryContext(Long userId, String conversationId);

    void rememberAfterTurn(Long userId, String conversationId, String userQuery, String assistantAnswer);

    void refreshConversationSummary(Long userId, String conversationId);
}
