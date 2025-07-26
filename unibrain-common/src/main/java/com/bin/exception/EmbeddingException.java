package com.bin.exception;

/**
 * @author P-bin
 * @since 2023/10/14 16:17
 * 向量化服务异常类
 */
public class EmbeddingException extends Exception{
    // 无参构造函数
    public EmbeddingException() {
        super();
    }

    // 带错误消息的构造函数
    public EmbeddingException(String message) {
        super(message);
    }

    // 带错误消息和原始异常的构造函数
    public EmbeddingException(String message, Throwable cause) {
        super(message, cause);
    }

    // 带原始异常的构造函数
    public EmbeddingException(Throwable cause) {
        super(cause);
    }

}
