package com.bin.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.dto.Courses;
import com.bin.mapper.CoursesMapper;
import com.bin.service.CoursesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoursesServiceImpl extends ServiceImpl<CoursesMapper, Courses> implements CoursesService {
    @Autowired
    private CoursesMapper coursesMapper;

    /**
     * 根据ID更新课程状态
     * @param id 课程ID
     * @param isActive 新的状态值
     */
    @Override
    public void updateCourseById(Long id, Boolean isActive) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        Courses courses = coursesMapper.selectById(id);
        if (courses == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        int count = coursesMapper.updateCourseById(id, isActive);
        if (count != 1) {
            throw new RuntimeException("更新失败");
        }
    }

    /**
     * 查询所有已选选修课
     */
    @Override
    public List<Courses> getIsActiveTrue() {
        LambdaQueryWrapper<Courses> wrapper = new LambdaQueryWrapper<Courses>()
                .eq(Courses::getIsActive, true)
                .eq(Courses::getCourseType, "选修");
        return coursesMapper.selectList(wrapper);
    }

    /**
     * 查询所有未选选修课
     */
    @Override
    public List<Courses> getIsActiveFalse() {
        LambdaQueryWrapper<Courses> wrapper = new LambdaQueryWrapper<Courses>()
                .eq(Courses::getIsActive, false)
                .eq(Courses::getCourseType, "选修");
        return coursesMapper.selectList(wrapper);
    }
}
