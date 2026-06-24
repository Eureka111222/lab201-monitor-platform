package com.lab201.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lab201.monitor.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ai_message")
@Schema(description = "AI 会话消息表")
public class AiMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话业务ID")
    @TableField(value = "conversation_id")
    private String conversationId;

    @Schema(description = "所属用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "消息角色：user/assistant/system/tool")
    @TableField(value = "role")
    private String role;

    @Schema(description = "消息内容")
    @TableField(value = "content")
    private String content;

    @Schema(description = "模型提供商")
    @TableField(value = "model_provider")
    private String modelProvider;

    @Schema(description = "模型名称")
    @TableField(value = "model_name")
    private String modelName;

    @Schema(description = "预估或实际 token 数")
    @TableField(value = "token_count")
    private Integer tokenCount;

    @Schema(description = "本次模型调用耗时毫秒")
    @TableField(value = "latency_ms")
    private Long latencyMs;

    @Schema(description = "消息状态：SUCCESS/STREAMING/FAILED")
    @TableField(value = "status")
    private String status;

    @Schema(description = "错误信息")
    @TableField(value = "error_message")
    private String errorMessage;
}
