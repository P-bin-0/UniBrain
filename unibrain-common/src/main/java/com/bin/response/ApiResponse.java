package com.bin.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author P-bin
 * @since 2023/11/17 16:01
 * 统一响应体
 */
@Data
@NoArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;

    public ApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 快速返回成功响应有数据
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }
    // 返回成功无响应数据
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "操作成功", null);
    }
    //自定义错误码和消息
    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
    //快速返回错误响应
    public static <T> ApiResponse<T> error() {
        return new ApiResponse<>(500, "系统异常", null);
    }
}