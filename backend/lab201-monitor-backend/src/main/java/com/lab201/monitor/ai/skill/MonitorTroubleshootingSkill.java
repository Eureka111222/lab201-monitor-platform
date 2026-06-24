package com.lab201.monitor.ai.skill;

import com.lab201.monitor.ai.agent.AgentToolOrchestrator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class MonitorTroubleshootingSkill extends AbstractAgentSkill {

    @Override
    public String name() {
        return "monitor-troubleshooting";
    }

    @Override
    public String displayName() {
        return "监控故障排查 Skill";
    }

    @Override
    public String description() {
        return "用于排查 Agent 上报、服务器离线、RabbitMQ、Redis 缓存、监控数据不刷新等监控链路问题。";
    }

    @Override
    public int priority() {
        return 85;
    }

    @Override
    public Set<String> allowedTools() {
        return new LinkedHashSet<>(List.of("list_lab_servers", "get_gpu_status", "query_gpu_trend", "search_lab_documents"));
    }

    @Override
    public boolean matches(AgentSkillContext context) {
        return containsAny(context.normalizedQuery(),
                "agent", "上报", "不刷新", "没数据", "离线", "心跳", "rabbitmq", "redis",
                "队列", "缓存", "监控异常", "监控故障", "消费", "同步", "异步");
    }

    @Override
    public List<AgentToolOrchestrator.PlanStep> plan(AgentSkillContext context) {
        List<AgentToolOrchestrator.PlanStep> steps = new ArrayList<>();
        steps.add(step(
                "list_lab_servers",
                "查询服务器在线状态",
                "监控故障排查需要先确认服务器心跳和在线状态。"
        ));
        if (context.hasMatchedServer()) {
            steps.add(step(
                    "get_gpu_status",
                    "查询 GPU 最新上报",
                    "如果指定了服务器，需要确认该服务器是否仍有最新 GPU 上报数据。",
                    "serverName", context.matchedServerName()
            ));
        }
        steps.add(step(
                "search_lab_documents",
                "检索监控链路文档",
                "需要结合 RabbitMQ、Redis、Agent 部署和监控链路说明进行排查。",
                "keyword", context.query()
        ));
        return steps;
    }

    @Override
    public String outputGuide() {
        return "按 Agent 上报、HTTP 接入、RabbitMQ 队列、消费者、MySQL 落库、Redis 缓存、前端刷新链路逐层排查。";
    }
}
