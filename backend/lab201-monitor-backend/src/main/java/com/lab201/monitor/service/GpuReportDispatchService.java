package com.lab201.monitor.service;

import com.lab201.monitor.entity.dto.GpuReportDto;

public interface GpuReportDispatchService {

    void dispatch(GpuReportDto reportDto);
}
