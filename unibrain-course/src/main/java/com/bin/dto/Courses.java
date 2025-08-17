package com.bin.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author bin
 * @date 2023/4/10 15:22
 * @description 课程
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("courses")
public class Courses {
    /**
     * 课程唯一标识符 (bigint)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程代码 (唯一标识) (varchar(20))
     */
    private String courseCode;

    /**
     * 课程名称 (varchar(255))
     */
    private String name;

    /**
     * 学分 (decimal(3,1))
     */
    private BigDecimal credits;

    /**
     * 总学时 (int)
     */
    private Integer totalHours;

    /**
     * 理论讲授学时 (int)
     */
    private Integer lectureHours;

    /**
     * 实验/上机学时 (int)
     */
    private Integer labHours;

    /**
     * 实践/实习学时 (int)
     */
    private Integer practiceHours;

    /**
     * 课程级别 (本科/硕士/博士) (enum)
     */
    private String courseLevel;

    /**
     * 课程类型 (必修/选修/核心/通识/研究) (enum)
     */
    private String courseType;

    /**
     * 所属院系ID (bigint)
     */
    private Long departmentId;

    /**
     * 所属学院ID (bigint)
     */
    private Long schoolId;

    /**
     * 课程是否启用 (TRUE=启用, FALSE=停用) (tinyint(1))
     */
    private Boolean isActive;

    /**
     * 记录创建时间 (timestamp)
     */
    private LocalDateTime createdAt;

    /**
     * 记录最后更新时间 (timestamp)
     */
    private LocalDateTime updatedAt;
}
