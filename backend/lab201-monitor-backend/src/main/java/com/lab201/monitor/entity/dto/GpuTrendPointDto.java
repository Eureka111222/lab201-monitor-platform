package com.lab201.monitor.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GpuTrendPointDto {

    private Integer gpuIndex;

    private String gpuName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bucketTime;

    private Double avgUtilization;

    private Double avgMemoryUsed;

    private Double avgMemoryTotal;

    private Double avgMemoryPercent;

    private Double avgTemperature;

    private Double avgFanSpeed;

    private Double avgPowerUsage;

    private Long sampleCount;
}
