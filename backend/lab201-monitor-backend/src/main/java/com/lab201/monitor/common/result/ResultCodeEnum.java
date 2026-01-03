package com.lab201.monitor.common.result;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 * 定制化：适配 Lab201 监控系统
 */
@Getter
public enum ResultCodeEnum {

    // --- 通用状态码 ---
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    
    // --- 客户端错误 (4xx) ---
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "没有权限执行该操作"),
    NOT_FOUND(404, "请求资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方式不支持"),
    Token_Empty(406, "Token为空"),
    Token_Error(407, "Token无效错误"),

    // --- 业务逻辑错误 (自定义) ---
    // 1000~1999: 用户与权限
    USER_NOT_EXIST(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLE(1003, "账号已被禁用"),
    VERIFY_CODE_ERROR(1004, "验证码错误"),
    
    // 2000~2999: 基础设施与监控
    SERVER_OFFLINE(2001, "服务器已离线"),
    GPU_DATA_EMPTY(2002, "暂无GPU监控数据"),
    AGENT_REPORT_TIMEOUT(2003, "Agent上报超时"),
    
    // 3000~3999: RAG 与 AI
    FILE_UPLOAD_ERROR(3001, "文件上传失败"),
    FILE_PARSE_ERROR(3002, "文件解析失败"),
    MILVUS_CONNECTION_ERROR(3003, "向量数据库连接失败"),
    AI_SERVICE_BUSY(3004, "AI服务繁忙，请稍后重试"),
    DOC_NOT_FOUND(3005, "文档不存在或已删除");

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}