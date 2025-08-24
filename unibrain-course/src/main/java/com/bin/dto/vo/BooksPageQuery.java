package com.bin.dto.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.Year;

/**
 * 课程相关书籍分页查询参数
 * @author bin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BooksPageQuery {
    private String name;
    private String publisher;
    private Year publicationYear;
    private String edition;
    private String subject;
    private String courseName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;
    private Long pageNum = 1L; //当前页码
    private Long pageSize = 10L; //每页记录数
}
