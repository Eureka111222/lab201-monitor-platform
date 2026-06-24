package com.lab201.monitor.service.impl;

import com.lab201.monitor.common.config.MonitorIntegrationProperties;
import com.lab201.monitor.entity.dto.GpuReportDto;
import com.lab201.monitor.service.GpuReportDispatchService;
import com.lab201.monitor.service.GpuReportProcessingService;
import com.lab201.monitor.service.IGpuReportIdempotencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GpuReportDispatchServiceImpl implements GpuReportDispatchService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MonitorIntegrationProperties properties;

    @Autowired
    private GpuReportProcessingService gpuReportProcessingService;

    @Autowired
    private IGpuReportIdempotencyService gpuReportIdempotencyService;

    @Override
    public void dispatch(GpuReportDto reportDto) {
        String reportId = gpuReportIdempotencyService.ensureReportId(reportDto);
        try {
            rabbitTemplate.convertAndSend(
                    properties.getAmqp().getGpuReportExchange(),
                    properties.getAmqp().getGpuReportRoutingKey(),
                    reportDto,
                    message -> {
                        message.getMessageProperties().setMessageId(reportId);
                        return message;
                    }
            );
        } catch (AmqpException ex) {
            log.error("GPU report enqueue failed, fallback to synchronous processing, server={}",
                    reportDto == null ? null : reportDto.getServerName(), ex);
            gpuReportProcessingService.process(reportDto);
        }
    }
}
