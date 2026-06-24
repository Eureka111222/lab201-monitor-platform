package com.lab201.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lab201.monitor.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ai_agent_task")
@Schema(description = "AI Agent 任务表")
public class AiAgentTask extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Agent 任务业务ID")
    @TableField(value = "task_id")
    private String taskId;

    @Schema(description = "会话业务ID")
    @TableField(value = "conversation_id")
    private String conversationId;

    @Schema(description = "用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @Schema(description = "用户问题")
    @TableField(value = "query_text")
    private String queryText;

    @Schema(description = "任务状态：RUNNING/SUCCESS/FAILED")
    @TableField(value = "status")
    private String status;

    @Schema(description = "模型提供商")
    @TableField(value = "model_provider")
    private String modelProvider;

    @Schema(description = "模型名称")
    @TableField(value = "model_name")
    private String modelName;

    @Schema(description = "最终回答")
    @TableField(value = "final_answer")
    private String finalAnswer;

    @Schema(description = "引用来源JSON")
    @TableField(value = "citations_json")
    private String citationsJson;

    @Schema(description = "错误信息")
    @TableField(value = "error_message")
    private String errorMessage;

    @Schema(description = "任务耗时毫秒")
    @TableField(value = "cost_ms")
    private Long costMs;
}
