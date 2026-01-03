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
@Schema(description = "系统知识库文档表")
public class SysDocument extends BaseEntity {

    private static final long serialVersionUID = 1L;

    // ==========================================
    // 1. 基础展示信息
    // ==========================================

    @Schema(description = "文档标题(展示用)")
    @TableField(value = "title")
    private String title;

    @Schema(description = "原始文件名(带后缀)")
    @TableField(value = "file_name")
    private String fileName;

    @Schema(description = "文件后缀(如 pdf, docx)")
    @TableField(value = "file_ext")
    private String fileExt;

    @Schema(description = "文件大小(字节)")
    @TableField(value = "file_size")
    private Long fileSize;

    // ==========================================
    // 2. MinIO 存储核心字段
    // ==========================================

    @Schema(description = "MinIO桶名")
    @TableField(value = "bucket_name")
    private String bucketName;

    @Schema(description = "MinIO对象名(UUID)")
    @TableField(value = "object_name")
    private String objectName;

    @Schema(description = "预留访问URL")
    @TableField(value = "file_url")
    private String fileUrl;

    // ==========================================
    // 3. 核心权限控制
    // ==========================================

    @Schema(description = "可见范围: 0-私有, 1-全员")
    @TableField(value = "view_scope")
    private Integer viewScope;

    // ==========================================
    // 4. RAG 知识库扩展
    // ==========================================

    @Schema(description = "是否存入向量库: 0-否, 1-是")
    @TableField(value = "is_rag")
    private Integer isRag;

    @Schema(description = "向量ID")
    @TableField(value = "milvus_id")
    private String milvusId;

    @Schema(description = "解析状态: 0-未解析, 1-处理中, 2-成功, 3-失败")
    @TableField(value = "parse_status")
    private Integer parseStatus;

    @Schema(description = "AI摘要")
    @TableField(value = "summary")
    private String summary;

    // ==========================================
    // 5. 标准审计字段 (业务审计)
    // ==========================================

    @Schema(description = "上传者ID")
    @TableField(value = "uploader_id")
    private Long uploaderId;

    @Schema(description = "上传者姓名(冗余字段用于展示)")
    @TableField(value = "uploader_name")
    private String uploaderName;

}