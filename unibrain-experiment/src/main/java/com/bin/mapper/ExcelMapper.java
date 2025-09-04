package com.bin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bin.dto.Analysis;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件上传Mapper
 */
@Mapper
public interface ExcelMapper extends BaseMapper<Analysis> {
}
