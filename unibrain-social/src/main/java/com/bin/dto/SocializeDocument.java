package com.bin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "comment")  // ES 索引名
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocializeDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long userId;

    // 存储真实用户名
    @Field(type = FieldType.Keyword)
    private String userName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Date,
    format = DateFormat.date_hour_minute_second)
    private LocalDateTime createAt;

    @Field(type = FieldType.Long)
    private Long likeCount;

    @Field(type = FieldType.Long)
    private Long contentCount;

    @Field(type = FieldType.Long)
    private Long forwardCount;

    @Field(type = FieldType.Long)
    private Long targetId;

    @Field(type = FieldType.Long)
    private Long parentId;
}
