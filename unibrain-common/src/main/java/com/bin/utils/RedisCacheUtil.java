package com.bin.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author P-bin
 * @since 2023/10/14 16:17
 * Redis缓存工具类
 */
@Component
public class RedisCacheUtil {
    private static final Logger log = LoggerFactory.getLogger(RedisCacheUtil.class);
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    /**
     * 设置缓存（带过期时间）
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void setCache(String key, String value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("设置字符串缓存失败，key: {}", key, e);
            throw new RuntimeException("Redis操作异常", e);
        }
    }

    /**
     * 获取缓存
     * @param key 缓存键
     * @return 缓存值
     */
    public String getCache(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("获取字符串缓存失败，key: {}", key, e);
            throw new RuntimeException("Redis操作异常", e);
        }
    }
    // -------------------------- 对象操作 --------------------------
    /**
     * 设置对象缓存（自动JSON序列化，带过期时间）
     */
    public <T> void setObject(String key, T value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("设置对象缓存失败，key: {}", key, e);
            throw new RuntimeException("Redis操作异常", e);
        }
    }

    /**
     * 获取对象缓存（自动JSON反序列化）
     */
    @SuppressWarnings("unchecked")
    public <T> T getObject(String key) {
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取对象缓存失败，key: {}", key, e);
            throw new RuntimeException("Redis操作异常", e);
        }
    }

    /**
     * 删除缓存
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            log.error("删除缓存失败，key: {}", key, e);
            throw new RuntimeException("Redis操作异常", e);
        }
    }
    /**
     * 尝试获取锁
     */
    public Boolean tryLock(String key, long expireTime, TimeUnit unit) {
        return redisTemplate.opsForValue()
                .setIfAbsent(key, "1", expireTime, unit);
    }
    /**
     * 释放锁
     */
    public void unlock(String key) {
        redisTemplate.delete(key);
    }
}
