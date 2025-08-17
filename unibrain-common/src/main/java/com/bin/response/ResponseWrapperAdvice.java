package com.bin.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.filters.AddDefaultCharsetFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author P-bin
 * @since 2023/11/17 16:01
 * 统一响应体包装器
 */
@ControllerAdvice
public class ResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger log = LoggerFactory.getLogger(ResponseWrapperAdvice.class);

    // 定义静态常量 ObjectMapper，避免重复创建
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /*在此可定义排除的包，比如排除特定包下的controller*/
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
         // 对所有响应体进行包装
        /*还可以排除特定包下的controller，具体可根据需求实现*/
        if (returnType.hasMethodAnnotation(NoWrap.class)) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // 如果返回null，直接返回
        if(body == null) {
            return ApiResponse.success();
        }
        //处理字符串类型的响应
        if(body instanceof String){
            try {
                return OBJECT_MAPPER.writeValueAsString(ApiResponse.success(body));
            } catch (JsonProcessingException e) {
                log.error("字符串转换Json失败", e);
                throw new RuntimeException("字符串转换失败", e);
            }
        }
        // 如果已经是ApiResponse，直接返回
        if(body instanceof ApiResponse) {
            return body;
        }
        return ApiResponse.success(body);
    }
}
