package com.lab201.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lab201.monitor.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user")
@Schema(description = "系统用户表")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "账号名称(学号)")
    @TableField(value = "username")
    private String username;

    @Schema(description = "加密密码")
    // select = false 保证普通查询时不返回密码，增强安全性
    @TableField(value = "password", select = false)
    private String password;

    @Schema(description = "真实姓名")
    @TableField(value = "real_name")
    private String realName;

    @Schema(description = "人脸特征值(Base64)")
    @TableField(value = "face_feature")
    private String faceFeature;

    @Schema(description = "头像URL")
    @TableField(value = "avatar")
    private String avatar;

    @Schema(description = "状态: 1正常 0禁用")
    @TableField(value = "status")
    private Integer status;

    // 🔥 重点：这个字段数据库里没有，但是为了传给前端，我们手动加在这里
    @Schema(description = "这个字段数据库里没有，但是为了传给前端，我们手动加在这里")
    @TableField(exist = false)
    private String role;
}