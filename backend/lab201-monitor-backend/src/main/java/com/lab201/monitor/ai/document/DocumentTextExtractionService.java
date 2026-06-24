package com.lab201.monitor.ai.document;

import com.lab201.monitor.common.config.Lab201AiProperties;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class DocumentTextExtractionService {

    private final List<DocumentTextExtractor> extractors;
    private final Lab201AiProperties properties;
    private final ExecutorService executorService;

    public DocumentTextExtractionService(List<DocumentTextExtractor> extractors,
                                         Lab201AiProperties properties) {
        this.extractors = extractors.stream()
                .sorted(Comparator.comparingInt(extractor -> extractor.getClass().getAnnotation(org.springframework.core.annotation.Order.class) == null
                        ? Integer.MAX_VALUE
                        : extractor.getClass().getAnnotation(org.springframework.core.annotation.Order.class).value()))
                .toList();
        this.properties = properties;
        this.executorService = Executors.newCachedThreadPool(new DocumentParseThreadFactory());
    }

    public ExtractResult extract(InputStream inputStream, String fileName, String fileExt, String contentType) {
        byte[] bytes = readAllBytes(inputStream);
        String normalizedExt = normalizeExt(StringUtils.hasText(fileExt) ? fileExt : fileName);
        List<DocumentTextExtractor> supportedExtractors = extractors.stream()
                .filter(extractor -> extractor.supports(normalizedExt, contentType))
                .toList();
        if (supportedExtractors.isEmpty()) {
            throw new IllegalStateException("No document extractor supports file extension: " + normalizedExt);
        }

        RuntimeException lastError = null;
        for (DocumentTextExtractor extractor : supportedExtractors) {
            String extractorName = extractor.getClass().getSimpleName();
            try {
                long startNanos = System.nanoTime();
                ExtractResult result = runWithTimeout(() ->
                        extractor.extract(new ByteArrayInputStream(bytes), fileName));
                long costMs = (System.nanoTime() - startNanos) / 1_000_000;
                if (result != null && StringUtils.hasText(result.text())) {
                    log.info(">>> [DOC_PARSE] parser={} status=success fileName={} chars={} costMs={}",
                            result.parserType(), fileName, result.text().length(), costMs);
                    return result;
                }
                lastError = new IllegalStateException("Parser returned empty text: " + extractorName);
                log.warn(">>> [DOC_PARSE] parser={} status=empty fileName={} costMs={}",
                        extractorName, fileName, costMs);
            } catch (RuntimeException ex) {
                lastError = ex;
                log.warn(">>> [DOC_PARSE] parser={} status=failed fileName={} error={}",
                        extractorName, fileName, ex.getMessage(), ex);
            }
        }

        throw new IllegalStateException("All document extractors failed for " + fileName,
                lastError == null ? null : lastError);
    }

    private ExtractResult runWithTimeout(Callable<ExtractResult> task) {
        Future<ExtractResult> future = executorService.submit(task);
        try {
            return future.get(properties.getDocumentParse().getTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw new IllegalStateException("Document parse timeout after "
                    + properties.getDocumentParse().getTimeoutSeconds() + " seconds", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Document parse interrupted", ex);
        } catch (Exception ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Document parse failed", cause);
        }
    }

    private byte[] readAllBytes(InputStream inputStream) {
        try {
            return StreamUtils.copyToByteArray(inputStream);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read document stream", ex);
        }
    }

    private String normalizeExt(String fileNameOrExt) {
        if (!StringUtils.hasText(fileNameOrExt)) {
            return "";
        }
        String value = fileNameOrExt.trim();
        int dotIndex = value.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex + 1 < value.length()) {
            value = value.substring(dotIndex + 1);
        }
        return value.toLowerCase(Locale.ROOT);
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }

    private static class DocumentParseThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "lab201-doc-parse-" + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
