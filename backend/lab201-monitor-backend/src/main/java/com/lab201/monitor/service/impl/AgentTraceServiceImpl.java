package com.lab201.monitor.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab201.monitor.ai.agent.AgentTraceContext;
import com.lab201.monitor.entity.AiAgentStep;
import com.lab201.monitor.entity.AiAgentTask;
import com.lab201.monitor.entity.dto.AgentStepEventDto;
import com.lab201.monitor.entity.dto.RagCitationDto;
import com.lab201.monitor.service.AgentTraceService;
import com.lab201.monitor.service.IAiAgentStepService;
import com.lab201.monitor.service.IAiAgentTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Service
public class AgentTraceServiceImpl implements AgentTraceService {

    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final int MAX_TEXT_LENGTH = 6000;
    private static final int MAX_ERROR_LENGTH = 1000;

    @Autowired
    private IAiAgentTaskService aiAgentTaskService;

    @Autowired
    private IAiAgentStepService aiAgentStepService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public AiAgentTask createTask(String conversationId, Long userId, String query, String modelProvider, String modelName) {
        AiAgentTask task = new AiAgentTask();
        task.setTaskId("task_" + UUID.randomUUID().toString().replace("-", ""));
        task.setConversationId(conversationId);
        task.setUserId(userId);
        task.setQueryText(query);
        task.setStatus(STATUS_RUNNING);
        task.setModelProvider(modelProvider);
        task.setModelName(modelName);
        aiAgentTaskService.save(task);
        return task;
    }

    @Override
    public void markTaskSuccess(String taskId, String finalAnswer, List<RagCitationDto> citations, long costMs) {
        if (!StringUtils.hasText(taskId)) {
            return;
        }
        Long id = resolveTaskId(taskId);
        if (id == null) {
            return;
        }

        AiAgentTask task = new AiAgentTask();
        task.setId(id);
        task.setStatus(STATUS_SUCCESS);
        task.setFinalAnswer(trim(finalAnswer, MAX_TEXT_LENGTH));
        task.setCitationsJson(writeJson(citations == null ? List.of() : citations));
        task.setCostMs(costMs);
        aiAgentTaskService.updateById(task);
    }

    @Override
    public void markTaskFailed(String taskId, String errorMessage, long costMs) {
        if (!StringUtils.hasText(taskId)) {
            return;
        }
        Long id = resolveTaskId(taskId);
        if (id == null) {
            return;
        }

        AiAgentTask task = new AiAgentTask();
        task.setId(id);
        task.setStatus(STATUS_FAILED);
        task.setErrorMessage(trim(errorMessage, MAX_ERROR_LENGTH));
        task.setCostMs(costMs);
        aiAgentTaskService.updateById(task);
    }

    @Override
    public String traceTool(String toolName, String displayName, Map<String, Object> args, Supplier<String> supplier) {
        return traceOperation(toolName, displayName, args, supplier, result -> result);
    }

    @Override
    public <T> T traceOperation(String toolName, String displayName, Map<String, Object> args,
                                Supplier<T> supplier, Function<T, String> resultFormatter) {
        AgentTraceContext.Context context = AgentTraceContext.current();
        if (context == null) {
            return supplier.get();
        }

        int stepOrder = context.nextStepOrder();
        long startNanos = System.nanoTime();
        AiAgentStep step = new AiAgentStep();
        step.setTaskId(context.getTaskId());
        step.setStepOrder(stepOrder);
        step.setToolName(toolName);
        step.setDisplayName(displayName);
        step.setToolArgs(writeJson(args == null ? Map.of() : args));
        step.setStatus(STATUS_RUNNING);
        aiAgentStepService.save(step);
        emitStep(context, stepOrder, toolName, displayName, STATUS_RUNNING, null);

        try {
            T result = supplier.get();
            long costMs = elapsedMillis(startNanos);
            String resultText = resultFormatter == null ? String.valueOf(result) : resultFormatter.apply(result);
            updateStep(step.getId(), STATUS_SUCCESS, resultText, null, costMs);
            emitStep(context, stepOrder, toolName, displayName, STATUS_SUCCESS, costMs);
            log.info(">>> [AGENT_TOOL] taskId={} step={} tool={} status=success costMs={}",
                    context.getTaskId(), stepOrder, toolName, costMs);
            return result;
        } catch (Exception ex) {
            long costMs = elapsedMillis(startNanos);
            updateStep(step.getId(), STATUS_FAILED, null, ex.getMessage(), costMs);
            emitStep(context, stepOrder, toolName, displayName, STATUS_FAILED, costMs);
            log.error(">>> [AGENT_TOOL] taskId={} step={} tool={} status=failed costMs={} error={}",
                    context.getTaskId(), stepOrder, toolName, costMs, ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public String buildCitationsEvent(List<RagCitationDto> citations) {
        return STREAM_EVENT_CITATIONS_PREFIX + writeJson(citations == null ? List.of() : citations);
    }

    private void updateStep(Long stepId, String status, String result, String errorMessage, long costMs) {
        if (stepId == null) {
            return;
        }

        AiAgentStep step = new AiAgentStep();
        step.setId(stepId);
        step.setStatus(status);
        step.setToolResult(trim(result, MAX_TEXT_LENGTH));
        step.setErrorMessage(trim(errorMessage, MAX_ERROR_LENGTH));
        step.setCostMs(costMs);
        aiAgentStepService.updateById(step);
    }

    private Long resolveTaskId(String taskId) {
        AiAgentTask task = aiAgentTaskService.lambdaQuery()
                .eq(AiAgentTask::getTaskId, taskId)
                .last("limit 1")
                .one();
        return task == null ? null : task.getId();
    }

    private void emitStep(AgentTraceContext.Context context, Integer stepOrder, String toolName,
                          String displayName, String status, Long costMs) {
        if (context.getEventSink() == null) {
            return;
        }
        AgentStepEventDto event = new AgentStepEventDto(
                context.getTaskId(), stepOrder, toolName, displayName, status, costMs
        );
        context.getEventSink().tryEmitNext(STREAM_EVENT_STEP_PREFIX + writeJson(event));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private String trim(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
