package com.lab201.monitor.ai.rag.rerank;

import java.util.List;

public interface RagRerankProvider {

    String providerName();

    List<Double> rerank(String query, List<RagRerankDocument> documents);
}
