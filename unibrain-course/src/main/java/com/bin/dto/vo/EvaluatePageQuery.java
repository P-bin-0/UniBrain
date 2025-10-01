package com.bin.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 评价课程分页查询
 * @author bin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluatePageQuery {
    private String semester; /** 评价学期 */
    private Boolean isEvaluate; /** 是否评价（0：未评价；1：已评价） */
}
