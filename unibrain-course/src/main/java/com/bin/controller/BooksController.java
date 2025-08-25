package com.bin.controller;

import com.bin.dto.Books;
import com.bin.dto.vo.BooksPageQuery;
import com.bin.dto.vo.BooksVo;
import com.bin.dto.vo.PageResult;
import com.bin.response.ApiResponse;
import com.bin.service.BooksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程相关书籍Controller
 * @author bin
 */
@RestController
@RequestMapping("/api/books")
public class BooksController {
    private static final Logger log = LoggerFactory.getLogger(BooksController.class);
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
    /**
     * 根据id查询书籍
     */
    @GetMapping("/{id}")
    public ApiResponse<Books> getById(@PathVariable("id") Long id) {
        // 校验id是否为空
        if (id == null) {
            return ApiResponse.error(400, "查询失败，未指定要查询的书籍");
        }
        Books books = booksService.getBookById(id);
        return ApiResponse.success(books);
    }
    /**
     * 添加书籍
     */
    @PostMapping("/add")
    public ApiResponse<Books> add(@RequestBody Books books) {
        booksService.addBook(books);
        return ApiResponse.success();
    }
    /**
     * 删除书籍(批量或单都可)
     */
    @DeleteMapping("/delete/{ids}")
    public ApiResponse<Books> delete(@PathVariable("ids") List<Long> ids) {
        // 校验ids是否为空
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.error(400, "删除失败，未指定要删除的书籍");
        }
        int counts =booksService.deleteByIds(ids);
        log.info("删除了{}本书籍", counts);
        return ApiResponse.success();
    }
    /**
     * 更新书籍
     */
    @PutMapping("/update")
    public ApiResponse<Books> update(@RequestBody Books books) {
        booksService.updateBook(books);
        return ApiResponse.success();
    }
}
