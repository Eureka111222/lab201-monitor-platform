package com.lab201.monitor.service.impl;

import com.lab201.monitor.entity.GpuMetricLog;
import com.lab201.monitor.entity.dto.GpuReportDto;
import com.lab201.monitor.service.GpuReportProcessingService;
import com.lab201.monitor.service.IGpuMetricLogService;
import com.lab201.monitor.service.IGpuReportIdempotencyService;
import com.lab201.monitor.service.IServerNodeService;
import com.lab201.monitor.service.MonitorCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GpuReportProcessingServiceImpl implements GpuReportProcessingService {

    @Autowired
    private IGpuMetricLogService gpuMetricLogService;

    @Autowired
    private IServerNodeService serverNodeService;

    @Autowired
    private MonitorCacheService monitorCacheService;

    @Autowired
    private IGpuReportIdempotencyService gpuReportIdempotencyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process(GpuReportDto reportDto) {
        if (reportDto == null || !StringUtils.hasText(reportDto.getServerName())) {
            log.warn("Ignore empty GPU report message");
            return;
        }

        String serverName = reportDto.getServerName().trim();
        String reportId = gpuReportIdempotencyService.ensureReportId(reportDto);
        if (!gpuReportIdempotencyService.tryAcquire(reportId, serverName)) {
            return;
        }

        List<GpuMetricLog> rawLogs = reportDto.getLogs() == null ? List.of() : reportDto.getLogs();
        List<GpuMetricLog> logs = new ArrayList<>(rawLogs.size());
        LocalDateTime heartbeatTime = LocalDateTime.now();

        for (GpuMetricLog logItem : rawLogs) {
            if (logItem == null) {
                continue;
            }
            logItem.setServerName(serverName);
            if (logItem.getCreateTime() == null) {
                logItem.setCreateTime(heartbeatTime);
            }
            logs.add(logItem);
        }

        if (!logs.isEmpty()) {
            gpuMetricLogService.saveBatch(logs);
        }

        serverNodeService.updateHeartbeat(serverName);
        registerAfterTransaction(reportId, serverName, logs, heartbeatTime);

        log.debug("GPU report processed, reportId={}, server={}, logCount={}", reportId, serverName, logs.size());
    }

    private void registerAfterTransaction(String reportId, String serverName, List<GpuMetricLog> logs, LocalDateTime heartbeatTime) {
        Runnable refreshTask = () -> {
            monitorCacheService.cacheLatestGpuLogs(serverName, logs);
            monitorCacheService.updateServerHeartbeat(serverName, heartbeatTime);
        };

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            try {
                gpuReportIdempotencyService.markProcessed(reportId, serverName);
            } catch (Exception ex) {
                log.error("Failed to mark GPU report as processed, reportId={}", reportId, ex);
            }
            refreshTask.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    gpuReportIdempotencyService.markProcessed(reportId, serverName);
                } catch (Exception ex) {
                    log.error("Failed to mark GPU report as processed, reportId={}", reportId, ex);
                }
                refreshTask.run();
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    gpuReportIdempotencyService.release(reportId);
                }
            }
        });
    }
}
