package com.bin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bin.dto.CourseDTO;
import com.bin.dto.Courses;
import com.bin.response.ApiResponse;
import com.bin.service.CoursesService;
import com.bin.util.MyRedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author P-bin
 * 选课管理，对选修课的增删改查
 */
@RestController
@RequestMapping("/api/courses")
public class CoursesController {
    @Autowired
    private CoursesService coursesService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 管理员启用选修课（修改is_active字段，true为启用，false为禁用）
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PostMapping("/add")
    public ApiResponse<Courses> addCourse(@RequestParam("id") Long id) {
        try {
            coursesService.updateCourseById(id, true);
            return ApiResponse.success();
        } catch (IllegalArgumentException e) {
            //处理业务逻辑异常
            return ApiResponse.error(400, e.getMessage());
        } catch (RuntimeException e) {
            //处理其他运行时异常
            return ApiResponse.error(500, e.getMessage());
        }
    }
    /**
     * 管理员删除选修课（修改is_active字段为false）
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PostMapping("/delete")
    public ApiResponse<Courses> deleteCourse(@RequestParam("id") Long id) {
        try {
            coursesService.updateCourseById(id, false);
            return ApiResponse.success();
        } catch (IllegalArgumentException e) {
            //处理业务逻辑异常
            return ApiResponse.error(400, e.getMessage());
        } catch (RuntimeException e) {
            //处理其他运行时异常
            return ApiResponse.error(500, e.getMessage());
        }
    }
    /**
     * 查询所有选修课
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @GetMapping("/queryAll")
    public ApiResponse<List<Courses>> queryAllCourses() {
        // 添加redis缓存，key为"courses:queryAll"，过期时间为10分钟
        String cacheKey = MyRedisKeyUtil.getCacheKey("courses", "queryAll");
        try {
            // 先从Redis缓存中获取数据
            List<Courses> cachedList = (List<Courses>) redisTemplate.opsForValue().get(cacheKey);

            // 检查缓存是否命中
            if (cachedList != null) {
                // 缓存命中，直接返回缓存数据
                return ApiResponse.success(cachedList);
            }

            // 缓存未命中，从数据库查询所有选修课
            List<Courses> list = coursesService.list(new LambdaQueryWrapper<Courses>()
                    .eq(Courses::getCourseType, "选修"));

            // 将查询结果存入Redis缓存
            // 使用String类型存储整个List对象，设置10分钟过期时间
            redisTemplate.opsForValue().set(cacheKey, list, 10, TimeUnit.MINUTES);

            // 返回查询结果
            return ApiResponse.success(list);

        } catch (Exception e) {
            // Redis操作异常处理：降级到数据库查询
            // 记录错误日志，但不影响主要业务功能
            System.err.println("Redis缓存操作异常，降级到数据库查询: " + e.getMessage());

            // 直接查询数据库，保证服务可用性
            List<Courses> list = coursesService.list(new LambdaQueryWrapper<Courses>()
                    .eq(Courses::getCourseType, "选修"));
            return ApiResponse.success(list);
        }
    }
    /**
     * 查询所有已选选修课
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/queryIsActiveTrue")
    public ApiResponse<List<Courses>> querySelectedCourses() {
        List<Courses> isActiveTrue = coursesService.getIsActiveTrue();
        return ApiResponse.success(isActiveTrue);
    }
    /**
     * 查询所有未选选修课
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/queryIsActiveFalse")
    public ApiResponse<List<Courses>> queryUnselectedCourses() {
        List<Courses> isActiveFalse = coursesService.getUnselectedElectiveCourses();
        return ApiResponse.success(isActiveFalse);
    }
    /**
     * 学生选课
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/addStudentCourse")
    public ApiResponse<String> addStudentCourse(@RequestParam("id") Long id) {
        try {
            coursesService.addCourseById(id);
            return ApiResponse.success("选课成功");
        } catch (IllegalArgumentException e) {
            //处理业务逻辑异常
            return ApiResponse.error(400, e.getMessage());
        } catch (RuntimeException e) {
            //处理其他运行时异常
            return ApiResponse.error(500, e.getMessage());
        }
    }
    /**
     * 学生退课
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/deleteStudentCourse")
    public ApiResponse<String> deleteStudentCourse(@RequestParam("id") Long id) {
        try {
            coursesService.deleteCourseById(id);
            return ApiResponse.success("退课成功");
        } catch (IllegalArgumentException e) {
            //处理业务逻辑异常
            return ApiResponse.error(400, e.getMessage());
        } catch (RuntimeException e) {
            //处理其他运行时异常
            return ApiResponse.error(500, e.getMessage());
        }
    }
    /**
     * 管理员新增选修课
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PostMapping("/addAdminCourse")
    public ApiResponse<String> addAdminCourse(@RequestBody CourseDTO courseDTO) {
        try {
            coursesService.insertCourse(courseDTO);
            return ApiResponse.success("新增选修课成功");
        } catch (IllegalArgumentException e) {
            //处理业务逻辑异常
            return ApiResponse.error(400, e.getMessage());
        } catch (RuntimeException e) {
            //处理其他运行时异常
            return ApiResponse.error(500, e.getMessage());
        }
    }
    /**
     * 管理员修改选修课
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @PostMapping("/updateAdminCourse")
    public ApiResponse<String> updateAdminCourse(@RequestBody CourseDTO courseDTO) {
        try {
            coursesService.updateCourse(courseDTO);
            return ApiResponse.success("修改选修课成功");
        } catch (IllegalArgumentException e) {
            //处理业务逻辑异常
            return ApiResponse.error(400, e.getMessage());
        } catch (RuntimeException e) {
            //处理其他运行时异常
            return ApiResponse.error(500, e.getMessage());
        }
    }


}