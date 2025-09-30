package com.bin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bin.dto.PageQueryDTO;
import com.bin.dto.PageResult;
import com.bin.dto.Socialize;
import com.bin.dto.SocializeDTO;
import com.bin.dto.vo.SocializeVO;

import java.io.IOException;
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
    PageResult<SocializeVO> searchComment(String keyword, PageQueryDTO pageQueryDTO);

    /**
     * 按用户ID搜索
     */
    SocializeVO searchByCommentIdScrollInit(Long id);

    /**
     * 按目标ID搜索（如某篇文章的所有评论）
     */
    PageResult<SocializeVO> searchByTargetId(Long targetId, PageQueryDTO pageQueryDTO);

    /**
     * 查询所有评论
     */
    PageResult<SocializeVO> selectAll(PageQueryDTO pageQueryDTO);
}