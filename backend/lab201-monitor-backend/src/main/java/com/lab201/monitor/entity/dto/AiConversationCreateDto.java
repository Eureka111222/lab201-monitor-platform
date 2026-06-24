package com.lab201.monitor.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建 AI 会话请求")
public class AiConversationCreateDto {

    @Schema(description = "会话标题，不传则使用默认标题")
    private String title;
}
