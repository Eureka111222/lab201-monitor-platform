package com.lab201.monitor.ai.skill;

import com.lab201.monitor.ai.agent.AgentToolOrchestrator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class GpuDiagnosisSkill extends AbstractAgentSkill {

    @Override
    public String name() {
        return "gpu-diagnosis";
    }

    @Override
    public String displayName() {
        return "GPU 诊断 Skill";
    }

    @Override
    public String description() {
        return "用于分析某台服务器 GPU 实时状态、近几天趋势、显存占用、性能变慢和异常原因。";
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public Set<String> allowedTools() {
        return new LinkedHashSet<>(List.of("get_gpu_status", "query_gpu_trend", "search_lab_documents"));
    }

    @Override
    public boolean matches(AgentSkillContext context) {
        return context.hasMatchedServer() && containsAny(context.normalizedQuery(),
                "gpu", "显卡", "显存", "温度", "功耗", "风扇", "实时", "当前", "现在", "状态", "情况",
                "最近", "近来", "近几天", "历史", "趋势", "利用率", "占用", "负载", "统计",
                "为什么慢", "变慢", "很慢", "卡", "异常", "原因", "分析", "诊断", "排查", "性能", "慢");
    }

    @Override
    public List<AgentToolOrchestrator.PlanStep> plan(AgentSkillContext context) {
        List<AgentToolOrchestrator.PlanStep> steps = new ArrayList<>();
        boolean diagnosis = containsAny(context.normalizedQuery(),
                "为什么慢", "变慢", "很慢", "卡", "异常", "原因", "分析", "诊断", "排查", "性能", "慢");
        boolean trend = diagnosis || containsAny(context.normalizedQuery(),
                "最近", "近来", "近几天", "历史", "趋势", "利用率", "占用", "负载", "统计");

        if (diagnosis || containsAny(context.normalizedQuery(),
                "gpu", "显卡", "显存", "温度", "功耗", "风扇", "实时", "当前", "现在", "状态", "情况")) {
            steps.add(step(
                    "get_gpu_status",
                    "查询 GPU 实时状态",
                    "GPU 诊断需要先确认当前利用率、显存、温度、功耗和占用用户。",
                    "serverName", context.matchedServerName()
            ));
        }
        if (trend) {
            steps.add(step(
                    "query_gpu_trend",
                    "查询 GPU 历史趋势",
                    "用户问题涉及最近使用情况或性能诊断，需要分析聚合趋势判断是否长期高负载。",
                    "serverName", context.matchedServerName(),
                    "days", context.days()
            ));
        }
        if (diagnosis) {
            steps.add(step(
                    "search_lab_documents",
                    "检索服务器维护文档",
                    "性能诊断需要结合服务器说明、维护记录或使用规范补充背景。",
                    "keyword", context.matchedServerName()
            ));
        }
        return steps;
    }

    @Override
    public String outputGuide() {
        return "先给诊断结论，再分别列出实时状态、历史趋势、可能原因和建议操作；没有工具证据时必须明确说明。";
    }
}
