package com.lab201.monitor.service.impl;

import com.lab201.monitor.entity.GpuMetricLog;
import com.lab201.monitor.mapper.GpuMetricLogMapper;
import com.lab201.monitor.service.IGpuMetricLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * GPU监控日志表 服务实现类
 * </p>
 *
 * @author lab201
 * @since 2025-12-22
 */
@Service
public class GpuMetricLogServiceImpl extends ServiceImpl<GpuMetricLogMapper, GpuMetricLog> implements IGpuMetricLogService {

}
