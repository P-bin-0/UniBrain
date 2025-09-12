package com.bin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 社交内容数据传输对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocializeDTO {
    private Long id; // 主键
    private Long userId; // 用户id
    private String content; // 内容
    private Long likeCount; // 点赞数
    private Long contentCount; // 评论数
    private Long forwardCount; // 转发数
    private Long targetId; // 目标id
    private Long parentId; // 父id
}
