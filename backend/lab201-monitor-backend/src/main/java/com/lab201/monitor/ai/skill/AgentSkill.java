package com.lab201.monitor.ai.skill;

import com.lab201.monitor.ai.agent.AgentToolOrchestrator;

import java.util.List;
import java.util.Set;

public interface AgentSkill {

    String name();

    String displayName();

    String description();

    int priority();

    Set<String> allowedTools();

    boolean matches(AgentSkillContext context);

    List<AgentToolOrchestrator.PlanStep> plan(AgentSkillContext context);

    String outputGuide();
}
