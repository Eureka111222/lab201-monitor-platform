package com.lab201.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lab201.monitor.common.domain.BaseLogEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "gpu_metric_log")
@Schema(description = "GPU监控日志表")
public class GpuMetricLog extends BaseLogEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主机名")
    @TableField(value = "server_name")
    private String serverName;

    @Schema(description = "显卡索引")
    @TableField(value = "gpu_index")
    private Integer gpuIndex;

    @Schema(description = "显卡型号")
    @TableField(value = "gpu_name")
    private String gpuName;

    @Schema(description = "利用率(%)")
    @TableField(value = "utilization")
    private Integer utilization;

    @Schema(description = "已用显存(MB)")
    @TableField(value = "memory_used")
    private Integer memoryUsed;

    @Schema(description = "总显存(MB)")
    @TableField(value = "memory_total")
    private Integer memoryTotal;

    @Schema(description = "温度(℃)")
    @TableField(value = "temperature")
    private Integer temperature;

    @Schema(description = "风扇转速(%)")
    @TableField(value = "fan_speed")
    private Integer fanSpeed;

    @Schema(description = "功率(W)")
    @TableField(value = "power_usage")
    private Integer powerUsage;

    @Schema(description = "当前占用者")
    @TableField(value = "user_name")
    private String userName;
}