package com.bin.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author P-bin
 * @since 2023/10/14 16:17
 * Redis缓存工具类
 */
@Component
public class RedisCacheUtil {
    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 设置缓存
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    public void setCache(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值
     */
    public String getCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    public void deleteCache(String key) {
        redisTemplate.delete(key);
    }
}
