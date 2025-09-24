package com.bin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bin.dto.Analysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文件上传Mapper
 */
@Mapper
public interface ExcelMapper extends BaseMapper<Analysis> {
}
