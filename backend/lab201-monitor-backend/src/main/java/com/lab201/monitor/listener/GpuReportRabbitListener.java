package com.lab201.monitor.listener;

import com.lab201.monitor.entity.dto.GpuReportDto;
import com.lab201.monitor.service.GpuReportProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GpuReportRabbitListener {

    @Autowired
    private GpuReportProcessingService gpuReportProcessingService;

    @RabbitListener(queues = "${monitor.integration.amqp.gpu-report-queue}")
    public void consume(GpuReportDto reportDto) {
        log.debug("收到 GPU 上报消息, server={}", reportDto == null ? null : reportDto.getServerName());
        gpuReportProcessingService.process(reportDto);
    }
}
