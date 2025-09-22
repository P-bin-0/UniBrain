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
    private String userName; /** 评价人 */
    private String semester; /** 评价学期 */
}
