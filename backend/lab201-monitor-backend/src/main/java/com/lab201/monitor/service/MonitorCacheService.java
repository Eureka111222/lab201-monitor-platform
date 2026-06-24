package com.lab201.monitor.service;

import com.lab201.monitor.entity.GpuMetricLog;
import com.lab201.monitor.entity.ServerNode;

import java.time.LocalDateTime;
import java.util.List;

public interface MonitorCacheService {

    List<ServerNode> getServerNodeList();

    List<GpuMetricLog> getLatestGpuLogs(String serverName);

    void cacheLatestGpuLogs(String serverName, List<GpuMetricLog> logs);

    void updateServerHeartbeat(String serverName, LocalDateTime heartbeatTime);

    void evictServerListCache();

    void evictLatestGpuCache(String serverName);
}
