package com.lab201.monitor.ai.rag.rerank;

import com.lab201.monitor.common.config.Lab201AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RagRerankService {

    private final Lab201AiProperties properties;
    private final Map<String, RagRerankProvider> providers;
    private final LocalRagRerankProvider localProvider;

    public RagRerankService(Lab201AiProperties properties,
                            List<RagRerankProvider> providers,
                            LocalRagRerankProvider localProvider) {
        this.properties = properties;
        this.localProvider = localProvider;
        this.providers = new LinkedHashMap<>();
        for (RagRerankProvider provider : providers) {
            this.providers.put(provider.providerName(), provider);
        }
    }

    public List<Double> rerank(String query, List<RagRerankDocument> documents) {
        String providerName = properties.getRerank().getProvider();
        RagRerankProvider provider = providers.getOrDefault(providerName, localProvider);
        try {
            return provider.rerank(query, documents);
        } catch (Exception ex) {
            if (provider == localProvider || !properties.getRerank().isFallbackToLocal()) {
                throw ex;
            }
            log.warn(">>> [RAG_RERANK] provider={} status=failed fallback=local error={}",
                    provider.providerName(), ex.getMessage(), ex);
            return localProvider.rerank(query, documents);
        }
    }
}
