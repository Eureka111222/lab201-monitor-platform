package com.lab201.monitor.ai.document;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Order(10)
public class TikaDocumentTextExtractor implements DocumentTextExtractor {

    @Override
    public boolean supports(String fileExt, String contentType) {
        return true;
    }

    @Override
    public ExtractResult extract(InputStream inputStream, String fileName) {
        TikaDocumentReader reader = new TikaDocumentReader(new InputStreamResource(inputStream));
        List<Document> documents = reader.get();
        String text = documents.stream()
                .map(Document::getText)
                .filter(StringUtils::hasText)
                .map(Objects::toString)
                .collect(Collectors.joining("\n\n"));
        return new ExtractResult(text, "tika");
    }
}
