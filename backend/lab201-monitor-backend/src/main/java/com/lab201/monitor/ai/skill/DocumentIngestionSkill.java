package com.lab201.monitor.ai.skill;

import com.lab201.monitor.ai.agent.AgentToolOrchestrator;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class DocumentIngestionSkill extends AbstractAgentSkill {

    @Override
    public String name() {
        return "document-ingestion";
    }

    @Override
    public String displayName() {
        return "文档解析入库 Skill";
    }

    @Override
    public String description() {
        return "用于解释或排查文档上传、解析、切片、向量化、Milvus 入库和 RAG 权限问题。";
    }

    @Override
    public int priority() {
        return 65;
    }

    @Override
    public Set<String> allowedTools() {
        return new LinkedHashSet<>(List.of("search_lab_documents"));
    }

    @Override
    public boolean matches(AgentSkillContext context) {
        return containsAny(context.normalizedQuery(),
                "上传", "解析", "入库", "切片", "向量化", "embedding", "milvus", "zilliz",
                "parse", "rag权限", "文档失败", "解析失败", "一直转圈", "ocr", "tika", "libreoffice");
    }

    @Override
    public List<AgentToolOrchestrator.PlanStep> plan(AgentSkillContext context) {
        return List.of(step(
                "search_lab_documents",
                "检索文档处理说明",
                "文档解析入库类问题需要先查找知识库中的解析、切片、向量化和权限说明。",
                "keyword", context.query()
        ));
    }

    @Override
    public String outputGuide() {
        return "按上传存储、文本解析、清洗切片、Embedding、向量库入库、权限过滤、失败排查的顺序回答。";
    }
}
