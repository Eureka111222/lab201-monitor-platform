package com.lab201.monitor.ai.document;

import com.lab201.monitor.common.config.Lab201AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(20)
public class LibreOfficeDocumentTextExtractor implements DocumentTextExtractor {

    private static final List<String> OFFICE_EXTENSIONS = List.of(
            "doc", "docx", "ppt", "pptx", "xls", "xlsx", "rtf", "odt", "ods", "odp"
    );

    private final Lab201AiProperties properties;

    public LibreOfficeDocumentTextExtractor(Lab201AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean supports(String fileExt, String contentType) {
        String ext = normalizeExt(fileExt);
        return properties.getDocumentParse().isLibreOfficeEnabled() && OFFICE_EXTENSIONS.contains(ext);
    }

    @Override
    public ExtractResult extract(InputStream inputStream, String fileName) {
        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("lab201-libreoffice-");
            String ext = normalizeExt(fileName);
            String baseName = UUID.randomUUID().toString().replace("-", "");
            Path sourceFile = workDir.resolve(baseName + (StringUtils.hasText(ext) ? "." + ext : ""));
            Files.copy(inputStream, sourceFile);

            ProcessBuilder builder = new ProcessBuilder(
                    properties.getDocumentParse().getLibreOfficeCommand(),
                    "--headless",
                    "--convert-to",
                    "txt:Text",
                    "--outdir",
                    workDir.toString(),
                    sourceFile.toString()
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();
            boolean finished = process.waitFor(properties.getDocumentParse().getTimeoutSeconds(), TimeUnit.SECONDS);
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("LibreOffice parse timeout after "
                        + Duration.ofSeconds(properties.getDocumentParse().getTimeoutSeconds()).toSeconds() + "s");
            }
            if (process.exitValue() != 0) {
                throw new IllegalStateException("LibreOffice parse failed, exitCode="
                        + process.exitValue() + ", output=" + output);
            }

            Path txtFile = workDir.resolve(baseName + ".txt");
            if (!Files.exists(txtFile)) {
                throw new IllegalStateException("LibreOffice did not generate txt output, output=" + output);
            }
            return new ExtractResult(readText(txtFile), "libreoffice");
        } catch (IOException ex) {
            throw new IllegalStateException("LibreOffice command is unavailable or failed to read document", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("LibreOffice parse interrupted", ex);
        } finally {
            if (workDir != null) {
                try {
                    FileSystemUtils.deleteRecursively(workDir);
                } catch (IOException ex) {
                    log.warn("Failed to clean LibreOffice temp directory, dir={}", workDir, ex);
                }
            }
        }
    }

    private String readText(Path txtFile) throws IOException {
        byte[] bytes = Files.readAllBytes(txtFile);
        String text = new String(bytes, StandardCharsets.UTF_8);
        if (text.indexOf('\uFFFD') >= 0) {
            text = new String(bytes, Charset.forName("GBK"));
        }
        return text;
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
