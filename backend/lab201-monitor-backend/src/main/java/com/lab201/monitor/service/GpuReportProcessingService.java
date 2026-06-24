package com.lab201.monitor.service;

import com.lab201.monitor.entity.dto.GpuReportDto;

public interface GpuReportProcessingService {

    void process(GpuReportDto reportDto);
}
