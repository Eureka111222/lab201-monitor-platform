package com.lab201.monitor.ai.document;

import com.lab201.monitor.common.config.Lab201AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(30)
public class OcrDocumentTextExtractor implements DocumentTextExtractor {

    private static final List<String> IMAGE_EXTENSIONS = List.of("png", "jpg", "jpeg", "bmp", "tif", "tiff");

    private final Lab201AiProperties properties;

    public OcrDocumentTextExtractor(Lab201AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean supports(String fileExt, String contentType) {
        String ext = normalizeExt(fileExt);
        return properties.getDocumentParse().isOcrEnabled() && IMAGE_EXTENSIONS.contains(ext);
    }

    @Override
    public ExtractResult extract(InputStream inputStream, String fileName) {
        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("lab201-ocr-");
            String ext = normalizeExt(fileName);
            Path imageFile = workDir.resolve(UUID.randomUUID().toString().replace("-", "") + "." + ext);
            Files.copy(inputStream, imageFile);

            ProcessBuilder builder = new ProcessBuilder(
                    properties.getDocumentParse().getTesseractCommand(),
                    imageFile.toString(),
                    "stdout",
                    "-l",
                    properties.getDocumentParse().getOcrLanguage()
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();
            boolean finished = process.waitFor(properties.getDocumentParse().getTimeoutSeconds(), TimeUnit.SECONDS);
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("OCR timeout");
            }
            if (process.exitValue() != 0) {
                throw new IllegalStateException("OCR failed, exitCode=" + process.exitValue() + ", output=" + output);
            }
            return new ExtractResult(output, "tesseract");
        } catch (IOException ex) {
            throw new IllegalStateException("Tesseract command is unavailable or failed to read image", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OCR interrupted", ex);
        } finally {
            if (workDir != null) {
                try {
                    FileSystemUtils.deleteRecursively(workDir);
                } catch (IOException ex) {
                    log.warn("Failed to clean OCR temp directory, dir={}", workDir, ex);
                }
            }
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
}
