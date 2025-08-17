package com.bin.controller;

import com.bin.dto.UserCourses;
import com.bin.dto.vo.UserCoursesVO;
import com.bin.response.ApiResponse;
import com.bin.response.NoWrap;
import com.bin.service.UserCoursesService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 用户课表（该模块总共实现两个接口，一个是展示用户的课表，一个是导出课表为Excel文件
 */
@RestController
@RequestMapping("/api/userCourses")
public class UserCoursesController {
    private final Logger log = LoggerFactory.getLogger(UserCoursesController.class);
    /**
     * 展示用户的课表
     */
    @Autowired
    private UserCoursesService userCoursesService;
    @GetMapping("/show")
    public ApiResponse<List<UserCoursesVO>> showUserCourses() {
        // 从数据库查询用户的课表
        List<UserCoursesVO> userCourses = userCoursesService.getUserCourses();
        if(userCourses == null) {
            return ApiResponse.error(500, "用户课表为空");
        }
        return ApiResponse.success(userCourses);
    }
    /**
     * 导出用户的课表为Excel文件
     */
    @GetMapping("/export")
    @NoWrap
    public void exportSchedule(HttpServletResponse response) {
        try {
            // 调用服务层方法（服务层已处理业务异常并设置响应）
            userCoursesService.exportScheduleToExcel(response);
        } catch (IOException e) {
            // 仅处理IO相关异常，设置JSON响应
            response.setContentType("application/json;charset=UTF-8");
            try {
                // 直接向响应流写入错误信息
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
            } catch (IOException ex) {
                // 记录最终异常，但不再向上抛出（避免响应已提交）
                log.error("导出响应写入失败", ex);
            }
        }
    }
}