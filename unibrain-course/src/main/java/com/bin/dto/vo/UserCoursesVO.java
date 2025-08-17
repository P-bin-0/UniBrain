package com.bin.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户课程VO
 * @author P-bin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCoursesVO {
    /**
     * 课表ID
     */
    private Long id;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 课程名
     */
    private String coursesName;
    /**
     * 星期
     */
    private String week;
    /**
     * 日课时
     */
    private Integer dayHour;
    /**
     * 教室
     */
    private String room;
    /**
     * 教师
     */
    private String teacher;
}
