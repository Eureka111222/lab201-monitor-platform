package com.lab201.monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab201.monitor.common.result.Result;
import com.lab201.monitor.entity.GpuMetricLog;
import com.lab201.monitor.entity.dto.GpuReportDto;
import com.lab201.monitor.service.IGpuMetricLogService;
import com.lab201.monitor.service.IServerNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GPU监控日志控制器
 * 负责接收 Agent 数据上报和前端图表查询
 */
@Tag(name = "01. GPU监控管理")
@RestController
@RequestMapping("/api/v1/gpu")
public class GpuMetricLogController {

    @Autowired
    private IGpuMetricLogService gpuService;

    @Autowired
    private IServerNodeService serverNodeService;

    @Operation(summary = "接收Agent上报的数据 (核心)")
    @PostMapping("/report")
    public Result<String> reportGpuData(@RequestBody GpuReportDto reportDto) {
        if (reportDto == null || reportDto.getLogs() == null) {
            return Result.fail(400, "上报数据为空");
        }

        String serverName = reportDto.getServerName();

        List<GpuMetricLog> logs = reportDto.getLogs();
        for (GpuMetricLog log :
                logs) {
            log.setServerName(serverName);
        }

        // 1. 保存日志
        gpuService.saveBatch(logs);

        // 2. 更新心跳
        serverNodeService.updateHeartbeat(reportDto.getServerName());
        return Result.success("上报成功");
    }

    @Operation(summary = "分页查询GPU日志")
    @GetMapping("/list")
    public Result<Page<GpuMetricLog>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "服务器名") @RequestParam(required = false) String serverName) {

        Page<GpuMetricLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<GpuMetricLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(serverName), GpuMetricLog::getServerName, serverName);
        wrapper.orderByDesc(GpuMetricLog::getCreateTime);

        // 这里需要在 Service 层实现带条件的查询，暂演示基础分页
        // LambdaQueryWrapper<GpuMetricLog> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(StringUtils.isNotBlank(serverName), GpuMetricLog::getServerName, serverName)
        //        .orderByDesc(GpuMetricLog::getCreateTime);
        return Result.success(gpuService.page(pageParam,wrapper));
    }

    //
    //@Operation(summary = "获取某台服务器最新的GPU状态")
    //@GetMapping("/latest/{serverName}")
    //public Result<List<GpuMetricLog>> getLatestStatus(@PathVariable String serverName) {
    //    // 业务逻辑：查询该机器 create_time 最近的一批数据
    //    // 建议在 Service 层实现 selectList logic
    //    return Result.success(null); // 占位
    //}
}