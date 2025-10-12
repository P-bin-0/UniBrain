package com.bin.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.*;
import com.bin.mapper.CoursesMapper;
import com.bin.mapper.StudentCourseMapper;
import com.bin.service.CoursesService;

import com.bin.util.MyRedisKeyUtil;
import com.bin.util.SecurityUtil;
import com.rabbitmq.client.Channel;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CoursesServiceImpl extends ServiceImpl<CoursesMapper, Courses> implements CoursesService {
    @Autowired
    private CoursesMapper coursesMapper;
    @Autowired
    private StudentCourseMapper studentCourseMapper;
    @Autowired
    private CourseIdBloomFilterService courseIdBloomFilterService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 根据ID更新课程状态(管理员启用/禁用)
     * @param id 课程ID
     * @param isActive 新的状态值
     */
    @Override
    public void updateCourseById(Long id, Boolean isActive) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        // 缓存键
        String cacheKey = MyRedisKeyUtil.getCacheKey("courses", id.toString());
        // 删除该课程在redis中的缓存
        Boolean deleted = redisTemplate.delete(cacheKey);
        if (Boolean.FALSE.equals(deleted)) {
            System.err.println("删除缓存失败：" + cacheKey);
        }
        Courses courses = coursesMapper.selectById(id);
        if (courses == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        int count = coursesMapper.updateCourseById(id, isActive);
        if (count != 1) {
            throw new RuntimeException("更新失败");
        }
        // 3. 优化：清理所有用户的已选课程缓存（先查键再删除，确保生效）
        String selectedCachePattern = MyRedisKeyUtil.getCacheKey("select:course:ids", "*");
        Set<String> selectedCacheKeys = redisTemplate.keys(selectedCachePattern);
        if (selectedCacheKeys != null && !selectedCacheKeys.isEmpty()) {
            redisTemplate.delete(selectedCacheKeys); // 逐个删除，避免通配符失效问题
            System.out.println("已清理已选课程缓存：" + selectedCacheKeys.size() + "个");
        }

        // 4. 优化：清理所有用户的未选课程缓存（同理）
        String unselectedCachePattern = MyRedisKeyUtil.getCacheKey("UnselectedElectiveCourses", "*");
        Set<String> unselectedCacheKeys = redisTemplate.keys(unselectedCachePattern);
        if (unselectedCacheKeys != null && !unselectedCacheKeys.isEmpty()) {
            redisTemplate.delete(unselectedCacheKeys);
            System.out.println("已清理未选课程缓存：" + unselectedCacheKeys.size() + "个");
        }
        // 清理所有查询选修课程缓存
        String allCachePattern = MyRedisKeyUtil.getCacheKey("courses", "queryAll");
        Set<String> allCacheKeys = redisTemplate.keys(allCachePattern);
        if (allCacheKeys != null && !allCacheKeys.isEmpty()) {
            redisTemplate.delete(allCacheKeys);
        }
    }
    /**
     * 选课
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务，确保数据库操作的原子性
    public void addCourseById(Long id){
        Long studentId = SecurityUtil.getCurrentUserId();
        // 检查布隆过滤器是否存在该课程（防止缓存穿透）
        if (!courseIdBloomFilterService.isValidCourseId(id)) {
            throw new IllegalArgumentException("课程不存在");
        }
        if (studentId == null) {
            throw new RuntimeException("用户未登录");
        }
        // 检查学生是否已选该课程
        String selectKey = MyRedisKeyUtil.getCacheKey("StudentCourse", studentId + "_" + id);
        // 检查缓存中是否已选
        Boolean isMember = redisTemplate.opsForSet().isMember(selectKey, id.toString());
        if (Boolean.TRUE.equals(isMember)) {
            throw new IllegalArgumentException("学生已选该课程");
        }
        // 缓存未命中时，查数据库确认
        LambdaQueryWrapper<StudentCourse> dbCheckWrapper = new LambdaQueryWrapper<>();
        dbCheckWrapper.eq(StudentCourse::getStudentId, studentId)
                .eq(StudentCourse::getCourseId, id);
        Long dbCount = studentCourseMapper.selectCount(dbCheckWrapper);
        if (dbCount > 0) {
            // 数据库已有记录但缓存缺失，同步缓存并抛异常
            redisTemplate.opsForSet().add(selectKey, id.toString());
            redisTemplate.expire(selectKey, 1, TimeUnit.DAYS);
            throw new IllegalArgumentException("学生已选该课程");
        }
        // 使用redis分布式锁，防止并发修改同一课程
        String lockKey = MyRedisKeyUtil.getLockKey("CourseLock", id.toString());
        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.lock(30, TimeUnit.SECONDS); // 自动续期，即使执行久也不会丢锁
            // 查询redis中的剩余名额
            String quotaKey = MyRedisKeyUtil.getCacheKey("CourseQuota", id.toString());

            Boolean hasKey = redisTemplate.hasKey(quotaKey);
            if (!hasKey) {
                Courses course = coursesMapper.selectById(id);
                if (course == null) {
                    throw new IllegalArgumentException("课程不存在");
                }
                redisTemplate.opsForValue().set(quotaKey, course.getRemainingQuota(), 30, TimeUnit.MINUTES);
            }
            // 使用redis原子操作预扣减课程名额
            Long newQuota = redisTemplate.opsForValue().decrement(quotaKey);
            redisTemplate.expire(quotaKey, 30, TimeUnit.MINUTES); // 重置过期时间

            // 判断是否超卖
            if (newQuota < 0) {
                redisTemplate.opsForValue().increment(quotaKey); // 回滚
                redisTemplate.expire(quotaKey, 10, TimeUnit.MINUTES);
                throw new IllegalArgumentException("课程名额已占满");
            }
            // 检查学生是否已选（再次确认，防止并发）
            LambdaQueryWrapper<StudentCourse> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StudentCourse::getStudentId, studentId)
                    .eq(StudentCourse::getCourseId, id);
            if (studentCourseMapper.selectCount(wrapper) > 0) {
                // 回滚预扣减
                redisTemplate.opsForValue().increment(quotaKey);
                redisTemplate.expire(quotaKey, 10, TimeUnit.MINUTES);
                throw new IllegalArgumentException("学生已选该课程");
            }
            // 清理当前学生的已选课程缓存
            String selectedCacheKey = MyRedisKeyUtil.getCacheKey("select:course:ids", studentId.toString());
            redisTemplate.delete(selectedCacheKey);

            // 清理当前学生的未选课程缓存
            String unselectedCacheKey = MyRedisKeyUtil.getCacheKey("UnselectedElectiveCourses", studentId.toString());
            redisTemplate.delete(unselectedCacheKey);

            // 清理查询所有选修课程缓存
            String allCachePattern = MyRedisKeyUtil.getCacheKey("courses", "queryAll");
            Set<String> allCacheKeys = redisTemplate.keys(allCachePattern);
            if (allCacheKeys != null && !allCacheKeys.isEmpty()) {
                redisTemplate.delete(allCacheKeys);
            }
            // 8. 发送异步消息到 RabbitMQ（选课成功，等待持久化）
            CourseSelectionMessage message = new CourseSelectionMessage();
            message.setStudentId(studentId);
            message.setCourseId(id);
            message.setTimestamp(System.currentTimeMillis());

            rabbitTemplate.convertAndSend("course.select.exchange", "course.select.routing.key", message);

            // 9. 将学生选课记录加入 Redis Set，避免重复选（可后续同步到 DB）
            redisTemplate.opsForSet().add(selectKey, id.toString());
            // 可设置过期时间，或由消费者确认后刷新
            redisTemplate.expire(selectKey, 1, TimeUnit.DAYS);

        } finally {
            String selectedCacheKey = MyRedisKeyUtil.getCacheKey("select:course:ids", studentId.toString());
            redisTemplate.delete(selectedCacheKey);
            String unselectedCacheKey = MyRedisKeyUtil.getCacheKey("UnselectedElectiveCourses", studentId.toString());
            if (redisTemplate.hasKey(unselectedCacheKey)) {
                redisTemplate.delete(unselectedCacheKey);
            }
            // 清理查询所有选修课程缓存
            String allCachePattern = MyRedisKeyUtil.getCacheKey("courses", "queryAll");
            Set<String> allCacheKeys = redisTemplate.keys(allCachePattern);
            if (allCacheKeys != null && !allCacheKeys.isEmpty()) {
                redisTemplate.delete(allCacheKeys);
            }
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    /**
     * 退课
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseById(Long id){
        Long studentId = SecurityUtil.getCurrentUserId();
        // 布隆过滤器检查是否有该课程
        if (!courseIdBloomFilterService.isValidCourseId(id)) {
            throw new IllegalArgumentException("课程不存在");
        }
        if (studentId == null) {
            throw new RuntimeException("用户未登录");
        }
        // redis检查是否已选课程
        String selectKey = MyRedisKeyUtil.getCacheKey("StudentCourse", studentId + "_" + id);
        // 检查缓存中是否已选
        boolean cacheHasRecord = redisTemplate.hasKey(selectKey);
        // 缓存中没有记录时，查数据库确认
        if (!cacheHasRecord) {
            LambdaQueryWrapper<StudentCourse> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StudentCourse::getStudentId, studentId)
                    .eq(StudentCourse::getCourseId, id);
            Long count = studentCourseMapper.selectCount(wrapper);
            if (count == 0) {
                throw new IllegalArgumentException("用户未选该课程");
            } else {
                // 数据库有记录但缓存缺失，同步缓存（修复缓存）
                redisTemplate.opsForSet().add(selectKey, id.toString());
                redisTemplate.expire(selectKey, 1, TimeUnit.DAYS);
            }
        }
        // 加锁
        String lockKey = MyRedisKeyUtil.getLockKey("CourseLock", id.toString());
        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.lock(30, TimeUnit.SECONDS); // 自动续期，即使执行久也不会丢锁
            // 查询redis中的剩余名额
            String quotaKey = MyRedisKeyUtil.getCacheKey("CourseQuota", id.toString());
            Integer remainingQuota = (Integer) redisTemplate.opsForValue().get(quotaKey);
            if (remainingQuota == null) {
                Courses course =coursesMapper.selectById(id);
                if (course == null) {
                    throw new IllegalArgumentException("课程不存在");
                }
                // 缓存课程剩余名额
                remainingQuota = course.getRemainingQuota();
                redisTemplate.opsForValue().set(quotaKey, remainingQuota, 30, TimeUnit.MINUTES); // 缓存10分钟
            }
            // 使用 Redis 原子操作预释放名额（+1）
            redisTemplate.opsForValue().increment(quotaKey); // 直接 +1，无需判断
            // 重新设置过期时间
            redisTemplate.expire(quotaKey, 30, TimeUnit.MINUTES);
            LambdaQueryWrapper<StudentCourse> dbWrapper = new LambdaQueryWrapper<>();
            dbWrapper.eq(StudentCourse::getStudentId, studentId)
                    .eq(StudentCourse::getCourseId, id);
            if (studentCourseMapper.selectCount(dbWrapper) == 0) {
                redisTemplate.opsForValue().decrement(quotaKey);
                redisTemplate.expire(quotaKey, 10, TimeUnit.MINUTES);
                throw new IllegalArgumentException("学生未选该课程");
            }
            // 发送退课消息到 RabbitMQ
            CourseDropMessage message = new CourseDropMessage(studentId, id);
            rabbitTemplate.convertAndSend("course.select.exchange", "course.drop.routing.key", message);

            // 清理当前学生的已选课程缓存
            String selectedCacheKey = MyRedisKeyUtil.getCacheKey("select:course:ids", studentId.toString());
            redisTemplate.delete(selectedCacheKey);
            // 清理当前学生的未选课程缓存
            String unselectedCacheKey = MyRedisKeyUtil.getCacheKey("UnselectedElectiveCourses", studentId.toString());
            redisTemplate.delete(unselectedCacheKey);

            // 清理查询所有选修课程缓存
            String allCachePattern = MyRedisKeyUtil.getCacheKey("courses", "queryAll");
            Set<String> allCacheKeys = redisTemplate.keys(allCachePattern);
            if (allCacheKeys != null && !allCacheKeys.isEmpty()) {
                redisTemplate.delete(allCacheKeys);
            }

            // 从 Redis Set 中移除已选记录
            redisTemplate.opsForSet().remove(selectKey, id.toString());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 查询用户已选选修课
     */
    @Override
    public List<Courses> getIsActiveTrue() {
        // 获取当前用户id
        Long studentId = SecurityUtil.getCurrentUserId();
        // 查询用户已选选修课
        if (studentId == null) {
            throw new RuntimeException("用户未登录");
        }
        String cacheKey = MyRedisKeyUtil.getCacheKey("select:course:ids", studentId.toString());
        String lockKey = MyRedisKeyUtil.getLockKey("SelectedCoursesLock", studentId.toString());

        // 1. 先从缓存拿 courseId 列表
        Set<String> courseIdStrs = redisTemplate.opsForSet().members(cacheKey);
        if (courseIdStrs != null && !courseIdStrs.isEmpty()) {
            List<Long> courseIds = courseIdStrs.stream()
                    .filter(idStr -> !"-1".equals(idStr)) // 过滤空标记，不处理 -1
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            if (courseIds.isEmpty()) {
                return Collections.emptyList();
            }
            return coursesMapper.selectBatchIds(courseIds);
        }

        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.lock(30, TimeUnit.SECONDS);
            courseIdStrs = redisTemplate.opsForSet().members(cacheKey);
            if (courseIdStrs != null && !courseIdStrs.isEmpty()) {
                List<Long> courseIds = courseIdStrs.stream()
                        .filter(idStr -> !"-1".equals(idStr)) // 过滤空标记，不处理 -1
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
                if (courseIds.isEmpty()) {
                    return Collections.emptyList();
                }
                return coursesMapper.selectBatchIds(courseIds);
            }

            List<Courses> courses = queryElectiveCoursesFromDB(studentId);
            if (courses.isEmpty()) {
                redisTemplate.opsForSet().add(cacheKey, "-1"); // 特殊标记空结果
                redisTemplate.expire(cacheKey, 30, TimeUnit.MINUTES);
            } else {
                List<String> ids = courses.stream()
                        .map(c -> c.getId().toString())
                        .collect(Collectors.toList());
                redisTemplate.opsForSet().add(cacheKey, ids.toArray(new String[0]));
                redisTemplate.expire(cacheKey, 30, TimeUnit.MINUTES);
            }
            return courses;
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }
    // 查询用户已选选修课（数据库）
    private List<Courses> queryElectiveCoursesFromDB(Long studentId) {
        return coursesMapper.selectElectiveCoursesByStudentId(studentId);
    }

    /**
     * 查询用户未选选修课
     */
    @Override
    public List<Courses> getUnselectedElectiveCourses() {
        Long studentId = SecurityUtil.getCurrentUserId();
        if (studentId == null) {
            throw new RuntimeException("用户未登录");
        }
        // 1. 缓存键
        String cacheKey = MyRedisKeyUtil.getCacheKey("UnselectedElectiveCourses", studentId.toString());
        String lockKey = MyRedisKeyUtil.getLockKey("UnselectedElectiveCoursesLock", studentId.toString());
        // 2. 尝试从缓存获取
        List<Courses> cached = (List<Courses>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        // 3. 缓存未命中，尝试加锁重建缓存
        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.lock(30, TimeUnit.SECONDS); // 自动续期，即使执行久也不会丢锁

            // 双重检查
            cached = (List<Courses>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }

            // 4. 查询数据库
            List<Courses> courses = queryUnselectedFromDB(studentId);

            // 5. 写入缓存（设置30分钟过期）
            redisTemplate.opsForValue().set(cacheKey, courses, 30, TimeUnit.MINUTES);

            return courses;

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    // 数据库查询未选课程（提取为私有方法）
    private List<Courses> queryUnselectedFromDB(Long studentId) {
        return coursesMapper.selectUnselectedCourses(studentId);
    }

    /**
     * 管理员新增选修课
     */
    @Override
    public void insertCourse(CourseDTO courseDTO) {
        Courses course = new Courses();
        BeanUtil.copyProperties(courseDTO, course);
        course.setRemainingQuota(course.getTotalQuota()); // 确保初始剩余 = 总名额

        coursesMapper.insert(course);

        // === Redis 同步开始 ===
        String infoKey = "course:info:" + course.getId();
        String quotaKey = "course:quota:" + course.getId();

        // 1. 缓存课程信息
        redisTemplate.opsForValue().set(infoKey, course, 30, TimeUnit.MINUTES);

        // 2. 初始化名额（用于预扣减）
        redisTemplate.opsForValue().set(quotaKey, course.getRemainingQuota(), 30, TimeUnit.MINUTES);

        // 3. 加入布隆过滤器
        courseIdBloomFilterService.addCourseId(course.getId());
        // 清理所有用户的“未选课程列表缓存
        String unselectedCachePattern = MyRedisKeyUtil.getCacheKey("UnselectedElectiveCourses", "*");
        Set<String> unselectedCacheKeys = redisTemplate.keys(unselectedCachePattern);
        if (unselectedCacheKeys != null && !unselectedCacheKeys.isEmpty()) {
            redisTemplate.delete(unselectedCacheKeys);
        }
        // 清理查询所有选修课程缓存
        String allCachePattern = MyRedisKeyUtil.getCacheKey("courses", "queryAll");
        Set<String> allCacheKeys = redisTemplate.keys(allCachePattern);
        if (allCacheKeys != null && !allCacheKeys.isEmpty()) {
            redisTemplate.delete(allCacheKeys);
        }
    }
    /**
     * 管理员修改选修课
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(CourseDTO courseDTO) {
        Courses oldCourse = coursesMapper.selectById(courseDTO.getId());
        if (oldCourse == null) throw new IllegalArgumentException("课程不存在");

        Courses newCourse = new Courses();
        BeanUtil.copyProperties(oldCourse, newCourse);
        BeanUtil.copyProperties(courseDTO, newCourse);

        int res = coursesMapper.updateById(newCourse);
        if (res != 1) throw new RuntimeException("更新失败");

        // === Redis 同步开始 ===
        String infoKey = "course:info:" + newCourse.getId();
        String quotaKey = "course:quota:" + newCourse.getId();

        redisTemplate.delete(infoKey); // 删除单个课程信息缓存
        if (!oldCourse.getRemainingQuota().equals(newCourse.getRemainingQuota())) {
            redisTemplate.delete(quotaKey); // 名额变更时删除名额缓存
        }
        String unselectedCachePattern = MyRedisKeyUtil.getCacheKey("UnselectedElectiveCourses", "*");
        Set<String> unselectedCacheKeys = redisTemplate.keys(unselectedCachePattern);
        if (unselectedCacheKeys != null && !unselectedCacheKeys.isEmpty()) {
            redisTemplate.delete(unselectedCacheKeys);
        }
        String selectedCachePattern = MyRedisKeyUtil.getCacheKey("select:course:ids", "*");
        Set<String> selectedCacheKeys = redisTemplate.keys(selectedCachePattern);
        if (selectedCacheKeys != null && !selectedCacheKeys.isEmpty()) {
            redisTemplate.delete(selectedCacheKeys);
        }
        // 清理查询所有选修课程缓存
        String allCachePattern = MyRedisKeyUtil.getCacheKey("courses", "queryAll");
        Set<String> allCacheKeys = redisTemplate.keys(allCachePattern);
        if (allCacheKeys != null && !allCacheKeys.isEmpty()) {
            redisTemplate.delete(allCacheKeys);
        }
    }
    @RabbitListener(queues = "course.select.queue")
    public void handleCourseSelection(CourseSelectionMessage message) {
        // 无需 Channel 和 Message 参数
        Long studentId = message.getStudentId();
        Long courseId = message.getCourseId();

        // 幂等检查
        LambdaQueryWrapper<StudentCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourse::getStudentId, studentId)
                .eq(StudentCourse::getCourseId, courseId);
        if (studentCourseMapper.selectCount(wrapper) > 0) {
            System.out.println("重复消息，已幂等处理");
            return; // 直接返回，Spring 自动 ACK
        }

        // 插入选课记录
        StudentCourse record = new StudentCourse();
        record.setStudentId(studentId);
        record.setCourseId(courseId);
        studentCourseMapper.insert(record);

        // 更新课程名额（兜底）
        LambdaUpdateWrapper<Courses> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("remaining_quota = remaining_quota - 1")
                .eq(Courses::getId, courseId)
                .gt(Courses::getRemainingQuota, 0);
        int update = coursesMapper.update(null, updateWrapper);
        if (update != 1) {
            throw new RuntimeException("选课失败：课程名额已满或不存在");
        }

        // 成功处理，Spring 自动 ACK
        // 无需调用 basicAck
    }
    @RabbitListener(queues = "course.drop.queue")
    public void handleCourseDrop(CourseDropMessage message) {
        Long studentId = message.getStudentId();
        Long courseId = message.getCourseId();

        // 幂等检查
        LambdaQueryWrapper<StudentCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourse::getStudentId, studentId)
                .eq(StudentCourse::getCourseId, courseId);
        StudentCourse record = studentCourseMapper.selectOne(wrapper);
        if (record == null) {
            System.out.println("退课消息幂等处理：记录已不存在");
            return;
        }

        // 删除记录
        studentCourseMapper.delete(wrapper);

        // 名额 +1
        LambdaUpdateWrapper<Courses> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("remaining_quota = remaining_quota + 1")
                .eq(Courses::getId, courseId);
        coursesMapper.update(null, updateWrapper);

        // Spring 自动 ACK
    }
}
