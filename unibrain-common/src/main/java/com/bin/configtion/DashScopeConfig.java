package com.bin.configtion;

import com.alibaba.dashscope.aigc.generation.Generation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author P-bin
 * @date 2023/12/20 14:25
 * 阿里云DashScope配置
 */
@Configuration
public class DashScopeConfig {
    @Bean
    public Generation generation() {
        return new Generation();
    }
}
