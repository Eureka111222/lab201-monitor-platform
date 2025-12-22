package com.lab201.monitor.common.result;

import lombok.Data;

/**
 * 全局统一返回结果类
 * @param <T> 返回数据的类型
 */
@Data
public class Result<T> {

    // 业务状态码 (200表示成功，非200表示异常)
    private Integer code;

    // 提示信息
    private String message;

    // 数据载体
    private T data;

    // 私有构造，强制使用静态方法创建
    private Result() {}

    /**
     * 构建基础 Result
     */
    private static <T> Result<T> build(T data, Integer code, String message) {
        Result<T> result = new Result<>();
        result.setData(data);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 通过枚举构建 Result
     */
    public static <T> Result<T> build(T data, ResultCodeEnum resultCodeEnum) {
        return build(data, resultCodeEnum.getCode(), resultCodeEnum.getMessage());
    }

    // ================= 成功响应 =================

    /**
     * 成功，不带数据
     */
    public static <T> Result<T> success() {
        return build(null, ResultCodeEnum.SUCCESS);
    }

    /**
     * 成功，带数据
     */
    public static <T> Result<T> success(T data) {
        return build(data, ResultCodeEnum.SUCCESS);
    }

    // ================= 失败响应 =================

    /**
     * 通用失败
     */
    public static <T> Result<T> fail() {
        return build(null, ResultCodeEnum.FAIL);
    }

    /**
     * 指定枚举的失败
     */
    public static <T> Result<T> fail(ResultCodeEnum resultCodeEnum) {
        return build(null, resultCodeEnum);
    }

    /**
     * 自定义消息的失败 (常用于捕获异常后直接返回异常信息)
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return build(null, code, message);
    }
}