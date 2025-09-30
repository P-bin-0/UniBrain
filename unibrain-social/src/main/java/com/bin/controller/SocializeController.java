package com.bin.controller;

import com.bin.dto.PageQueryDTO;
import com.bin.dto.PageResult;
import com.bin.dto.SocializeDTO;
import com.bin.dto.vo.SocializeVO;
import com.bin.response.ApiResponse;
import com.bin.service.SocializeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * 社交控制器
 */
@Validated
@RestController
@RequestMapping("/api/socialize")
public class SocializeController {

    @Autowired
    private SocializeService socializeService;

    /**
     * 发表评论
     */
    @PostMapping("/comment")
    public ApiResponse<SocializeVO> publishComment(@RequestBody SocializeDTO socializeDTO) {
        socializeService.insertComment(socializeDTO);
        return ApiResponse.success();
    }
    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{id}")
    public ApiResponse<SocializeVO> deleteComment(@PathVariable("id") Long id) {
        socializeService.removeCommentById(id);
        return ApiResponse.success();
    }
    /**
     * 根据关键词进行搜索
     */
    @GetMapping("/search")
    public ApiResponse<PageResult<SocializeVO>> searchComment(@RequestParam("keyword") String keyword,
                                                              @Validated PageQueryDTO pageQueryDTO) {
        PageResult<SocializeVO> voList = socializeService.searchComment(keyword, pageQueryDTO);
        return ApiResponse.success(voList);
    }
    // 按评论ID搜索评论
    @GetMapping("/search/comment")
    public ApiResponse<SocializeVO> searchByCommentId(@RequestParam("id") Long id) {
        SocializeVO vo = socializeService.searchByCommentIdScrollInit(id);
        return ApiResponse.success(vo);
    }

    // 按目标ID搜索评论
    @GetMapping("/search/target")
    public ApiResponse<PageResult<SocializeVO>> searchByTargetId(@RequestParam("targetId") Long targetId,
                                                           @Validated PageQueryDTO pageQueryDTO) {
        PageResult<SocializeVO> voList = socializeService.searchByTargetId(targetId, pageQueryDTO);
        return ApiResponse.success(voList);
    }
    // 查询所有评论
    @GetMapping("/all")
    public ApiResponse<PageResult<SocializeVO>> getAllComments(@Validated PageQueryDTO pageQueryDTO) {
        PageResult<SocializeVO> voList = socializeService.selectAll(pageQueryDTO);
        return ApiResponse.success(voList);
    }
}