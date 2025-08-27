package com.bin.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成绩VO类
 * @author bin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GradesVO {
    private Long id; // 主键
    private String userName; // 用户名
    private String courseName; // 课程名
    private String teacher; // 教师名
    private Integer grade; // 成绩
    private String semester; // 学期
}
