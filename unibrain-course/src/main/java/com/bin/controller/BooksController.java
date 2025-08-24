package com.bin.controller;

import com.bin.dto.vo.BooksPageQuery;
import com.bin.dto.vo.BooksVo;
import com.bin.dto.vo.PageResult;
import com.bin.response.ApiResponse;
import com.bin.service.BooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课程相关书籍Controller
 * @author bin
 */
@RestController
@RequestMapping("/api/books")
public class BooksController {
    @Autowired
    private BooksService booksService;
    /**
     * 分页查询书籍
     * @return 书籍列表
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<BooksVo>> pageQuery(BooksPageQuery booksPageQuery) {
        PageResult<BooksVo> pageResult = booksService.pageQuery(booksPageQuery);
        return ApiResponse.success(pageResult);
    }

}
