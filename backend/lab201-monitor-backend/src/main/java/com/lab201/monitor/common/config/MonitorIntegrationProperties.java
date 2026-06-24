package com.lab201.monitor.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "monitor.integration")
public class MonitorIntegrationProperties {

    private final Amqp amqp = new Amqp();
    private final Cache cache = new Cache();

    @Data
    public static class Amqp {
        private String gpuReportExchange = "lab201.monitor.gpu.exchange";
        private String gpuReportQueue = "lab201.monitor.gpu.queue";
        private String gpuReportRoutingKey = "lab201.monitor.gpu.report";
    }

    @Data
    public static class Cache {
        private long serverListTtlMinutes = 30;
        private long latestGpuTtlMinutes = 10;
        private long offlineThresholdSeconds = 20;
        private long gpuReportIdempotencyDays = 3;
        private long gpuReportProcessingExpireSeconds = 30;
    }
}
