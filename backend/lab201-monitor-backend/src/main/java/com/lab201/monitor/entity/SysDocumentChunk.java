package com.lab201.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lab201.monitor.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_document_chunk")
@Schema(description = "知识库文档切片表")
public class SysDocumentChunk extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "文档ID")
    @TableField(value = "doc_id")
    private Long docId;

    @Schema(description = "切片序号")
    @TableField(value = "chunk_index")
    private Integer chunkIndex;

    @Schema(description = "文档标题冗余")
    @TableField(value = "title")
    private String title;

    @Schema(description = "小切片内容，用于召回")
    @TableField(value = "chunk_content")
    private String chunkContent;

    @Schema(description = "父级上下文内容，用于回答")
    @TableField(value = "parent_content")
    private String parentContent;

    @Schema(description = "切片字符数")
    @TableField(value = "char_count")
    private Integer charCount;
}
