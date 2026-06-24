package com.lab201.monitor.ai.tool;

import com.lab201.monitor.entity.GpuMetricLog;
import com.lab201.monitor.entity.dto.GpuTrendResponseDto;
import com.lab201.monitor.service.AgentTraceService;
import com.lab201.monitor.service.IGpuMetricLogService;
import com.lab201.monitor.service.MonitorCacheService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GpuTools {

    private final MonitorCacheService monitorCacheService;

    private final IGpuMetricLogService gpuMetricLogService;

    private final AgentTraceService agentTraceService;

    public GpuTools(MonitorCacheService monitorCacheService,
                    IGpuMetricLogService gpuMetricLogService,
                    AgentTraceService agentTraceService) {
        this.monitorCacheService = monitorCacheService;
        this.gpuMetricLogService = gpuMetricLogService;
        this.agentTraceService = agentTraceService;
    }

    @Tool(name = "get_gpu_status", description = "根据服务器主机名查询该服务器最新 GPU 利用率、显存、温度、功耗和占用用户")
    public String getGpuStatus(
            @ToolParam(description = "服务器主机名，例如 gpu-server-01、lab201-a100-01") String serverName) {
        return agentTraceService.traceTool(
                "get_gpu_status",
                "查询 GPU 实时状态",
                args("serverName", serverName),
                () -> doGetGpuStatus(serverName)
        );
    }

    @Tool(name = "query_gpu_trend", description = "查询某台服务器最近几天 GPU 利用率、显存和用户占用趋势摘要")
    public String queryGpuTrend(
            @ToolParam(description = "服务器主机名，例如 serverG1") String serverName,
            @ToolParam(description = "最近几天，默认7天，最大30天") Integer days) {
        return agentTraceService.traceTool(
                "query_gpu_trend",
                "查询 GPU 历史趋势",
                args("serverName", serverName, "days", days),
                () -> doQueryGpuTrend(serverName, days)
        );
    }

    private String doGetGpuStatus(String serverName) {
        if (!StringUtils.hasText(serverName)) {
            return "服务器主机名不能为空。";
        }

        List<GpuMetricLog> gpuLogs = monitorCacheService.getLatestGpuLogs(serverName.trim());
        String result = gpuLogs.stream()
                .sorted(Comparator.comparing(GpuMetricLog::getGpuIndex, Comparator.nullsLast(Integer::compareTo)))
                .map(this::formatGpuLog)
                .collect(Collectors.joining("\n"));

        return result.isBlank()
                ? "没有查询到服务器 " + serverName + " 的最新 GPU 状态，可能该服务器未上报或名称不正确。"
                : "服务器 " + serverName + " 的最新 GPU 状态如下：\n" + result;
    }

    private String doQueryGpuTrend(String serverName, Integer days) {
        if (!StringUtils.hasText(serverName)) {
            return "服务器主机名不能为空。";
        }

        GpuTrendResponseDto trend = gpuMetricLogService.getTrend(serverName.trim(), days == null ? 7 : days, 5);
        int pointCount = trend.getPoints() == null ? 0 : trend.getPoints().size();
        int userCount = trend.getUserUsages() == null ? 0 : trend.getUserUsages().size();
        return String.format(
                "服务器 %s 最近 %s 天 GPU 趋势已聚合完成，时间范围 %s 至 %s，趋势点数量 %s，用户占用统计数量 %s。请结合趋势点和用户占用统计总结是否存在长期高负载、显存占满或异常占用。",
                trend.getServerName(),
                trend.getDays(),
                trend.getStartTime(),
                trend.getEndTime(),
                pointCount,
                userCount
        );
    }

    private String formatGpuLog(GpuMetricLog log) {
        return String.format(
                "- GPU %s: %s, 利用率 %s%%, 显存 %s/%s MB, 温度 %s℃, 风扇 %s%%, 功耗 %s W, 占用用户: %s",
                value(log.getGpuIndex()),
                safe(log.getGpuName()),
                value(log.getUtilization()),
                value(log.getMemoryUsed()),
                value(log.getMemoryTotal()),
                value(log.getTemperature()),
                value(log.getFanSpeed()),
                value(log.getPowerUsage()),
                safe(log.getUserName())
        );
    }

    private Map<String, Object> args(Object... keyValues) {
        Map<String, Object> args = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            args.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return args;
    }

    private String safe(String value) {
        return StringUtils.hasText(value) ? value : "未知";
    }

    private String value(Object value) {
        return value == null ? "未知" : value.toString();
    }
}
