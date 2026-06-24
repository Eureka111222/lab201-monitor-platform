package com.lab201.monitor.ai.tool;

import com.lab201.monitor.entity.GpuMetricLog;
import com.lab201.monitor.entity.ServerNode;
import com.lab201.monitor.service.MonitorCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ServerStatusTools {

    private final MonitorCacheService monitorCacheService;

    public ServerStatusTools(MonitorCacheService monitorCacheService) {
        this.monitorCacheService = monitorCacheService;
    }

    @Tool(name = "list_lab_servers", description = "查询 Lab201 所有服务器的在线状态、IP、显卡型号和最后心跳时间")
    public String listLabServers() {
        long startNanos = System.nanoTime();
        try {
            List<ServerNode> servers = monitorCacheService.getServerNodeList();
            String result = servers.stream()
                    .sorted(Comparator.comparing(ServerNode::getServerName, Comparator.nullsLast(String::compareTo)))
                    .map(this::formatServerNode)
                    .collect(Collectors.joining("\n"));

            long costMs = elapsedMillis(startNanos);
            log.info(">>> [AI_TOOL] tool=list_lab_servers status=success serverCount={} costMs={}",
                    servers.size(), costMs);
            return result.isBlank() ? "当前没有登记服务器信息。" : result;
        } catch (Exception ex) {
            long costMs = elapsedMillis(startNanos);
            log.error(">>> [AI_TOOL] tool=list_lab_servers status=failed costMs={} error={}",
                    costMs, ex.getMessage(), ex);
            return "查询服务器列表失败，请稍后重试。";
        }
    }

    @Tool(name = "get_gpu_status", description = "根据服务器主机名查询该服务器最新 GPU 利用率、显存、温度、功耗和占用用户")
    public String getGpuStatus(
            @ToolParam(description = "服务器主机名，例如 gpu-server-01、lab201-a100-01") String serverName) {
        long startNanos = System.nanoTime();
        if (!StringUtils.hasText(serverName)) {
            return "服务器主机名不能为空。";
        }

        try {
            List<GpuMetricLog> gpuLogs = monitorCacheService.getLatestGpuLogs(serverName.trim());
            String result = gpuLogs.stream()
                    .sorted(Comparator.comparing(GpuMetricLog::getGpuIndex, Comparator.nullsLast(Integer::compareTo)))
                    .map(this::formatGpuLog)
                    .collect(Collectors.joining("\n"));

            long costMs = elapsedMillis(startNanos);
            log.info(">>> [AI_TOOL] tool=get_gpu_status status=success serverName={} gpuCount={} costMs={}",
                    serverName, gpuLogs.size(), costMs);
            return result.isBlank()
                    ? "没有查询到服务器 " + serverName + " 的最新 GPU 状态，可能该服务器未上报或名称不正确。"
                    : "服务器 " + serverName + " 的最新 GPU 状态如下：\n" + result;
        } catch (Exception ex) {
            long costMs = elapsedMillis(startNanos);
            log.error(">>> [AI_TOOL] tool=get_gpu_status status=failed serverName={} costMs={} error={}",
                    serverName, costMs, ex.getMessage(), ex);
            return "查询服务器 " + serverName + " 的 GPU 状态失败，请稍后重试。";
        }
    }

    private String formatServerNode(ServerNode node) {
        String status = Integer.valueOf(1).equals(node.getStatus()) ? "在线" : "离线";
        return String.format(
                "- 主机名: %s, 状态: %s, 内网IP: %s, 公网IP: %s, 显卡型号: %s, 位置: %s, 最后心跳: %s",
                safe(node.getServerName()),
                status,
                safe(node.getServerIp()),
                safe(node.getPublicIp()),
                safe(node.getGpuModel()),
                safe(node.getLocation()),
                node.getLastHeartbeat() == null ? "未知" : node.getLastHeartbeat()
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

    private String safe(String value) {
        return StringUtils.hasText(value) ? value : "未知";
    }

    private String value(Object value) {
        return value == null ? "未知" : value.toString();
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
