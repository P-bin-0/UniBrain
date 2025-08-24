package com.bin.configtion;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;

/**
 * Redis配置类
 * @author bin
 */
@Configuration
public class RedisConfig {
    /**
     * 配置RedisTemplate
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate实例
     */
    // 日期时间格式（LocalDateTime）
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // 年份格式（Year）
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 1. 自定义ObjectMapper
        ObjectMapper objectMapper = customObjectMapper();

        // 2. 配置JSON序列化器（通过构造函数传入ObjectMapper，替代弃用的setObjectMapper）
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // 3. String序列化器（用于key）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 4. 设置序列化方式
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 自定义ObjectMapper，解决时间类型序列化问题
     */
    private ObjectMapper customObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();

        // 处理LocalDateTime：序列化/反序列化为yyyy-MM-dd HH:mm:ss
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        timeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String dateStr = p.getValueAsString();
                return dateStr != null ? LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER) : null;
            }
        });

        // 处理Year：解决序列化错误，格式化为yyyy
        timeModule.addSerializer(Year.class, new JsonSerializer<Year>() {
            @Override
            public void serialize(Year value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.format(YEAR_FORMATTER));
            }
        });
        timeModule.addDeserializer(Year.class, new JsonDeserializer<Year>() {
            @Override
            public Year deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String yearStr = p.getValueAsString();
                return yearStr != null ? Year.parse(yearStr, YEAR_FORMATTER) : null;
            }
        });

        // 基础配置
        mapper.registerModule(timeModule); // 注册时间模块
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 禁用时间戳格式
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY); // 允许访问所有字段
        // 多态类型支持（如需反序列化复杂对象，保留此配置）
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return mapper;
    }
}
