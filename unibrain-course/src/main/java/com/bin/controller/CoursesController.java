package com.bin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bin.dto.Courses;
import com.bin.response.ApiResponse;
import com.bin.service.CoursesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author P-bin
 * 选课管理，对选修课的增删改查
 */
@RestController
@RequestMapping("/api/courses")
public class CoursesController {
    @Autowired
    private CoursesService coursesService;
    /**
     * 新增选修课（修改is_active字段，true为已选，false为未选）
     */
    @PreAuthorize("isAuthenticated()")
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
     * 删除选修课（修改is_active字段，true为已选，false为未选）
     */
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/queryAll")
    public ApiResponse<List<Courses>> queryAllCourses() {
        List<Courses> list = coursesService.list(new LambdaQueryWrapper<Courses>()
                .eq(Courses::getCourseType, "选修"));
        return ApiResponse.success(list);
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
        List<Courses> isActiveFalse = coursesService.getIsActiveFalse();
        return ApiResponse.success(isActiveFalse);
    }
}