package com.bin.handler;

import com.bin.Exception.InValidTokenException;
import com.bin.Exception.NonTokenException;
import com.bin.Exception.UserNonLoginException;
import com.bin.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice // 全局异常处理，返回JSON
public class GlobalExceptionHandler {

    // 处理token为空的异常
    @ExceptionHandler(NonTokenException.class)
    public ApiResponse<Void> handleNonTokenException(NonTokenException e) {
        return ApiResponse.error(401, e.getMessage()); // 401：未授权
    }

    // 处理token无效的异常
    @ExceptionHandler(InValidTokenException.class)
    public ApiResponse<Void> handleInValidTokenException(InValidTokenException e) {
        return ApiResponse.error(401, e.getMessage()); // 401：未授权
    }

    // 处理登录过期的异常
    @ExceptionHandler(UserNonLoginException.class)
    public ApiResponse<Void> handleUserNonLoginException(UserNonLoginException e) {
        return ApiResponse.error(401, e.getMessage()); // 401：未授权
    }

    // 处理权限不足的异常（如果有）
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException e) {
        return ApiResponse.error(403, "权限不足：" + e.getMessage()); // 403：禁止访问
    }

    // 处理其他未知异常
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOtherException(Exception e) {
        e.printStackTrace(); // 日志记录异常详情
        return ApiResponse.error(500, "服务器内部错误"); // 500：服务器错误
    }
}
