package com.lab201.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.lab201.monitor.common.domain.BaseEntity;
import com.lab201.monitor.entity.dto.CudaItem; // 记得保留之前定义的这个DTO
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "server_node", autoResultMap = true) // 必须开启 autoResultMap 以支持 JSON 转换
@Schema(description = "服务器资产配置表")
public class ServerNode extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主机名(唯一标识)")
    @TableField(value = "server_name")
    private String serverName;

    @Schema(description = "内网IP")
    @TableField(value = "server_ip")
    private String serverIp;

    @Schema(description = "公网IP")
    @TableField(value = "public_ip")
    private String publicIp;

    @Schema(description = "SSH端口")
    @TableField(value = "ssh_port")
    private Integer sshPort;

    @Schema(description = "物理位置")
    @TableField(value = "location")
    private String location;

    @Schema(description = "显卡型号")
    @TableField(value = "gpu_model")
    private String gpuModel;

    @Schema(description = "端口范围")
    @TableField(value = "port_range")
    private String portRange;

    @Schema(description = "公共Anaconda: 1是 0否")
    @TableField(value = "has_anaconda")
    private Integer hasAnaconda;

    @Schema(description = "NCCL信息")
    @TableField(value = "nccl_info")
    private String ncclInfo;

    @Schema(description = "CUDA版本列表(JSON)")
    @TableField(value = "cuda_info", typeHandler = JacksonTypeHandler.class)
    private List<CudaItem> cudaInfo;

    @Schema(description = "状态: 1在线 0离线")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "最后心跳时间")
    @TableField(value = "last_heartbeat")
    private LocalDateTime lastHeartbeat;
}