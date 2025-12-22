package com.lab201.monitor.common.exception;

import com.lab201.monitor.common.result.ResultCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true) // 建议加上这个，避免Lombok警告
public class LabException extends RuntimeException {

    private Integer code;

    // 1. 直接传错误码枚举（最常用）
    // 用法：throw new LabException(ResultCodeEnum.USER_NOT_EXIST);
    public LabException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    // 2. 自定义错误信息（灵活场景）
    // 用法：throw new LabException(500, "MinIO上传失败：文件太大");
    public LabException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}