package com.bin.controller;

import com.bin.Service.impl.UserLoginService;
import com.bin.dto.StudentSemesterAvgDTO;
import com.bin.dto.vo.GradesVO;
import com.bin.service.GradesService;
import com.bin.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 成绩Controller
 * @author bin
 */
@RestController
@RequestMapping("/api/grades")
public class GradesController {
    @Autowired
    private GradesService gradesService;
    @Autowired
    private UserLoginService userLoginService;
    /**
     * 查询成绩（根据用户名）
     * @return 成绩列表
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getByUserName")
    public List<GradesVO> getByUserName(@RequestParam(value = "semester", required = false) String semester) {
        // 获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        // 根据ID获取当前登录用户的昵称
        String userName = userLoginService.selectById(userId).getUsername();
        return gradesService.getByUserName(userName, semester);
    }
    /**
     * 查询用户每个学期的平均成绩（根据用户名查询）
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getAVG")
    public List<StudentSemesterAvgDTO> getAverageGradesByUserName() {
        // 获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        // 根据ID获取当前登录用户的昵称
        String userName = userLoginService.selectById(userId).getUsername();
        return gradesService.getAverageGradesByUserName(userName);
    }
    /**
     * 分析用户成绩
     * @return 分析报告
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/ai/analyze")
    public String analyzeScoresWithAi() {
        // 获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        // 根据ID获取当前登录用户的昵称
        String userName = userLoginService.selectById(userId).getUsername();
        return gradesService.analyzeScoresWithAi(userName);
    }
}
