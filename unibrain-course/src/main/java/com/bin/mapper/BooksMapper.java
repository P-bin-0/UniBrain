package com.bin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bin.dto.Books;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程相关书籍Mapper
 */
@Mapper
public interface BooksMapper extends BaseMapper<Books> {
}
