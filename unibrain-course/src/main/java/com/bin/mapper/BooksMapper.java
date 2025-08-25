package com.bin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bin.dto.Books;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 课程相关书籍Mapper
 */
@Mapper
public interface BooksMapper extends BaseMapper<Books> {
    /**
     * 根据名称查询书籍
     * @param name 书籍名称
     * @return id
     */
    @Select("select * from books where name = #{name}")
    Books selectByName(String name);
}
