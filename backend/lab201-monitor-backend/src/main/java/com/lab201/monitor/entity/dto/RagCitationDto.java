package com.lab201.monitor.entity.dto;

import lombok.Data;

@Data
public class RagCitationDto {

    private Long docId;

    private String title;

    private String snippet;

    private Double score;
}
