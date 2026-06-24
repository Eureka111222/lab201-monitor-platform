package com.lab201.monitor.ai.skill;

import com.lab201.monitor.ai.agent.AgentToolOrchestrator;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public abstract class AbstractAgentSkill implements AgentSkill {

    protected boolean containsAny(String normalizedText, String... keywords) {
        if (!StringUtils.hasText(normalizedText)) {
            return false;
        }
        for (String keyword : keywords) {
            if (normalizedText.contains(normalize(keyword))) {
                return true;
            }
        }
        return false;
    }

    protected String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    protected AgentToolOrchestrator.PlanStep step(String toolName,
                                                  String displayName,
                                                  String reason,
                                                  Object... keyValues) {
        Map<String, Object> args = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            args.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return new AgentToolOrchestrator.PlanStep(toolName, displayName, reason, args);
    }
}
