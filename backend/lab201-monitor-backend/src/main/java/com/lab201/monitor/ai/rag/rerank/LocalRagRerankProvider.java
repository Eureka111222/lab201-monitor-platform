package com.lab201.monitor.ai.rag.rerank;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LocalRagRerankProvider implements RagRerankProvider {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z0-9._:/#%+\\-]+");
    private static final List<String> CHINESE_KEYWORDS = List.of(
            "装机", "安装", "部署", "端口", "驱动", "显卡", "显存", "温度", "报错", "环境", "配置", "服务器"
    );

    @Override
    public String providerName() {
        return "local";
    }

    @Override
    public List<Double> rerank(String query, List<RagRerankDocument> documents) {
        List<String> queryTerms = tokenize(query);
        double maxVector = documents.stream().mapToDouble(RagRerankDocument::vectorScore).max().orElse(0.0);
        double maxKeyword = documents.stream().mapToDouble(RagRerankDocument::keywordScore).max().orElse(0.0);
        List<Double> scores = new ArrayList<>(documents.size());
        for (RagRerankDocument document : documents) {
            double normalizedVector = maxVector <= 0 ? 0 : document.vectorScore() / maxVector;
            double normalizedKeyword = maxKeyword <= 0 ? 0 : document.keywordScore() / maxKeyword;
            double exactBoost = exactBoost(queryTerms, document.title() + " " + document.text());
            scores.add(round(0.50 * normalizedVector + 0.40 * normalizedKeyword + 0.10 * exactBoost));
        }
        return scores;
    }

    private List<String> tokenize(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String token = matcher.group().trim();
            if (token.length() >= 2) {
                tokens.add(token);
            }
        }
        for (String keyword : CHINESE_KEYWORDS) {
            if (text.contains(keyword)) {
                tokens.add(keyword);
            }
        }
        return tokens.stream().distinct().toList();
    }

    private double exactBoost(List<String> queryTerms, String text) {
        if (!StringUtils.hasText(text) || queryTerms.isEmpty()) {
            return 0.0;
        }
        String normalizedText = text.toLowerCase(Locale.ROOT);
        long exactHits = queryTerms.stream()
                .filter(term -> term.length() >= 3)
                .filter(normalizedText::contains)
                .count();
        return Math.min(1.0, exactHits / 3.0);
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
