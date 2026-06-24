package com.lab201.monitor.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GpuTrendResponseDto {

    private String serverName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer days;

    private Integer bucketMinutes;

    private List<GpuTrendPointDto> points;

    private List<GpuUserUsageDto> userUsages;
}
