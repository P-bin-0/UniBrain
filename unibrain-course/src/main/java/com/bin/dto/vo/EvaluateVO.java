package com.bin.dto.vo;

import lombok.Data;

/**
 * 评价课程VO
 * @author bin
 */
@Data
public class EvaluateVO {
    private Long id; /** 评价id */
    private String userName; /** 评价人 */
    private String coursesName; /** 评价课程 */
    private Integer score; /** 评价分数 */
    private String context; /** 评价描述 */
    private String semester; /** 评价学期 */
    private Boolean isEvaluate; /** 是否评价（0：未评价；1：已评价） */
}
