package com.lab201.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lab201.monitor.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ai_agent_step")
@Schema(description = "AI Agent 执行步骤表")
public class AiAgentStep extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Agent 任务业务ID")
    @TableField(value = "task_id")
    private String taskId;

    @Schema(description = "步骤序号")
    @TableField(value = "step_order")
    private Integer stepOrder;

    @Schema(description = "工具名称")
    @TableField(value = "tool_name")
    private String toolName;

    @Schema(description = "工具展示名称")
    @TableField(value = "display_name")
    private String displayName;

    @Schema(description = "工具入参JSON")
    @TableField(value = "tool_args")
    private String toolArgs;

    @Schema(description = "工具结果")
    @TableField(value = "tool_result")
    private String toolResult;

    @Schema(description = "步骤状态：RUNNING/SUCCESS/FAILED")
    @TableField(value = "status")
    private String status;

    @Schema(description = "步骤耗时毫秒")
    @TableField(value = "cost_ms")
    private Long costMs;

    @Schema(description = "错误信息")
    @TableField(value = "error_message")
    private String errorMessage;
}
