package com.bin.controller;

import com.bin.dto.SocializeDTO;
import com.bin.dto.vo.SocializeVO;
import com.bin.response.ApiResponse;
import com.bin.service.SocializeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * 社交控制器
 */
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
    public ApiResponse<List<SocializeVO>> searchComment(@RequestParam("keyword") String keyword,
                                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                                  @RequestParam(value = "size", defaultValue = "10") int size) {
        List<SocializeVO> voList = socializeService.searchComment(keyword, page, size);
        return ApiResponse.success(voList);
    }
    // 按用户ID搜索评论
    @GetMapping("/search/user")
    public ApiResponse<List<SocializeVO>> searchByUserId(@RequestParam("userId") Long userId,
                                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                                         @RequestParam(value = "size", defaultValue = "10") int size) {
        List<SocializeVO> voList = socializeService.searchByUserId(userId, page, size);
        return ApiResponse.success(voList);
    }

    // 按目标ID搜索评论
    @GetMapping("/search/target")
    public ApiResponse<List<SocializeVO>> searchByTargetId(@RequestParam("targetId") Long targetId,
                                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "10") int size) {
        List<SocializeVO> voList = socializeService.searchByTargetId(targetId, page, size);
        return ApiResponse.success(voList);
    }
}