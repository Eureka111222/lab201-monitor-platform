package com.lab201.monitor.ai.skill;

import org.springframework.util.StringUtils;

public record AgentSkillContext(String query,
                                String normalizedQuery,
                                String role,
                                String matchedServerName,
                                Integer days) {

    public boolean hasMatchedServer() {
        return StringUtils.hasText(matchedServerName);
    }
}
