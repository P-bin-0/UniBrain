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
}