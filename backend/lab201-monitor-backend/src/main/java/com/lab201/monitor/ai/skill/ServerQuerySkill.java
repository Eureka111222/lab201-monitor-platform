package com.lab201.monitor.ai.skill;

import com.lab201.monitor.ai.agent.AgentToolOrchestrator;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class ServerQuerySkill extends AbstractAgentSkill {

    @Override
    public String name() {
        return "server-query";
    }

    @Override
    public String displayName() {
        return "服务器查询 Skill";
    }

    @Override
    public String description() {
        return "用于查询 Lab201 服务器列表、在线状态、IP、显卡型号、位置和基础资产信息。";
    }

    @Override
    public int priority() {
        return 90;
    }

    @Override
    public Set<String> allowedTools() {
        return new LinkedHashSet<>(List.of("list_lab_servers", "get_gpu_status"));
    }

    @Override
    public boolean matches(AgentSkillContext context) {
        return containsAny(context.normalizedQuery(),
                "所有服务器", "全部服务器", "服务器列表", "服务器情况", "服务器状态",
                "在线服务器", "离线服务器", "有哪些服务器", "现在服务器", "目前服务器",
                "ip", "内网", "公网", "位置", "显卡型号", "资产");
    }

    @Override
    public List<AgentToolOrchestrator.PlanStep> plan(AgentSkillContext context) {
        return List.of(step(
                "list_lab_servers",
                "查询服务器列表",
                "服务器查询类问题需要先获取服务器清单、在线状态、IP、显卡型号和位置。"
        ));
    }

    @Override
    public String outputGuide() {
        return "以服务器资产信息为主，优先说明在线/离线、IP、显卡型号和位置；不要编造列表中没有的端口或配置。";
    }
}
