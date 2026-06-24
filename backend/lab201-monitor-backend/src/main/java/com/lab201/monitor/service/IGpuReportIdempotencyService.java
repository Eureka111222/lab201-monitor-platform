package com.lab201.monitor.service;

import com.lab201.monitor.entity.dto.GpuReportDto;

public interface IGpuReportIdempotencyService {

    String ensureReportId(GpuReportDto reportDto);

    boolean tryAcquire(String reportId, String serverName);

    void markProcessed(String reportId, String serverName);

    void release(String reportId);
}
