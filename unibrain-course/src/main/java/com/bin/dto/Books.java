package com.bin.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;

/**
 * @author bin
 * 课程相关书籍
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("books")
public class Books implements Serializable {
    /**
     * 资源唯一标识符
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 作者
     */
    private String author;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 出版年份
     */
    private Year publicationYear;

    /**
     * 版次 (如：第1版, 第2版修订版)
     */
    private String edition;

    /**
     * 学科
     */
    private String subject;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 单价（元）
     */
    private BigDecimal price;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;
}
