package com.bin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bin.dto.Books;
import com.bin.dto.vo.BooksPageQuery;
import com.bin.dto.vo.BooksVo;
import com.bin.dto.vo.PageResult;

import java.util.List;

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

    /**
     * 添加书籍
     * @param books 书籍信息
     */
    void addBook(Books books);

    /**
     * 删除书籍(批量或单都可)
     * @param ids 书籍id列表
     * @return 删除成功的书籍数量
     */
    int deleteByIds(List<Long> ids);

    /**
     * 更新书籍
     * @param books 书籍信息
     */
    void updateBook(Books books);

    /**
     * 根据id查询书籍
     * @param id 书籍id
     * @return 书籍信息
     */
    Books getBookById(Long id);
}
