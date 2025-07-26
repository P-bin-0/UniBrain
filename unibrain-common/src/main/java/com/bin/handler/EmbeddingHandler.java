package com.bin.handler;

import com.bin.exception.EmbeddingException;
import com.bin.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author P-bin
 * @since 2023/10/14 16:20
 * 向量化服务异常的处理类
 */
@RestControllerAdvice
public class EmbeddingHandler {
    @ExceptionHandler(EmbeddingException.class)
    public ApiResponse<String> handleEmbeddingException(EmbeddingException e){
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),e.getMessage());
    }
}
