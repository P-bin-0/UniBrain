package com.bin.dto.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;

/**
 * @author bin
 * 课程相关书籍VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BooksVo {
    private Long id;//资源唯一标识符
    private String name;//资源名称
    private String author;//作者
    private String publisher;//出版社
    private Year publicationYear;//出版年份
    private String edition;//版次
    private String subject;//学科
    private String courseName;//课程名称
    private BigDecimal price;//单价
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;//创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;//更新时间
}
