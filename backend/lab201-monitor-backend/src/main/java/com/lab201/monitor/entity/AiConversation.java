package com.lab201.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lab201.monitor.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ai_conversation")
@Schema(description = "AI 会话表")
public class AiConversation extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话业务ID，前端和流式接口使用")
    @TableField(value = "conversation_id")
    private String conversationId;

    @Schema(description = "所属用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "会话标题")
    @TableField(value = "title")
    private String title;

    @Schema(description = "模型提供商，例如 ollama/openai")
    @TableField(value = "model_provider")
    private String modelProvider;

    @Schema(description = "模型名称，例如 qwen-plus/qwen2.5:7b")
    @TableField(value = "model_name")
    private String modelName;
}
