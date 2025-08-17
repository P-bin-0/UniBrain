package com.bin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bin.dto.UserCourses;
import com.bin.dto.vo.UserCoursesVO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface UserCoursesService extends IService<UserCourses> {
    /**
     * 获取用户的课表
     * @return 用户的课表
     */
    List<UserCoursesVO> getUserCourses();
    /**
     * 导出用户的课表为Excel文件
     */
    void exportScheduleToExcel(HttpServletResponse response) throws IOException;
}
