package com.lab201.monitor.ai.rag.rerank;

public record RagRerankDocument(String title,
                                String text,
                                double vectorScore,
                                double keywordScore) {
}
