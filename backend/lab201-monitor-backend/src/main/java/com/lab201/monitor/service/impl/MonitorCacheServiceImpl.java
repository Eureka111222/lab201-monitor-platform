package com.lab201.monitor.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab201.monitor.common.config.MonitorIntegrationProperties;
import com.lab201.monitor.common.constant.MonitorCacheConstants;
import com.lab201.monitor.entity.GpuMetricLog;
import com.lab201.monitor.entity.ServerNode;
import com.lab201.monitor.service.IGpuMetricLogService;
import com.lab201.monitor.service.IServerNodeService;
import com.lab201.monitor.service.MonitorCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MonitorCacheServiceImpl implements MonitorCacheService {

    private static final TypeReference<List<ServerNode>> SERVER_NODE_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<GpuMetricLog>> GPU_LOG_LIST_TYPE = new TypeReference<>() {
    };

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IServerNodeService serverNodeService;

    @Autowired
    private IGpuMetricLogService gpuMetricLogService;

    @Autowired
    private MonitorIntegrationProperties properties;

    @Override
    public List<ServerNode> getServerNodeList() {
        String cacheValue = stringRedisTemplate.opsForValue().get(MonitorCacheConstants.SERVER_LIST_KEY);
        if (StringUtils.hasText(cacheValue)) {
            List<ServerNode> cachedNodes = deserialize(cacheValue, SERVER_NODE_LIST_TYPE, "server list");
            if (cachedNodes != null) {
                return applyOfflineThreshold(cachedNodes);
            }
        }

        List<ServerNode> serverNodes = serverNodeService.list();
        writeServerNodeList(serverNodes);
        return applyOfflineThreshold(new ArrayList<>(serverNodes));
    }

    @Override
    public List<GpuMetricLog> getLatestGpuLogs(String serverName) {
        if (!StringUtils.hasText(serverName)) {
            return List.of();
        }

        String cacheKey = MonitorCacheConstants.gpuLatestKey(serverName);
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cacheValue)) {
            List<GpuMetricLog> cachedLogs = deserialize(cacheValue, GPU_LOG_LIST_TYPE, "gpu latest logs");
            if (cachedLogs != null) {
                return cachedLogs;
            }
        }

        List<GpuMetricLog> latestLogs = gpuMetricLogService.listLatestByServerName(serverName);
        cacheLatestGpuLogs(serverName, latestLogs);
        return latestLogs;
    }

    @Override
    public void cacheLatestGpuLogs(String serverName, List<GpuMetricLog> logs) {
        if (!StringUtils.hasText(serverName)) {
            return;
        }

        writeCacheValue(
                MonitorCacheConstants.gpuLatestKey(serverName),
                logs == null ? List.of() : logs,
                Duration.ofMinutes(properties.getCache().getLatestGpuTtlMinutes())
        );
    }

    @Override
    public void updateServerHeartbeat(String serverName, LocalDateTime heartbeatTime) {
        if (!StringUtils.hasText(serverName) || heartbeatTime == null) {
            return;
        }

        String cacheValue = stringRedisTemplate.opsForValue().get(MonitorCacheConstants.SERVER_LIST_KEY);
        if (!StringUtils.hasText(cacheValue)) {
            return;
        }

        List<ServerNode> cachedNodes = deserialize(cacheValue, SERVER_NODE_LIST_TYPE, "server list");
        if (cachedNodes == null || cachedNodes.isEmpty()) {
            return;
        }

        boolean updated = false;
        for (ServerNode node : cachedNodes) {
            if (serverName.equals(node.getServerName())) {
                node.setLastHeartbeat(heartbeatTime);
                node.setStatus(1);
                updated = true;
                break;
            }
        }

        if (updated) {
            writeServerNodeList(cachedNodes);
        }
    }

    @Override
    public void evictServerListCache() {
        stringRedisTemplate.delete(MonitorCacheConstants.SERVER_LIST_KEY);
    }

    @Override
    public void evictLatestGpuCache(String serverName) {
        if (!StringUtils.hasText(serverName)) {
            return;
        }
        stringRedisTemplate.delete(MonitorCacheConstants.gpuLatestKey(serverName));
    }

    private List<ServerNode> applyOfflineThreshold(List<ServerNode> serverNodes) {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(properties.getCache().getOfflineThresholdSeconds());
        for (ServerNode node : serverNodes) {
            LocalDateTime lastHeartbeat = node.getLastHeartbeat();
            node.setStatus(lastHeartbeat != null && !lastHeartbeat.isBefore(threshold) ? 1 : 0);
        }
        return serverNodes;
    }

    private void writeServerNodeList(List<ServerNode> serverNodes) {
        writeCacheValue(
                MonitorCacheConstants.SERVER_LIST_KEY,
                serverNodes == null ? List.of() : serverNodes,
                Duration.ofMinutes(properties.getCache().getServerListTtlMinutes())
        );
    }

    private void writeCacheValue(String key, Object value, Duration ttl) {
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (JsonProcessingException ex) {
            log.error("写入 Redis 缓存失败, key={}", key, ex);
        }
    }

    private <T> T deserialize(String json, TypeReference<T> typeReference, String cacheName) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException ex) {
            log.warn("解析 Redis 缓存失败, cacheName={}", cacheName, ex);
            return null;
        }
    }
}
