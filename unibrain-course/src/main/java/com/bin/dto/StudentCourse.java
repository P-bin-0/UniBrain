package com.bin.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生课程关系表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("student_course")
public class StudentCourse {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 学生ID
     */
    private Long studentId;
    /**
     * 课程ID
     */
    private Long courseId;
     /**
     * 选课时间
     */
    private LocalDateTime selectTime;
}
