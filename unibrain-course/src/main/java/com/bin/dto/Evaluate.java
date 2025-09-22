package com.bin.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评价课程
 * @author bin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("evaluate")
public class Evaluate {
    @TableId(type = IdType.AUTO)
    private Long id; /** 评价id */
    private String userName; /** 评价人 */
    private String coursesName; /** 评价课程 */
    private Integer score; /** 评价分数 */
    private String context; /** 评价描述 */
    private String semester; /** 评价学期 */
    private Boolean isEvaluate; /** 是否评价（0：未评价；1：已评价） */
}
