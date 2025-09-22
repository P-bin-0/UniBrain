package com.bin;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // 对所有API路径生效
                .allowedOrigins("http://localhost:5173")  // 允许的来源
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // 允许的HTTP方法
                .allowedHeaders("*")  // 允许的请求头
                .allowCredentials(true)  // 是否允许携带cookie
                .maxAge(3600);  // 预检请求的缓存时间（秒）
    }
}
