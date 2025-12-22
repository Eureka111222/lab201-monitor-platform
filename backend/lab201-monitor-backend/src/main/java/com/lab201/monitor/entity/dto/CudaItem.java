package com.lab201.monitor.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CudaItem implements Serializable {
    private String version;    // 例如 "11.1"
    private Boolean isDefault; // 例如 true
}