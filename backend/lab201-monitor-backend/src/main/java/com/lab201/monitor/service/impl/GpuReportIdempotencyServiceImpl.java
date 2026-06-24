package com.lab201.monitor.service.impl;

import com.lab201.monitor.common.config.MonitorIntegrationProperties;
import com.lab201.monitor.common.constant.MonitorCacheConstants;
import com.lab201.monitor.entity.dto.GpuReportDto;
import com.lab201.monitor.service.IGpuReportIdempotencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class GpuReportIdempotencyServiceImpl implements IGpuReportIdempotencyService {

    private static final DateTimeFormatter REPORT_ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final DateTimeFormatter BUCKET_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_DONE = "DONE";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MonitorIntegrationProperties properties;

    @Override
    public String ensureReportId(GpuReportDto reportDto) {
        if (reportDto == null) {
            return nextReportId();
        }

        if (StringUtils.hasText(reportDto.getReportId())) {
            String reportId = reportDto.getReportId().trim();
            reportDto.setReportId(reportId);
            return reportId;
        }

        String reportId = nextReportId();
        reportDto.setReportId(reportId);
        return reportId;
    }

    @Override
    public boolean tryAcquire(String reportId, String serverName) {
        if (!StringUtils.hasText(reportId)) {
            return false;
        }

        String normalizedReportId = reportId.trim();
        String bucketKey = resolveBucketKey(normalizedReportId);
        String existingState = (String) stringRedisTemplate.opsForHash().get(bucketKey, normalizedReportId);
        if (StringUtils.hasText(existingState)) {
            if (isDone(existingState)) {
                log.info("Skip duplicated GPU report, reportId={}, server={}", normalizedReportId, serverName);
                return false;
            }
            if (!isProcessingExpired(existingState)) {
                log.info("GPU report is already being processed, reportId={}, server={}", normalizedReportId, serverName);
                return false;
            }
        }

        boolean acquired = Boolean.TRUE.equals(stringRedisTemplate.opsForHash().putIfAbsent(
                bucketKey,
                normalizedReportId,
                buildProcessingValue(serverName)
        ));

        if (!acquired && isProcessingExpired((String) stringRedisTemplate.opsForHash().get(bucketKey, normalizedReportId))) {
            stringRedisTemplate.opsForHash().put(bucketKey, normalizedReportId, buildProcessingValue(serverName));
            acquired = true;
        }

        if (acquired) {
            stringRedisTemplate.expire(bucketKey, Duration.ofDays(properties.getCache().getGpuReportIdempotencyDays()));
        }
        return acquired;
    }

    @Override
    public void markProcessed(String reportId, String serverName) {
        if (!StringUtils.hasText(reportId)) {
            return;
        }

        String normalizedReportId = reportId.trim();
        String bucketKey = resolveBucketKey(normalizedReportId);
        stringRedisTemplate.opsForHash().put(bucketKey, normalizedReportId, buildDoneValue(serverName));
        stringRedisTemplate.expire(bucketKey, Duration.ofDays(properties.getCache().getGpuReportIdempotencyDays()));
    }

    @Override
    public void release(String reportId) {
        if (!StringUtils.hasText(reportId)) {
            return;
        }

        String normalizedReportId = reportId.trim();
        String bucketKey = resolveBucketKey(normalizedReportId);
        Object state = stringRedisTemplate.opsForHash().get(bucketKey, normalizedReportId);
        if (state instanceof String stateText && stateText.startsWith(STATUS_PROCESSING + "|")) {
            stringRedisTemplate.opsForHash().delete(bucketKey, normalizedReportId);
        }
    }

    private String nextReportId() {
        return REPORT_ID_FORMATTER.format(LocalDateTime.now()) + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String resolveBucketKey(String reportId) {
        String bucket = reportId.length() >= 8 && reportId.substring(0, 8).chars().allMatch(Character::isDigit)
                ? reportId.substring(0, 8)
                : BUCKET_FORMATTER.format(LocalDateTime.now());
        return MonitorCacheConstants.gpuReportIdempotencyKey(bucket);
    }

    private String buildProcessingValue(String serverName) {
        long expiresAt = System.currentTimeMillis() + properties.getCache().getGpuReportProcessingExpireSeconds() * 1000;
        return STATUS_PROCESSING + "|" + expiresAt + "|" + normalizeServer(serverName);
    }

    private String buildDoneValue(String serverName) {
        return STATUS_DONE + "|" + System.currentTimeMillis() + "|" + normalizeServer(serverName);
    }

    private boolean isDone(String state) {
        return state.startsWith(STATUS_DONE + "|");
    }

    private boolean isProcessingExpired(String state) {
        if (!StringUtils.hasText(state) || !state.startsWith(STATUS_PROCESSING + "|")) {
            return false;
        }

        List<String> parts = List.of(state.split("\\|", 3));
        if (parts.size() < 2) {
            return false;
        }

        try {
            long expiresAt = Long.parseLong(parts.get(1));
            return expiresAt < System.currentTimeMillis();
        } catch (NumberFormatException ex) {
            log.warn("Invalid GPU report processing state, state={}", state, ex);
            return false;
        }
    }

    private String normalizeServer(String serverName) {
        return StringUtils.hasText(serverName) ? serverName.trim() : "";
    }
}
