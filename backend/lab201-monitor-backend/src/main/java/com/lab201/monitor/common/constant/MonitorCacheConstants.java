package com.lab201.monitor.common.constant;

public final class MonitorCacheConstants {

    public static final String SERVER_LIST_KEY = "lab201:server:list";
    public static final String GPU_LATEST_PREFIX = "lab201:gpu:latest:";
    public static final String GPU_REPORT_IDEMPOTENCY_PREFIX = "lab201:gpu:report:idempotency:";

    private MonitorCacheConstants() {
    }

    public static String gpuLatestKey(String serverName) {
        return GPU_LATEST_PREFIX + serverName;
    }

    public static String gpuReportIdempotencyKey(String bucket) {
        return GPU_REPORT_IDEMPOTENCY_PREFIX + bucket;
    }
}
