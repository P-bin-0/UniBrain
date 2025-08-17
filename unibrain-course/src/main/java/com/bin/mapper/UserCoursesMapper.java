package com.bin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bin.dto.UserCourses;
import com.bin.dto.vo.UserCoursesVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserCoursesMapper extends BaseMapper<UserCourses> {
    List<UserCoursesVO> getShowUserCourses();
}
