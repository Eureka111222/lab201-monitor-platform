package com.lab201.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lab201.monitor.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role")
@Schema(description = "角色表")
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "角色名称")
    @TableField(value = "role_name")
    private String roleName;

    @Schema(description = "角色编码 (如: ROLE_ADMIN)")
    @TableField(value = "role_code")
    private String roleCode;

    @Schema(description = "描述")
    @TableField(value = "description")
    private String description;
}