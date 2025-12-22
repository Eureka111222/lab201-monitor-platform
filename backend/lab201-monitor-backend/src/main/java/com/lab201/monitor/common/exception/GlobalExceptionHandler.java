package com.lab201.monitor.common.exception;

import com.lab201.monitor.common.result.Result;
import com.lab201.monitor.common.result.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 * 作用：拦截所有Controller抛出的异常，转为统一的Result对象
 */
@Slf4j // 开启日志功能
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. 捕获自定义业务异常 (LabException)
     * 说明：这是我们预期的错误，不需要打印堆栈，记录日志即可。
     */
    @ExceptionHandler(LabException.class)
    public Result<?> handleLabException(LabException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        // 返回我们在 LabException 里指定的 code 和 message
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 2. 捕获所有未知的系统异常 (Exception)
     * 说明：这是意料之外的错误（如空指针、SQL语法错），必须打印堆栈日志！
     * 返回：统一返回 "操作失败" 或 500 错误。
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        // 关键：打印完整堆栈信息，方便排查 BUG
        log.error("系统内部异常:", e);

        // 给前端返回一个笼统的错误，不要把具体的 "NullPointerException" 返回给用户看，不安全也不友好
        return Result.fail(ResultCodeEnum.FAIL);
    }
}