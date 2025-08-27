package com.bin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bin.dto.Grades;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成绩Mapper接口
 * @author bin
 */
@Mapper
public interface GradesMapper extends BaseMapper<Grades> {
}
