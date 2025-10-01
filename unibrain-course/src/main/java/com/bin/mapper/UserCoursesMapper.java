package com.bin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bin.dto.UserCourses;
import com.bin.dto.vo.UserCoursesVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserCoursesMapper extends BaseMapper<UserCourses> {
    /**
     * 获取用户的课表
     * @param userId 用户ID
     * @return 用户的课表
     */
    List<UserCoursesVO> getShowUserCourses(Long userId);
}
