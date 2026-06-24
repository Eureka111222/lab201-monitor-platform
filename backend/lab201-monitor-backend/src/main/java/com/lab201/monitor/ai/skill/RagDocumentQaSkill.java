package com.lab201.monitor.ai.skill;

import com.lab201.monitor.ai.agent.AgentToolOrchestrator;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class RagDocumentQaSkill extends AbstractAgentSkill {

    @Override
    public String name() {
        return "rag-document-qa";
    }

    @Override
    public String displayName() {
        return "知识库问答 Skill";
    }

    @Override
    public String description() {
        return "用于回答部署流程、CUDA、端口、报错、实验室文档说明等需要知识库依据的问题。";
    }

    @Override
    public int priority() {
        return 70;
    }

    @Override
    public Set<String> allowedTools() {
        return new LinkedHashSet<>(List.of("search_lab_documents"));
    }

    @Override
    public boolean matches(AgentSkillContext context) {
        return containsAny(context.normalizedQuery(),
                "文档", "知识库", "教程", "说明", "部署", "安装", "配置", "流程", "报错",
                "怎么", "如何", "端口", "cuda", "pytorch", "环境", "手册", "指南");
    }

    @Override
    public List<AgentToolOrchestrator.PlanStep> plan(AgentSkillContext context) {
        return List.of(step(
                "search_lab_documents",
                "检索知识库文档",
                "知识库问答需要先检索当前用户有权限访问且已加入 RAG 的文档。",
                "keyword", context.query()
        ));
    }

    @Override
    public String outputGuide() {
        return "回答必须优先基于 RAG 上下文和文档工具结果，并在有引用来源时提示用户可核验来源；没有依据时说明未检索到。";
    }
}
