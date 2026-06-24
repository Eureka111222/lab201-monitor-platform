package com.lab201.monitor.ai.agent;

import lombok.Getter;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicInteger;

public final class AgentTraceContext {

    private static final ThreadLocal<Context> CURRENT = new InheritableThreadLocal<>();

    private AgentTraceContext() {
    }

    public static Context current() {
        return CURRENT.get();
    }

    public static void open(String taskId, Long userId, String role, Sinks.Many<String> eventSink) {
        CURRENT.set(new Context(taskId, userId, role, eventSink));
    }

    public static void close() {
        CURRENT.remove();
    }

    @Getter
    public static class Context {
        private final String taskId;
        private final Long userId;
        private final String role;
        private final Sinks.Many<String> eventSink;
        private final AtomicInteger stepCounter = new AtomicInteger();

        private Context(String taskId, Long userId, String role, Sinks.Many<String> eventSink) {
            this.taskId = taskId;
            this.userId = userId;
            this.role = role;
            this.eventSink = eventSink;
        }

        public int nextStepOrder() {
            return stepCounter.incrementAndGet();
        }
    }
}
