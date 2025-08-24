package com.bin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bin.dto.Books;
import com.bin.dto.vo.BooksPageQuery;
import com.bin.dto.vo.BooksVo;
import com.bin.dto.vo.PageResult;

/**
 * 课程相关书籍Service
 */
public interface BooksService extends IService<Books> {
    /**
     * 分页查询书籍
     * @param booksPageQuery 书籍分页查询参数
     * @return 书籍列表
     */
    PageResult<BooksVo> pageQuery(BooksPageQuery booksPageQuery);
}
