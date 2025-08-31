package com.bin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 学生学期平均成绩DTO
 * @author bin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentSemesterAvgDTO {
    private String userName;
    private String semester;
    private BigDecimal avgGrade;
}
