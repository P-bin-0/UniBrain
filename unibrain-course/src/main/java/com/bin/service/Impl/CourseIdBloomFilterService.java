package com.bin.service.Impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bin.dto.Courses;
import com.bin.mapper.CoursesMapper;
import com.bin.util.MyRedisKeyUtil;
import jakarta.annotation.PostConstruct;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseIdBloomFilterService {
    @Autowired
    private RedissonClient redisson;
    @Autowired
    private CoursesMapper coursesMapper;

    RBloomFilter<Long> bloomFilter;
    
    @PostConstruct
    public void initBloomFilter() {
        String bloomFilterKey = MyRedisKeyUtil.getBloomFilterKey("courseId");
        bloomFilter = redisson.getBloomFilter(bloomFilterKey);
        // 初始化布隆过滤器，预计数据量100万，误判率0.1%
        // tryInit 会在过滤器不存在时创建，存在则忽略初始化
        bloomFilter.tryInit(1000000L, 0.001);
        // 从数据库加载所有现有的有效ID
        List<Long> courseIds = coursesMapper.selectList(Wrappers.<Courses>lambdaQuery()
                .select(Courses::getId)
                .eq(Courses::getIsActive, true)
                .eq(Courses::getCourseType, "选修"))
                .stream()
                .map(Courses::getId)
                .toList();
        // 批量添加到布隆过滤器
        bloomFilter.add(courseIds);
    }
    /**
     * 添加一个新的课程ID到布隆过滤器
     * @param courseId 课程ID
     */
    public void addCourseId(Long courseId) {
        if (bloomFilter.contains(courseId)) {
            return;
        }
        if (courseId != null) {
            bloomFilter.add(courseId);
        }
    }
    /**
     * 注意：标准布隆过滤器本身不支持真正的删除操作
     * 这是因为布隆过滤器的工作原理是在多个哈希函数对应的位置设置1，无法安全地将这些位重置为0
     * 不使用
     * @param courseId 课程ID
     */
    public void deleteCourseId(Long courseId) {
        if (courseId != null) {
            if (bloomFilter.contains(courseId)) {
                // 方式1：不做任何操作（默认策略）
                // 布隆过滤器只能增加元素，不能删除元素
                // 这是最常见的处理方式，但会导致误判率随时间增加
                
                // 方式2：记录已删除的ID，在查询时额外检查
                // 可在Redis中维护一个已删除ID的集合
                // redisTemplate.opsForSet().add("deleted_course_ids", courseId.toString());
                
                // 方式3：重建整个布隆过滤器
                // 当日志增长到一定阈值时触发
                // 这种方式成本较高，但能完全重置过滤器状态
                // rebuildBloomFilter();
            }
        }
    }
    
    /**
     * 重建布隆过滤器的方法（可选实现）
     */
    private void rebuildBloomFilter() {
        String bloomFilterKey = MyRedisKeyUtil.getBloomFilterKey("courseId");
        // 删除旧的布隆过滤器
        redisson.getBloomFilter(bloomFilterKey).delete();
        // 重新初始化并加载数据
        initBloomFilter();
    }
    
    /**
     * 检查课程ID是否有效（结合布隆过滤器和数据库验证）
     * @param courseId 课程ID
     * @return 课程ID是否有效存在
     */
    public boolean isValidCourseId(Long courseId) {
        // 快速判断：如果布隆过滤器说不存在，那一定不存在
        if (!mightContain(courseId)) {
            return false;
        }
        
        // 二次验证：布隆过滤器说存在，需要去数据库确认是否真的存在
        // 这是处理布隆过滤器误判和元素删除的常用策略
        Courses course = coursesMapper.selectById(courseId);
        return course != null && course.getIsActive() && "选修".equals(course.getCourseType());
    }
    /**
     * 检查一个课程ID是否可能存在于布隆过滤器中
     * @param courseId 课程ID
     * @return 如果可能存在则返回true，否则返回false
     */
    public boolean mightContain(Long courseId) {
        return courseId != null && bloomFilter.contains(courseId);
    }
}