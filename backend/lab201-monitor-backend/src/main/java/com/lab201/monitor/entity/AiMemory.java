package com.lab201.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lab201.monitor.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ai_memory")
@Schema(description = "AI 长期记忆表")
public class AiMemory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "所属用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "记忆类型：PREFERENCE/ENTITY/TASK")
    @TableField(value = "memory_type")
    private String memoryType;

    @Schema(description = "记忆键，例如 language/server_alias")
    @TableField(value = "memory_key")
    private String memoryKey;

    @Schema(description = "记忆内容")
    @TableField(value = "memory_value")
    private String memoryValue;

    @Schema(description = "来源会话ID")
    @TableField(value = "source_conversation_id")
    private String sourceConversationId;

    @Schema(description = "置信度 0-100")
    @TableField(value = "confidence")
    private Integer confidence;
}
