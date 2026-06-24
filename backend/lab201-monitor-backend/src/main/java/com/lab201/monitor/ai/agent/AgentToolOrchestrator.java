package com.lab201.monitor.ai.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab201.monitor.ai.skill.AgentSkill;
import com.lab201.monitor.ai.skill.AgentSkillContext;
import com.lab201.monitor.ai.skill.AgentSkillSelector;
import com.lab201.monitor.ai.tool.DocumentTools;
import com.lab201.monitor.ai.tool.GpuTools;
import com.lab201.monitor.ai.tool.ServerTools;
import com.lab201.monitor.entity.ServerNode;
import com.lab201.monitor.service.MonitorCacheService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AgentToolOrchestrator {

    private static final int DEFAULT_TREND_DAYS = 7;
    private static final Pattern DAYS_PATTERN = Pattern.compile("(?:最近|近来|近)?\\s*(\\d+)\\s*(?:天|日)");

    private final ServerTools serverTools;
    private final GpuTools gpuTools;
    private final DocumentTools documentTools;
    private final MonitorCacheService monitorCacheService;
    private final ObjectMapper objectMapper;
    private final AgentSkillSelector agentSkillSelector;

    public AgentToolOrchestrator(ServerTools serverTools,
                                 GpuTools gpuTools,
                                 DocumentTools documentTools,
                                 MonitorCacheService monitorCacheService,
                                 ObjectMapper objectMapper,
                                 AgentSkillSelector agentSkillSelector) {
        this.serverTools = serverTools;
        this.gpuTools = gpuTools;
        this.documentTools = documentTools;
        this.monitorCacheService = monitorCacheService;
        this.objectMapper = objectMapper;
        this.agentSkillSelector = agentSkillSelector;
    }

    public AgentPlan plan(String query, String role) {
        if (!StringUtils.hasText(query)) {
            return new AgentPlan("回答用户问题", List.of());
        }

        String normalizedQuery = normalize(query);
        String matchedServerName = findMatchedServerName(query, normalizedQuery);
        AgentSkillContext context = new AgentSkillContext(
                query,
                normalizedQuery,
                role,
                matchedServerName,
                extractDays(query)
        );
        List<AgentSkill> selectedSkills = agentSkillSelector.select(context);
        if (selectedSkills.isEmpty()) {
            return new AgentPlan("回答用户问题：" + query, List.of());
        }

        List<PlanStep> steps = new ArrayList<>();
        Set<String> stepKeys = new LinkedHashSet<>();
        for (AgentSkill skill : selectedSkills) {
            for (PlanStep step : skill.plan(context)) {
                if (!skill.allowedTools().contains(step.toolName())) {
                    continue;
                }
                String stepKey = step.toolName() + "|" + step.args();
                if (stepKeys.add(stepKey)) {
                    steps.add(step);
                }
            }
        }

        return new AgentPlan(
                buildGoal(query, matchedServerName),
                steps,
                selectedSkills.stream().map(this::toSkillSummary).toList(),
                agentSkillSelector.buildSkillGuide(selectedSkills)
        );
    }

    public ToolPlanResult execute(AgentPlan plan) {
        if (plan == null || plan.steps() == null || plan.steps().isEmpty()) {
            return new ToolPlanResult(List.of(), plan == null ? "" : plan.skillGuide());
        }

        List<String> observations = new ArrayList<>();
        for (PlanStep step : plan.steps()) {
            observations.add(executeStep(step));
        }
        return new ToolPlanResult(observations, plan.skillGuide());
    }

    public String formatPlan(AgentPlan plan) {
        try {
            return objectMapper.writeValueAsString(plan == null ? new AgentPlan("回答用户问题", List.of()) : plan);
        } catch (JsonProcessingException ex) {
            return "{\"goal\":\"回答用户问题\",\"steps\":[]}";
        }
    }

    private String executeStep(PlanStep step) {
        String toolName = step.toolName();
        Map<String, Object> args = step.args() == null ? Map.of() : step.args();
        String result;
        if ("list_lab_servers".equals(toolName)) {
            result = serverTools.listLabServers();
        } else if ("get_gpu_status".equals(toolName)) {
            result = gpuTools.getGpuStatus(asString(args.get("serverName")));
        } else if ("query_gpu_trend".equals(toolName)) {
            result = gpuTools.queryGpuTrend(asString(args.get("serverName")), asInteger(args.get("days")));
        } else if ("search_lab_documents".equals(toolName)) {
            result = documentTools.searchLabDocuments(asString(args.get("keyword")));
        } else {
            result = "未识别的工具：" + toolName;
        }
        return formatObservation(step.displayName(), result);
    }

    private String findMatchedServerName(String rawQuery, String normalizedQuery) {
        List<ServerNode> servers = monitorCacheService.getServerNodeList();
        for (ServerNode server : servers) {
            if (matchesServer(rawQuery, normalizedQuery, server, true)) {
                return server.getServerName();
            }
        }
        for (ServerNode server : servers) {
            if (matchesServer(rawQuery, normalizedQuery, server, false)) {
                return server.getServerName();
            }
        }
        return null;
    }

    private boolean matchesServer(String rawQuery, String normalizedQuery, ServerNode server, boolean exactOnly) {
        if (server == null) {
            return false;
        }

        String serverName = server.getServerName();
        if (StringUtils.hasText(serverName) && normalizedQuery.contains(normalize(serverName))) {
            return true;
        }
        if (StringUtils.hasText(server.getServerIp()) && rawQuery.contains(server.getServerIp())) {
            return true;
        }
        if (StringUtils.hasText(server.getPublicIp()) && rawQuery.contains(server.getPublicIp())) {
            return true;
        }
        if (exactOnly || !StringUtils.hasText(serverName)) {
            return false;
        }

        for (String token : serverNameTokens(serverName)) {
            if (token.length() >= 2 && normalizedQuery.contains(normalize(token))) {
                return true;
            }
        }
        return false;
    }

    private Set<String> serverNameTokens(String serverName) {
        Set<String> tokens = new LinkedHashSet<>();
        for (String token : serverName.split("[._\\-\\s]+")) {
            if (StringUtils.hasText(token)) {
                tokens.add(token);
                if (token.toLowerCase(Locale.ROOT).startsWith("server") && token.length() > 6) {
                    tokens.add(token.substring(6));
                }
            }
        }
        return tokens;
    }

    private Integer extractDays(String query) {
        Matcher matcher = DAYS_PATTERN.matcher(query);
        if (!matcher.find()) {
            return DEFAULT_TREND_DAYS;
        }
        try {
            return Math.max(1, Math.min(Integer.parseInt(matcher.group(1)), 30));
        } catch (NumberFormatException ex) {
            return DEFAULT_TREND_DAYS;
        }
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    private String formatObservation(String title, String content) {
        return "【" + title + "】\n" + (StringUtils.hasText(content) ? content : "无结果");
    }

    private String buildGoal(String query, String matchedServerName) {
        if (StringUtils.hasText(matchedServerName)) {
            return "围绕服务器 " + matchedServerName + " 回答用户问题：" + query;
        }
        return "回答用户问题：" + query;
    }

    private SelectedSkill toSkillSummary(AgentSkill skill) {
        return new SelectedSkill(
                skill.name(),
                skill.displayName(),
                skill.description(),
                new ArrayList<>(skill.allowedTools())
        );
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? null : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public record AgentPlan(String goal,
                            List<PlanStep> steps,
                            List<SelectedSkill> selectedSkills,
                            String skillGuide) {

        public AgentPlan(String goal, List<PlanStep> steps) {
            this(goal, steps, List.of(), "");
        }
    }

    public record SelectedSkill(String name,
                                String displayName,
                                String description,
                                List<String> allowedTools) {
    }

    public record PlanStep(String toolName, String displayName, String reason, Map<String, Object> args) {
    }

    public record ToolPlanResult(List<String> observations, String skillGuide) {

        public static ToolPlanResult empty() {
            return new ToolPlanResult(List.of(), "");
        }

        public String asPromptContext() {
            List<String> parts = new ArrayList<>();
            if (StringUtils.hasText(skillGuide)) {
                parts.add("Agent Skill 指令：\n" + skillGuide);
            }
            if (observations != null && !observations.isEmpty()) {
                parts.add("工具执行结果：\n" + String.join("\n\n", observations));
            }
            return parts.isEmpty() ? "暂无工具执行结果。" : String.join("\n\n", parts);
        }
    }
}
