package com.lab201.monitor.service;

import com.lab201.monitor.entity.AiAgentTask;
import com.lab201.monitor.entity.dto.RagCitationDto;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface AgentTraceService {

    String STREAM_EVENT_STEP_PREFIX = "__LAB201_AGENT_STEP__:";

    String STREAM_EVENT_CITATIONS_PREFIX = "__LAB201_CITATIONS__:";

    AiAgentTask createTask(String conversationId, Long userId, String query, String modelProvider, String modelName);

    void markTaskSuccess(String taskId, String finalAnswer, List<RagCitationDto> citations, long costMs);

    void markTaskFailed(String taskId, String errorMessage, long costMs);

    String traceTool(String toolName, String displayName, Map<String, Object> args, Supplier<String> supplier);

    <T> T traceOperation(String toolName, String displayName, Map<String, Object> args,
                         Supplier<T> supplier, Function<T, String> resultFormatter);

    String buildCitationsEvent(List<RagCitationDto> citations);
}
