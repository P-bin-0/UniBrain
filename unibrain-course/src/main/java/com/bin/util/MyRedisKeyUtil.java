package com.bin.util;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Component;


/**
 * Redis Key工具类
 * 统一管理Redis中的键命名和过期时间
 */
@Component
public class MyRedisKeyUtil {

    // 项目前缀，避免不同项目键冲突
    private static final String PROJECT_PREFIX = "UniBrain";

    // 分隔符
    private static final String SEPARATOR = ":";

    // 缓存相关
    private static final String CACHE_PREFIX = "cache";
    private static final String CACHE_CONFIG = "config";

    // 分布式锁
    private static final String LOCK_PREFIX = "lock";

    // 限流相关
    private static final String RATE_LIMIT_PREFIX = "rate_limit";

    // 消息队列
    private static final String MQ_PREFIX = "mq";

    // 布隆过滤器
    private static final String BLOOM_FILTER_PREFIX = "bloom_filter";


    // 构建基础键（包含项目前缀）
    private static String buildBaseKey(String... parts) {
        StringBuilder sb = new StringBuilder(PROJECT_PREFIX);
        for (String part : parts) {
            sb.append(SEPARATOR).append(part);
        }
        return sb.toString();
    }
     // ==================== 布隆过滤器 ====================

    /**
     * 生成布隆过滤器键
     * @param filterName 过滤器名
     * @return 完整布隆过滤器键
     */
    public static String getBloomFilterKey(String filterName) {
        return buildBaseKey(BLOOM_FILTER_PREFIX, filterName);
    }

    // ==================== 缓存相关 ====================

    /**
     * 生成通用缓存键
     * @param module 模块名
     * @param key 键名
     * @return 完整缓存键
     */
    public static String getCacheKey(String module, String key) {
        return buildBaseKey(CACHE_PREFIX, module, key);
    }
    /**
     * 生成配置缓存键
     * @param configName 配置名
     * @return 完整配置缓存键
     */
    public static String getConfigCacheKey(String configName) {
        return buildBaseKey(CACHE_PREFIX, CACHE_CONFIG, configName);
    }
    // ==================== 分布式锁 ====================

    /**
     * 生成分布式锁键
     * @param resource 资源名
     * @return 完整分布式锁键
     */
    public static String getLockKey(String resource) {
        return buildBaseKey(LOCK_PREFIX, resource);
    }
    /**
     * 生成带ID的分布式锁键
     * @param resource 资源名
     * @param id 资源ID
     * @return 完整分布式锁键
     */
    public static String getLockKey(String resource, String id) {
        return buildBaseKey(LOCK_PREFIX, resource, id);
    }
    // ==================== 限流相关 ====================

    /**
     * 生成限流键
     * @param resource 资源名
     * @param userId 用户ID
     * @return 完整限流键
     */
    public static String getRateLimitKey(String resource, String userId) {
        return buildBaseKey(RATE_LIMIT_PREFIX, resource, userId);
    }
    // ==================== 消息队列 ====================

    /**
     * 生成消息队列键
     * @param queueName 队列名
     * @return 完整消息队列键
     */
    public static String getMqKey(String queueName) {
        return buildBaseKey(MQ_PREFIX, queueName);
    }

    /**
     * 生成消息队列主题键
     * @param topic 主题名
     * @return 完整消息队列主题键
     */
    public static String getMqTopicKey(String topic) {
        return buildBaseKey(MQ_PREFIX, "topic", topic);
    }

    // 布隆过滤器，启动时先将数据库中的数据加载到布隆过滤器中


}