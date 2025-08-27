package com.bin.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成绩实体类
 * @author bin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("grades")
public class Grades {
    @TableId(type = IdType.AUTO)
    private Long id; // 主键
    private String userName; // 用户名
    private String courseName; // 课程名
    private String teacher; // 教师名
    private Integer grade; // 成绩
    private String semester; // 学期
}
