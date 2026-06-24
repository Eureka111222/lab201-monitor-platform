package com.lab201.monitor.ai.rag.rerank;

import com.lab201.monitor.common.config.Lab201AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DashScopeRagRerankProvider implements RagRerankProvider {

    private final Lab201AiProperties properties;
    private final RestClient restClient;

    public DashScopeRagRerankProvider(Lab201AiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public String providerName() {
        return "dashscope";
    }

    @Override
    public List<Double> rerank(String query, List<RagRerankDocument> documents) {
        Lab201AiProperties.DashScope dashScope = properties.getRerank().getDashscope();
        if (!StringUtils.hasText(dashScope.getApiKey())) {
            throw new IllegalStateException("DashScope rerank api-key is empty");
        }
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", dashScope.getModel());
        requestBody.put("query", query);
        requestBody.put("documents", documents.stream()
                .map(document -> abbreviate(document.title() + "\n" + document.text(), 6000))
                .toList());
        requestBody.put("top_n", Math.min(dashScope.getTopN(), documents.size()));

        long startNanos = System.nanoTime();
        Map<?, ?> response = restClient.post()
                .uri(dashScope.getEndpoint())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + dashScope.getApiKey())
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        List<Double> scores = parseScores(response, documents.size());
        long costMs = (System.nanoTime() - startNanos) / 1_000_000;
        log.info(">>> [RAG_RERANK] provider=dashscope model={} docs={} timeout={}s costMs={}",
                dashScope.getModel(), documents.size(), Duration.ofSeconds(dashScope.getTimeoutSeconds()).toSeconds(), costMs);
        return scores;
    }

    private List<Double> parseScores(Map<?, ?> response, int size) {
        List<Double> scores = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            scores.add(0.0);
        }
        Object resultsObject = response == null ? null : response.get("results");
        if (!(resultsObject instanceof List<?> results)) {
            throw new IllegalStateException("DashScope rerank response has no results");
        }
        for (Object item : results) {
            if (!(item instanceof Map<?, ?> result)) {
                continue;
            }
            Integer index = parseInteger(result.get("index"));
            Double score = parseDouble(result.get("relevance_score"));
            if (score == null) {
                score = parseDouble(result.get("score"));
            }
            if (index != null && index >= 0 && index < size && score != null) {
                scores.set(index, round(score));
            }
        }

        double fallback = scores.stream().max(Comparator.naturalOrder()).orElse(0.0);
        if (fallback <= 0) {
            throw new IllegalStateException("DashScope rerank returned empty scores");
        }
        return scores;
    }

    private Integer parseInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? null : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double parseDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return value == null ? null : Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String abbreviate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
