package com.lab201.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lab201.monitor.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ai_conversation_summary")
@Schema(description = "AI 会话摘要记忆表")
public class AiConversationSummary extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话业务ID")
    @TableField(value = "conversation_id")
    private String conversationId;

    @Schema(description = "所属用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "摘要文本")
    @TableField(value = "summary_text")
    private String summaryText;

    @Schema(description = "已摘要消息数量")
    @TableField(value = "summarized_message_count")
    private Integer summarizedMessageCount;
}
