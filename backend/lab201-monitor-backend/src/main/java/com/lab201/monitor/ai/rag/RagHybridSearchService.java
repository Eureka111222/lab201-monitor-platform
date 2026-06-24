package com.lab201.monitor.ai.rag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab201.monitor.ai.rag.rerank.RagRerankDocument;
import com.lab201.monitor.ai.rag.rerank.RagRerankService;
import com.lab201.monitor.entity.SysDocument;
import com.lab201.monitor.entity.SysDocumentChunk;
import com.lab201.monitor.entity.dto.RagCitationDto;
import com.lab201.monitor.service.ISysDocumentChunkService;
import com.lab201.monitor.service.ISysDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RagHybridSearchService {

    private static final int VECTOR_TOP_K = 20;
    private static final int KEYWORD_TOP_K = 20;
    private static final int FINAL_TOP_K = 5;
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z0-9._:/#%+\\-]+");
    private static final List<String> CHINESE_KEYWORDS = List.of(
            "装机", "安装", "部署", "端口", "驱动", "显卡", "显存", "温度", "报错", "环境", "配置", "服务器"
    );

    private final VectorStore vectorStore;
    private final ISysDocumentService documentService;
    private final ISysDocumentChunkService chunkService;
    private final RagRerankService ragRerankService;

    public RagHybridSearchService(VectorStore vectorStore,
                                  ISysDocumentService documentService,
                                  ISysDocumentChunkService chunkService,
                                  RagRerankService ragRerankService) {
        this.vectorStore = vectorStore;
        this.documentService = documentService;
        this.chunkService = chunkService;
        this.ragRerankService = ragRerankService;
    }

    public RagRetrievalResult search(String query, Long userId, String role) {
        long startNanos = System.nanoTime();
        List<String> queryVariants = rewriteQueries(query);
        Map<String, Candidate> candidates = new LinkedHashMap<>();

        for (String variant : queryVariants) {
            recallByVector(variant, candidates);
        }
        recallByKeyword(queryVariants, candidates);

        List<Candidate> readableCandidates = filterReadable(candidates.values().stream().toList(), userId, role);
        rerank(query, readableCandidates);
        List<Candidate> topCandidates = readableCandidates.stream()
                .sorted(Comparator.comparing(Candidate::rerankScore).reversed())
                .limit(FINAL_TOP_K)
                .toList();

        List<Document> documents = topCandidates.stream().map(Candidate::toDocument).toList();
        long costMs = (System.nanoTime() - startNanos) / 1_000_000;
        log.info(">>> [RAG_HYBRID] query={} variants={} vectorTopK={} keywordTopK={} merged={} readable={} final={} costMs={}",
                abbreviate(query, 120), queryVariants.size(), VECTOR_TOP_K, KEYWORD_TOP_K,
                candidates.size(), readableCandidates.size(), documents.size(), costMs);
        return new RagRetrievalResult(
                documents,
                buildContext(documents),
                buildCitations(documents),
                queryVariants
        );
    }

    public void saveDocumentChunks(SysDocument document, List<Document> splitDocuments) {
        if (document == null || document.getId() == null || splitDocuments == null || splitDocuments.isEmpty()) {
            return;
        }

        chunkService.remove(new LambdaQueryWrapper<SysDocumentChunk>()
                .eq(SysDocumentChunk::getDocId, document.getId()));
        List<SysDocumentChunk> chunks = new ArrayList<>(splitDocuments.size());
        for (int i = 0; i < splitDocuments.size(); i++) {
            Document splitDoc = splitDocuments.get(i);
            String chunkText = splitDoc.getText();
            if (!StringUtils.hasText(chunkText)) {
                continue;
            }
            SysDocumentChunk chunk = new SysDocumentChunk();
            chunk.setDocId(document.getId());
            chunk.setChunkIndex(i);
            chunk.setTitle(document.getTitle());
            chunk.setChunkContent(chunkText);
            chunk.setParentContent(buildParentContent(splitDocuments, i));
            chunk.setCharCount(chunkText.length());
            chunks.add(chunk);
        }
        if (!chunks.isEmpty()) {
            chunkService.saveBatch(chunks);
        }
    }

    private void recallByVector(String query, Map<String, Candidate> candidates) {
        try {
            List<Document> documents = vectorStore.similaritySearch(
                    SearchRequest.builder().query(query).topK(VECTOR_TOP_K).build()
            );
            for (Document document : documents == null ? List.<Document>of() : documents) {
                Long docId = parseLong(document.getMetadata().get("doc_id"));
                Integer chunkIndex = parseInteger(document.getMetadata().get("chunk_index"));
                String title = String.valueOf(document.getMetadata().getOrDefault("title", "未知文档"));
                String text = enrichParentContent(docId, chunkIndex, document.getText());
                Candidate candidate = candidates.computeIfAbsent(candidateKey(docId, chunkIndex, text),
                        key -> new Candidate(docId, chunkIndex, title, text));
                candidate.vectorScore = Math.max(candidate.vectorScore, safeScore(document.getScore()));
                candidate.sources.add("vector");
            }
        } catch (Exception ex) {
            log.error(">>> [RAG_VECTOR] status=failed query={} error={}", abbreviate(query, 120), ex.getMessage(), ex);
        }
    }

    private void recallByKeyword(List<String> queryVariants, Map<String, Candidate> candidates) {
        List<String> terms = tokenize(String.join(" ", queryVariants)).stream()
                .limit(12)
                .toList();
        if (terms.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<SysDocumentChunk> wrapper = new LambdaQueryWrapper<SysDocumentChunk>()
                .and(w -> {
                    for (int i = 0; i < terms.size(); i++) {
                        String term = terms.get(i);
                        if (i == 0) {
                            w.like(SysDocumentChunk::getChunkContent, term)
                                    .or()
                                    .like(SysDocumentChunk::getTitle, term);
                        } else {
                            w.or(q -> q.like(SysDocumentChunk::getChunkContent, term)
                                    .or()
                                    .like(SysDocumentChunk::getTitle, term));
                        }
                    }
                })
                .last("limit 120");
        List<SysDocumentChunk> chunks = chunkService.list(wrapper);
        List<SysDocumentChunk> rankedChunks = chunks.stream()
                .sorted(Comparator.comparingDouble((SysDocumentChunk chunk) ->
                        bm25Score(terms, chunk.getChunkContent(), chunks)).reversed())
                .limit(KEYWORD_TOP_K)
                .toList();

        for (SysDocumentChunk chunk : rankedChunks) {
            Candidate candidate = candidates.computeIfAbsent(candidateKey(chunk.getDocId(), chunk.getChunkIndex(), chunk.getChunkContent()),
                    key -> new Candidate(chunk.getDocId(), chunk.getChunkIndex(), chunk.getTitle(),
                            StringUtils.hasText(chunk.getParentContent()) ? chunk.getParentContent() : chunk.getChunkContent()));
            candidate.keywordScore = Math.max(candidate.keywordScore, bm25Score(terms, chunk.getChunkContent(), chunks));
            candidate.sources.add("keyword");
        }
    }

    private List<Candidate> filterReadable(List<Candidate> candidates, Long userId, String role) {
        List<Long> docIds = candidates.stream()
                .map(Candidate::docId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, SysDocument> documentMap = docIds.isEmpty() ? Map.of() : documentService.listByIds(docIds).stream()
                .collect(Collectors.toMap(SysDocument::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

        return candidates.stream()
                .filter(candidate -> isReadableRagDocument(documentMap.get(candidate.docId()), userId, role))
                .peek(candidate -> {
                    SysDocument document = documentMap.get(candidate.docId());
                    if (document != null && StringUtils.hasText(document.getTitle())) {
                        candidate.title = document.getTitle();
                    }
                })
                .toList();
    }

    private void rerank(String query, List<Candidate> candidates) {
        List<RagRerankDocument> documents = candidates.stream()
                .map(candidate -> new RagRerankDocument(
                        candidate.title,
                        candidate.text,
                        candidate.vectorScore,
                        candidate.keywordScore))
                .toList();
        List<Double> scores = ragRerankService.rerank(query, documents);
        for (int i = 0; i < candidates.size(); i++) {
            double score = i < scores.size() && scores.get(i) != null ? scores.get(i) : 0.0;
            candidates.get(i).rerankScore = round(score);
        }
    }

    private List<String> rewriteQueries(String query) {
        Set<String> variants = new LinkedHashSet<>();
        if (StringUtils.hasText(query)) {
            variants.add(query.trim());
        }
        String expanded = expandServerAlias(query);
        if (StringUtils.hasText(expanded)) {
            variants.add(expanded);
        }
        if (query != null && query.contains("装机")) {
            variants.add(expanded + " 装机流程 Ubuntu CUDA 驱动 环境部署");
        }
        if (query != null && (query.contains("端口") || query.matches(".*\\b\\d{4,5}\\b.*"))) {
            variants.add(expanded + " 端口 服务 访问地址");
        }
        return variants.stream().filter(StringUtils::hasText).limit(4).toList();
    }

    private String expandServerAlias(String query) {
        if (!StringUtils.hasText(query)) {
            return "";
        }
        Matcher matcher = Pattern.compile("\\b([A-Z])\\s*(\\d+)\\b", Pattern.CASE_INSENSITIVE).matcher(query);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String alias = matcher.group(1).toUpperCase(Locale.ROOT) + matcher.group(2);
            matcher.appendReplacement(buffer, alias + " server" + alias);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
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

    private double bm25Score(List<String> queryTerms, String content, List<SysDocumentChunk> corpus) {
        if (!StringUtils.hasText(content) || queryTerms.isEmpty() || corpus.isEmpty()) {
            return 0.0;
        }
        List<String> docTerms = tokenize(content);
        if (docTerms.isEmpty()) {
            return 0.0;
        }
        double avgDocLength = corpus.stream()
                .map(SysDocumentChunk::getChunkContent)
                .map(this::tokenize)
                .mapToInt(List::size)
                .average()
                .orElse(docTerms.size());
        double k1 = 1.5;
        double b = 0.75;
        double score = 0.0;
        for (String term : queryTerms) {
            long tf = docTerms.stream().filter(term::equals).count();
            if (tf <= 0) {
                continue;
            }
            long df = corpus.stream().filter(chunk -> tokenize(chunk.getChunkContent()).contains(term)).count();
            double idf = Math.log(1 + (corpus.size() - df + 0.5) / (df + 0.5));
            double denominator = tf + k1 * (1 - b + b * docTerms.size() / Math.max(avgDocLength, 1.0));
            score += idf * (tf * (k1 + 1)) / denominator;
        }
        return round(score);
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

    private boolean isReadableRagDocument(SysDocument document, Long userId, String role) {
        if (document == null) {
            return false;
        }
        if (!Integer.valueOf(1).equals(document.getIsRag()) || !Integer.valueOf(2).equals(document.getParseStatus())) {
            return false;
        }
        if ("admin".equals(role)) {
            return true;
        }
        Integer ragScope = document.getRagScope() == null ? document.getViewScope() : document.getRagScope();
        return Integer.valueOf(1).equals(ragScope) || (userId != null && userId.equals(document.getUploaderId()));
    }

    private String enrichParentContent(Long docId, Integer chunkIndex, String fallback) {
        if (docId == null || chunkIndex == null) {
            return fallback;
        }
        SysDocumentChunk chunk = chunkService.getOne(new LambdaQueryWrapper<SysDocumentChunk>()
                .eq(SysDocumentChunk::getDocId, docId)
                .eq(SysDocumentChunk::getChunkIndex, chunkIndex)
                .last("limit 1"));
        if (chunk == null) {
            return fallback;
        }
        return StringUtils.hasText(chunk.getParentContent()) ? chunk.getParentContent() : chunk.getChunkContent();
    }

    private String buildParentContent(List<Document> splitDocuments, int index) {
        List<String> parts = new ArrayList<>();
        if (index > 0) {
            parts.add(splitDocuments.get(index - 1).getText());
        }
        parts.add(splitDocuments.get(index).getText());
        if (index + 1 < splitDocuments.size()) {
            parts.add(splitDocuments.get(index + 1).getText());
        }
        return parts.stream().filter(StringUtils::hasText).collect(Collectors.joining("\n\n"));
    }

    private String buildContext(List<Document> documents) {
        return documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
    }

    private List<RagCitationDto> buildCitations(List<Document> documents) {
        return documents.stream().map(document -> {
            Map<String, Object> metadata = document.getMetadata();
            RagCitationDto citation = new RagCitationDto();
            citation.setDocId(parseLong(metadata.get("doc_id")));
            citation.setTitle(String.valueOf(metadata.getOrDefault("title", "未知文档")));
            citation.setScore(document.getScore());
            citation.setSnippet(abbreviate(document.getText(), 180));
            return citation;
        }).toList();
    }

    private String candidateKey(Long docId, Integer chunkIndex, String text) {
        if (docId != null && chunkIndex != null) {
            return docId + ":" + chunkIndex;
        }
        return (docId == null ? "unknown" : docId) + ":" + Math.abs(Objects.toString(text, "").hashCode());
    }

    private double safeScore(Double score) {
        return score == null ? 0.0 : Math.max(score, 0.0);
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private Long parseLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return value == null ? null : Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
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

    private String abbreviate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength) + "...";
    }

    private static class Candidate {
        private final Long docId;
        private final Integer chunkIndex;
        private String title;
        private final String text;
        private final Set<String> sources = new LinkedHashSet<>();
        private double vectorScore;
        private double keywordScore;
        private double rerankScore;

        private Candidate(Long docId, Integer chunkIndex, String title, String text) {
            this.docId = docId;
            this.chunkIndex = chunkIndex;
            this.title = title;
            this.text = text;
        }

        private Long docId() {
            return docId;
        }

        private double vectorScore() {
            return vectorScore;
        }

        private double keywordScore() {
            return keywordScore;
        }

        private double rerankScore() {
            return rerankScore;
        }

        private Document toDocument() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("doc_id", docId);
            metadata.put("chunk_index", chunkIndex);
            metadata.put("title", title);
            metadata.put("retrieval_sources", String.join(",", sources));
            metadata.put("vector_score", vectorScore);
            metadata.put("keyword_score", keywordScore);
            metadata.put("rerank_score", rerankScore);
            return Document.builder()
                    .text(text)
                    .metadata(metadata)
                    .score(rerankScore)
                    .build();
        }
    }

    public record RagRetrievalResult(List<Document> documents,
                                     String context,
                                     List<RagCitationDto> citations,
                                     List<String> queryVariants) {
    }
}
