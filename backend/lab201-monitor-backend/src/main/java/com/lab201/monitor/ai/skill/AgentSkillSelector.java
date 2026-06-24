package com.lab201.monitor.ai.skill;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AgentSkillSelector {

    private final List<AgentSkill> skills;

    public AgentSkillSelector(List<AgentSkill> skills) {
        this.skills = skills.stream()
                .sorted(Comparator.comparingInt(AgentSkill::priority).reversed())
                .toList();
    }

    public List<AgentSkill> select(AgentSkillContext context) {
        return skills.stream()
                .filter(skill -> skill.matches(context))
                .toList();
    }

    public String buildSkillGuide(List<AgentSkill> selectedSkills) {
        if (selectedSkills == null || selectedSkills.isEmpty()) {
            return "";
        }
        return selectedSkills.stream()
                .map(skill -> "Skill: " + skill.displayName()
                        + "\n- Purpose: " + skill.description()
                        + "\n- Allowed tools: " + String.join(", ", skill.allowedTools())
                        + "\n- Output guide: " + skill.outputGuide())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("\n\n"));
    }
}
