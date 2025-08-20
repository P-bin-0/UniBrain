package com.bin.handler;

import com.bin.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author P-bin
 * @since 2023/11/17 16:01
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    //捕获运行时异常
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        return ApiResponse.error(500, e.getMessage());
    }
    //捕获非法参数异常
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(IllegalArgumentException e) {
        return ApiResponse.error(400, e.getMessage());
    }
    //捕获缺少请求参数异常
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ApiResponse.error(400, "缺少必要的请求参数: " + e.getParameterName());
    }
    //其他异常...
}