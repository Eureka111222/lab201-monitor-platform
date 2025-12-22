package com.lab201.monitor.entity.dto;

import com.lab201.monitor.entity.GpuMetricLog; // 复用你的实体类
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * GPU上报数据传输对象
 * 对应 Python 脚本发送的 JSON 结构
 */
@Data
@Schema(description = "GPU数据上报传输对象")
public class GpuReportDto {

    @Schema(description = "服务器主机名 (Python脚本中的 SERVER_NAME)")
    private String serverName;

    @Schema(description = "显卡日志列表 (Python脚本中的 logs)")
    private List<GpuMetricLog> logs; 
    // 这里可以直接复用 GpuMetricLog 实体，因为 logs 数组里的字段和 Entity 是一致的
}