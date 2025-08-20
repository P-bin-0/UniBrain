package com.bin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bin.dto.Courses;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CoursesMapper extends BaseMapper<Courses> {
    /**
     * 根据ID更新课程状态
     * @param id 课程ID
     * @param isActive 新的状态值
     */
    @Update("update courses set is_active = #{isActive} where id = #{id} and course_type = '选修'")
    int updateCourseById(@Param("id") Long id, @Param("isActive") Boolean isActive);
}
