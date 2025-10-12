package com.bin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bin.dto.Courses;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CoursesMapper extends BaseMapper<Courses> {
    /**
     * 根据ID更新课程状态
     * @param id 课程ID
     * @param isActive 新的状态值
     */
    @Update("update courses set is_active = #{isActive} where id = #{id} and course_type = '选修'")
    int updateCourseById(@Param("id") Long id, @Param("isActive") Boolean isActive);

    /**
     * 查询用户已选选修课（数据库）
     * @param studentId 用户ID
     * @return 已选选修课列表
     */
    @Select("SELECT c.* FROM courses c " +
            "INNER JOIN student_course sc ON c.id = sc.course_id " +
            "WHERE sc.student_id = #{studentId} " +
            "AND c.course_type = '选修' " +
            "AND c.is_active = true")
    List<Courses> selectElectiveCoursesByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询用户未选选修课（数据库）
     * @param studentId 用户ID
     * @return 未选选修课列表
     */
    @Select("SELECT * FROM courses " +
            "WHERE course_type = '选修' " +
            "  AND is_active = true " +
            "  AND NOT EXISTS (" +
            "    SELECT 1 FROM student_course sc " +
            "    WHERE sc.student_id = #{studentId} " +
            "      AND sc.course_id = courses.id" +
            ")")
    List<Courses> selectUnselectedCourses(Long studentId);
}
