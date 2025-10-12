package com.bin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bin.dto.CourseDTO;
import com.bin.dto.Courses;

import java.util.List;

public interface CoursesService extends IService<Courses> {

    /**
     * 根据ID更新课程状态
     * @param id 课程ID
     * @param isActive 新的状态值
     */
    void updateCourseById(Long id, Boolean isActive);

    /**
     * 查询所有已选选修课
     */
    List<Courses> getIsActiveTrue();

    /**
     * 查询所有未选选修课
     */
    List<Courses> getUnselectedElectiveCourses();
     /**
      * 学生选课
      */
    void addCourseById(Long id);
    /**
     * 学生退课
     */
    void deleteCourseById(Long id);

     /**
      * 管理员新增选修课
      */
    void insertCourse(CourseDTO courseDTO);

     /**
      * 管理员修改选修课
      */
    void updateCourse(CourseDTO courseDTO);
}
