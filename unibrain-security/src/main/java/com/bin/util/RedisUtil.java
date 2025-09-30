package com.bin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * author : rounding
 * date : 2022-06-01
 * 实现针对常用数据类型的redis缓存处理：存、取、删
 */
@Component
public class RedisUtil {
    private final static Logger LOG = LoggerFactory.getLogger(RedisUtil.class);

    @Autowired
    RedisTemplate redisTemplate;

    //////////////// 基本的对象、包装类对象、String、实体类 ////////////////////////
    public <T> boolean setCacheObject(String key, T value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    public <T> boolean setCacheObject(String key, T value, Long timeout) {
        try {
            redisTemplate.opsForValue().set(key, value);
            expire(key, timeout);
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    public <T> T getCacheObject(String key, Class<T> cls) {
        try {
            ValueOperations<String, T> operations = redisTemplate.opsForValue();
            return operations.get(key);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    /////////////// List  ////////////////

    /**
     * 缓存List数据
     */
    public <T> boolean setCacheList(String key, List<T> list) {
        if (null != list && list.size() > 0) {
            try {
                delete(key);   // 没有delete语句，如果之前该key中已存有了List，则新的值会追加到原有List元素的前面
                redisTemplate.opsForList().leftPushAll(key, list);
                return true;
            } catch (Exception e) {
                LOG.error(e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * 缓存List数据 , 并设置保存时长，以秒为单位
     */
    public <T> boolean setCacheList(String key, List<T> list, Long timeout) {
        if (null != list && list.size() > 0) {
            try {
                delete(key);   // 没有delete语句，如果之前该key中已存有了List，则新的值会追加到原有List元素的前面
                redisTemplate.opsForList().leftPushAll(key, list);
                expire(key, timeout);
                return true;
            } catch (Exception e) {
                LOG.error(e.getMessage());
                return false;
            }
        }
        return false;
    }

    public <T> List<T> getCacheList(String key) {
        try {
            List<T> list = new ArrayList<>();
            ListOperations<String, T> listOperations = redisTemplate.opsForList();
            // 获取缓存的List的长度
            Long size = listOperations.size(key);
            for (int i = 0; i < size; i++) {
                list.add(listOperations.index(key, i));
            }
            return list;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    /////////////// Map  ////////////////

    /**
     * 缓存Map数据
     */
    public <T> boolean setCacheMap(String key, Map<String, T> map) {
        if (null != map && map.size() > 0) {
            try {
                redisTemplate.opsForHash().putAll(key, map);
                return true;
            } catch (Exception e) {
                LOG.error(e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * 缓存Map数据 , 并设置保存时长
     */
    public <T> boolean setCacheMap(String key, Map<String, T> map, Long timeout) {
        if (setCacheMap(key, map)) {
            try {
                expire(key, timeout);
                return true;
            } catch (Exception e) {
                LOG.error(e.getMessage());
                return false;
            }
        }
        return false;
    }

    public <T> Map<String, T> getCacheMap(String key) {
        try {
            Map<String, T> map = redisTemplate.opsForHash().entries(key);
            return map;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    /////////////// Set  ////////////////

    /**
     * 缓存Set数据
     */
    public <T> boolean setCacheSet(String key, Set<T> set) {
        if (null != set && set.size() > 0) {
            try {
                delete(key);
                SetOperations<String, T> setOperations = redisTemplate.opsForSet();
                for (T item : set) {
                    setOperations.add(key, item);
                }
                return true;
            } catch (Exception e) {
                LOG.error(e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * 缓存Set数据 , 并设置保存时长，以秒为单位
     */
    public <T> boolean setCacheSet(String key, Set<T> set, Long timeout) {
        if (null != set && set.size() > 0) {
            try {
                delete(key);
                SetOperations<String, T> setOperations = redisTemplate.opsForSet();
                for (T item : set) {
                    setOperations.add(key, item);
                }
                expire(key, timeout);
                return true;
            } catch (Exception e) {
                LOG.error(e.getMessage());
                return false;
            }
        }
        return false;
    }

    public <T> Set<T> getCacheSet(String key) {
        try {
            SetOperations<String, T> settOperations = redisTemplate.opsForSet();
            Set<T> set = settOperations.members(key);
            return set;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return null;
        }
    }


    ///////////////// 通用方法 ///////////////

    /**
     * 根据key删除
     */
    public void delete(String... key) {
        if (null != key && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(Arrays.asList(key));
            }
        }
    }

    /**
     * 获取缓存中的key信息
     *
     * @param pattern 查看符合规则的可以，如果传参 * ,查看所有key
     * @return key 构成的Set集合
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    /**
     * 指定key缓存时长 ， 以秒为单位
     */
    public boolean expire(String key, Long timeout) {
        try {
            if (timeout > 0) {
                redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    /**
     * 根据key获取过期时间
     */
    public long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return 0;
        }
    }
}