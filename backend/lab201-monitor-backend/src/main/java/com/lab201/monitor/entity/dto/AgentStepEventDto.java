package com.lab201.monitor.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentStepEventDto {

    private String taskId;

    private Integer stepOrder;

    private String toolName;

    private String displayName;

    private String status;

    private Long costMs;
}
