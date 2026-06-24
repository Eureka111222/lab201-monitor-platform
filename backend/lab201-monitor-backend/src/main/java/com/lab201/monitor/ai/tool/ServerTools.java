package com.lab201.monitor.ai.tool;

import com.lab201.monitor.entity.ServerNode;
import com.lab201.monitor.service.AgentTraceService;
import com.lab201.monitor.service.MonitorCacheService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ServerTools {

    private final MonitorCacheService monitorCacheService;

    private final AgentTraceService agentTraceService;

    public ServerTools(MonitorCacheService monitorCacheService, AgentTraceService agentTraceService) {
        this.monitorCacheService = monitorCacheService;
        this.agentTraceService = agentTraceService;
    }

    @Tool(name = "list_lab_servers", description = "查询 Lab201 所有服务器的在线状态、IP、显卡型号、位置和最后心跳时间")
    public String listLabServers() {
        return agentTraceService.traceTool(
                "list_lab_servers",
                "查询服务器列表",
                Map.of(),
                this::doListLabServers
        );
    }

    private String doListLabServers() {
        List<ServerNode> servers = monitorCacheService.getServerNodeList();
        String result = servers.stream()
                .sorted(Comparator.comparing(ServerNode::getServerName, Comparator.nullsLast(String::compareTo)))
                .map(this::formatServerNode)
                .collect(Collectors.joining("\n"));
        return result.isBlank() ? "当前没有登记服务器信息。" : result;
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

    private String safe(String value) {
        return StringUtils.hasText(value) ? value : "未知";
    }
}
