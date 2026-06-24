package com.lab201.monitor.ai.document;

import com.lab201.monitor.common.config.Lab201AiProperties;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RagDocumentChunkService {

    private final Lab201AiProperties properties;

    public RagDocumentChunkService(Lab201AiProperties properties) {
        this.properties = properties;
    }

    public List<Document> split(String text, Map<String, Object> metadata) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        Document sourceDocument = Document.builder()
                .text(normalizeText(text))
                .metadata(metadata == null ? Map.of() : new LinkedHashMap<>(metadata))
                .build();
        TokenTextSplitter splitter = new TokenTextSplitter(
                properties.getDocumentParse().getChunkSize(),
                properties.getDocumentParse().getMinChunkSizeChars(),
                properties.getDocumentParse().getMinChunkLengthToEmbed(),
                properties.getDocumentParse().getMaxNumChunks(),
                true
        );
        List<Document> tokenChunks = splitter.apply(List.of(sourceDocument));
        return hardSplitChunks(tokenChunks);
    }

    private List<Document> hardSplitChunks(List<Document> documents) {
        int maxChunkChars = Math.max(400, properties.getDocumentParse().getMaxChunkChars());
        List<Document> safeDocuments = new ArrayList<>();
        for (Document document : documents == null ? List.<Document>of() : documents) {
            String text = normalizeText(document.getText());
            if (!StringUtils.hasText(text)) {
                continue;
            }
            if (text.length() <= maxChunkChars) {
                safeDocuments.add(copyDocument(document, text));
                continue;
            }
            for (String part : splitByChars(text, maxChunkChars)) {
                if (StringUtils.hasText(part)) {
                    safeDocuments.add(copyDocument(document, part));
                }
            }
        }
        return safeDocuments;
    }

    private List<String> splitByChars(String text, int maxChunkChars) {
        List<String> parts = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChunkChars, text.length());
            int splitAt = findSoftBoundary(text, start, end);
            if (splitAt <= start) {
                splitAt = end;
            }
            parts.add(text.substring(start, splitAt).trim());
            start = splitAt;
        }
        return parts;
    }

    private int findSoftBoundary(String text, int start, int end) {
        int searchStart = Math.max(start, end - 300);
        for (int i = end - 1; i >= searchStart; i--) {
            char ch = text.charAt(i);
            if (ch == '\n' || ch == '。' || ch == '！' || ch == '？' || ch == '.' || ch == '!' || ch == '?') {
                return i + 1;
            }
        }
        return end;
    }

    private Document copyDocument(Document source, String text) {
        return Document.builder()
                .text(text)
                .metadata(new LinkedHashMap<>(source.getMetadata()))
                .build();
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('\u0000', ' ')
                .replaceAll("[\\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n{4,}", "\n\n")
                .replaceAll(" {2,}", " ")
                .trim();
    }
}
