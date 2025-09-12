package com.bin.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 社交实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "socialize")
public class Socialize {
    @TableId(type = IdType.AUTO)
    private Long id; // 主键
    private Long userId; // 用户id
    private String content; // 内容
    private LocalDateTime createAt; // 创建时间
    private Long likeCount; // 点赞数
    private Long contentCount; // 评论数
    private Long forwardCount; // 转发数
    private Long targetId; // 目标id
    private Long parentId; // 父id
}