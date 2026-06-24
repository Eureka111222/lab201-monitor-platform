package com.lab201.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab201.monitor.entity.AiConversationSummary;
import com.lab201.monitor.entity.AiMemory;
import com.lab201.monitor.entity.AiMessage;
import com.lab201.monitor.service.AiLongTermMemoryService;
import com.lab201.monitor.service.IAiConversationSummaryService;
import com.lab201.monitor.service.IAiMemoryService;
import com.lab201.monitor.service.IAiMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AiLongTermMemoryServiceImpl implements AiLongTermMemoryService {

    private static final String TYPE_PREFERENCE = "PREFERENCE";
    private static final String TYPE_ENTITY = "ENTITY";
    private static final String TYPE_TASK = "TASK";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final int LONG_MEMORY_LIMIT = 12;
    private static final int SUMMARY_KEEP_RECENT = 10;
    private static final int SUMMARY_MAX_LENGTH = 4000;
    private static final Pattern SERVER_ALIAS_PATTERN = Pattern.compile("\\b([A-Z]\\d+)\\b\\s*(?:对应|是|就是|代表)\\s*\\b(server[A-Z]?\\d+)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern SERVER_PATTERN = Pattern.compile("\\bserver[A-Z]?\\d+\\b", Pattern.CASE_INSENSITIVE);

    private final IAiMemoryService aiMemoryService;
    private final IAiConversationSummaryService summaryService;
    private final IAiMessageService aiMessageService;

    public AiLongTermMemoryServiceImpl(IAiMemoryService aiMemoryService,
                                       IAiConversationSummaryService summaryService,
                                       IAiMessageService aiMessageService) {
        this.aiMemoryService = aiMemoryService;
        this.summaryService = summaryService;
        this.aiMessageService = aiMessageService;
    }

    @Override
    public String buildLongTermMemoryContext(Long userId) {
        if (userId == null) {
            return "暂无长期记忆。";
        }
        List<AiMemory> memories = aiMemoryService.list(new LambdaQueryWrapper<AiMemory>()
                .eq(AiMemory::getUserId, userId)
                .orderByDesc(AiMemory::getUpdateTime)
                .last("limit " + LONG_MEMORY_LIMIT));
        if (memories.isEmpty()) {
            return "暂无长期记忆。";
        }
        return memories.stream()
                .map(memory -> "- [" + memory.getMemoryType() + "] " + memory.getMemoryKey() + ": " + memory.getMemoryValue())
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String buildConversationSummaryContext(Long userId, String conversationId) {
        if (userId == null || !StringUtils.hasText(conversationId)) {
            return "暂无会话摘要。";
        }
        AiConversationSummary summary = summaryService.getOne(new LambdaQueryWrapper<AiConversationSummary>()
                .eq(AiConversationSummary::getUserId, userId)
                .eq(AiConversationSummary::getConversationId, conversationId.trim())
                .last("limit 1"));
        return summary == null || !StringUtils.hasText(summary.getSummaryText())
                ? "暂无会话摘要。"
                : summary.getSummaryText();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rememberAfterTurn(Long userId, String conversationId, String userQuery, String assistantAnswer) {
        if (userId == null || !StringUtils.hasText(userQuery)) {
            return;
        }
        rememberPreference(userId, conversationId, userQuery);
        rememberEntityAlias(userId, conversationId, userQuery);
        rememberTask(userId, conversationId, userQuery, assistantAnswer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshConversationSummary(Long userId, String conversationId) {
        if (userId == null || !StringUtils.hasText(conversationId)) {
            return;
        }
        List<AiMessage> messages = aiMessageService.list(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getUserId, userId)
                .eq(AiMessage::getConversationId, conversationId.trim())
                .eq(AiMessage::getStatus, STATUS_SUCCESS)
                .orderByAsc(AiMessage::getCreateTime)
                .orderByAsc(AiMessage::getId));
        if (messages.size() <= SUMMARY_KEEP_RECENT) {
            return;
        }

        int summarizeCount = messages.size() - SUMMARY_KEEP_RECENT;
        String summaryText = messages.subList(0, summarizeCount).stream()
                .map(message -> roleLabel(message.getRole()) + ": " + abbreviate(message.getContent(), 300))
                .collect(Collectors.joining("\n"));
        summaryText = abbreviate(summaryText, SUMMARY_MAX_LENGTH);

        AiConversationSummary existing = summaryService.getOne(new LambdaQueryWrapper<AiConversationSummary>()
                .eq(AiConversationSummary::getUserId, userId)
                .eq(AiConversationSummary::getConversationId, conversationId.trim())
                .last("limit 1"));
        if (existing == null) {
            AiConversationSummary summary = new AiConversationSummary();
            summary.setConversationId(conversationId.trim());
            summary.setUserId(userId);
            summary.setSummaryText(summaryText);
            summary.setSummarizedMessageCount(summarizeCount);
            summaryService.save(summary);
            return;
        }
        existing.setSummaryText(summaryText);
        existing.setSummarizedMessageCount(summarizeCount);
        summaryService.updateById(existing);
    }

    private void rememberPreference(Long userId, String conversationId, String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        if (text.contains("中文") || text.contains("用中文") || normalized.contains("chinese")) {
            upsertMemory(userId, TYPE_PREFERENCE, "answer_language", "用户偏好使用中文回答", conversationId, 90);
        }
        if (text.contains("详细") || text.contains("讲清楚") || text.contains("一步步")) {
            upsertMemory(userId, TYPE_PREFERENCE, "answer_style", "用户偏好详细、分步骤解释", conversationId, 80);
        }
    }

    private void rememberEntityAlias(Long userId, String conversationId, String text) {
        Matcher matcher = SERVER_ALIAS_PATTERN.matcher(text);
        while (matcher.find()) {
            String alias = matcher.group(1).toUpperCase(Locale.ROOT);
            String serverName = matcher.group(2);
            upsertMemory(userId, TYPE_ENTITY, "server_alias:" + alias, alias + " 对应 " + serverName, conversationId, 95);
        }
    }

    private void rememberTask(Long userId, String conversationId, String userQuery, String assistantAnswer) {
        if (!(userQuery.contains("分析") || userQuery.contains("慢") || userQuery.contains("异常") || userQuery.contains("排查"))) {
            return;
        }
        Matcher matcher = SERVER_PATTERN.matcher(userQuery + " " + assistantAnswer);
        String target = matcher.find() ? matcher.group() : "未知服务器";
        upsertMemory(userId, TYPE_TASK, "last_diagnosis:" + target.toLowerCase(Locale.ROOT),
                "用户曾分析 " + target + " 的性能或 GPU 使用异常", conversationId, 70);
    }

    private void upsertMemory(Long userId, String type, String key, String value, String conversationId, int confidence) {
        AiMemory existing = aiMemoryService.getOne(new LambdaQueryWrapper<AiMemory>()
                .eq(AiMemory::getUserId, userId)
                .eq(AiMemory::getMemoryType, type)
                .eq(AiMemory::getMemoryKey, key)
                .last("limit 1"));
        if (existing == null) {
            AiMemory memory = new AiMemory();
            memory.setUserId(userId);
            memory.setMemoryType(type);
            memory.setMemoryKey(key);
            memory.setMemoryValue(value);
            memory.setSourceConversationId(conversationId);
            memory.setConfidence(confidence);
            aiMemoryService.save(memory);
            return;
        }
        existing.setMemoryValue(value);
        existing.setSourceConversationId(conversationId);
        existing.setConfidence(Math.max(existing.getConfidence() == null ? 0 : existing.getConfidence(), confidence));
        aiMemoryService.updateById(existing);
    }

    private String roleLabel(String role) {
        return "assistant".equalsIgnoreCase(role) ? "助手" : "用户";
    }

    private String abbreviate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength) + "...";
    }
}
