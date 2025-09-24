package com.bin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bin.dto.Socialize;
import com.bin.dto.SocializeDTO;
import com.bin.dto.vo.SocializeVO;

import java.util.List;


/**
 * 社交服务接口
 */
public interface SocializeService extends IService<Socialize> {

    /**
     * 发表评论
     */
    void insertComment(SocializeDTO socializeDTO);

    /**
     * 删除评论
     */
    void removeCommentById(Long id);

    /**
     * 搜索评论
     */
    List<SocializeVO> searchComment(String keyword, int page, int size);

    /**
     * 按用户ID搜索
     */
    List<SocializeVO> searchByUserId(Long userId, int page, int size);

    /**
     * 按目标ID搜索（如某篇文章的所有评论）
     */
    List<SocializeVO> searchByTargetId(Long targetId, int page, int size);

    /**
     * 查询所有评论
     */
    List<SocializeVO> selectAll(int page, int size);
}