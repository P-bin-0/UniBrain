package com.bin.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 社交内容视图对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocializeVO {
    private Long id; // 主键
    private String userName; // 用户id
    private String content; // 内容
    private LocalDateTime createAt; // 创建时间
    private Long likeCount; // 点赞数
    private Long contentCount; // 评论数
    private Long forwardCount; // 转发数
    private Long targetId; // 目标id
    private Long parentId; // 父id
    // 层级字段：存储当前评论的子评论
    private List<SocializeVO> children = new ArrayList<>(); // 初始化避免前端null
}
