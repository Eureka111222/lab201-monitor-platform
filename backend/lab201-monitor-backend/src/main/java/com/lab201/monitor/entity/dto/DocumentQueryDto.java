package com.lab201.monitor.entity.dto;

import lombok.Data;

@Data
public class DocumentQueryDto {
    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
    private Integer viewScope; // 0:私有 1:公开
}