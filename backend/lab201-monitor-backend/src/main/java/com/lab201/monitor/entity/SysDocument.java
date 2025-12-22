package com.lab201.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lab201.monitor.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_document")
@Schema(description = "知识库文档表")
public class SysDocument extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文件名")
    @TableField(value = "file_name")
    private String fileName;

    @Schema(description = "MinIO路径")
    @TableField(value = "file_path")
    private String filePath;

    @Schema(description = "向量ID")
    @TableField(value = "milvus_id")
    private String milvusId;

    @Schema(description = "状态: 0:解析中 1:成功 2:失败")
    @TableField(value = "status")
    private Integer status;

    @Schema(description = "文档摘要")
    @TableField(value = "summary")
    private String summary;
}