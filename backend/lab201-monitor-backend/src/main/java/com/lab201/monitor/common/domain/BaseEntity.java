package com.lab201.monitor.common.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 实体通用父类
 * 包含所有表共有的字段和通用前端参数
 */
@Data
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID (所有表统一使用自增或雪花算法)
     */
    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     * fill = FieldFill.INSERT 表示插入时自动填充
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    //@JsonIgnore
    private LocalDateTime createTime;

    /** 更新时间 */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    //@JsonIgnore
    private LocalDateTime updateTime;

    /** 逻辑删除 (1已删 0未删) */
    @Schema(description = "逻辑删除")
    @JsonIgnore // 前端通常不需要看到这个字段
    @TableLogic
    private Integer deleted;

    /**
     * 请求参数 (用于前端传范围查询等非数据库字段参数)
     * 例如 params.beginTime, params.endTime
     */
    @Schema(description = "请求参数 (用于前端传范围查询等非数据库字段参数)")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private Map<String, Object> params;

    /**
     * 获取请求参数 Map (防空指针)
     */
    public Map<String, Object> getParams() {
        if (params == null) {
            params = new HashMap<>();
        }
        return params;
    }
}