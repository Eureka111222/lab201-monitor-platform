package com.lab201.monitor.entity.dto;

import lombok.Data;

@Data
public class GpuUserUsageDto {

    private Integer gpuIndex;

    private String userName;

    private Long sampleCount;

    private Double percentage;
}
