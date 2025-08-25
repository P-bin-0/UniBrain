package com.bin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
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
     * @param key 缓存键
     * @param clazz 目标对象类型
     * @return 反序列化后的对象，若不存在则返回null
     */
    public <T> T getObject(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            // RedisTemplate会自动进行反序列化
            return clazz.cast(value);
        } catch (ClassCastException e) {
            log.error("对象类型转换失败，key: {}, 目标类型: {}", key, clazz.getName(), e);
            throw new RuntimeException("对象类型转换异常", e);
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
     * 批量删除单条数据缓存（针对具体key集合）
     * @param keys 要删除的key集合（非模糊匹配）
     * @return 成功删除的数量
     */
    public long deleteBatch(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            log.warn("批量删除缓存失败：key集合为空");
            return 0;
        }
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("批量删除缓存失败，keys: {}", keys, e);
            throw new RuntimeException("Redis操作异常", e);
        }
    }
    /**
     * 批量删除缓存（支持模糊匹配，如"books:page:*"）
     * @param pattern 模糊匹配的key模式（需包含通配符*）
     */
    public void deleteByPattern(String pattern) { // 方法名改为deleteByPattern
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("批量删除缓存失败，pattern: {}", pattern, e);
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
