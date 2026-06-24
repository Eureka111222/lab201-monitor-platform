package com.lab201.monitor.entity.dto;

import com.lab201.monitor.entity.GpuMetricLog;
import com.lab201.monitor.entity.ServerNode;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GpuDashboardOverviewDto {

    private List<ServerNode> servers;

    private Map<String, List<GpuMetricLog>> latestLogs;
}
