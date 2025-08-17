package com.bin.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author P-bin
 * @date 2023/4/10 15:22
 * @description 用户&&课程关系表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user_courses")
public class UserCourses {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long coursesId;
    private String week;
    private Integer dayHour;
    private String room;
    private String teacher;
}
