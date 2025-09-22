package com.bin.controller;

import com.bin.dto.StudentSemesterAvgDTO;
import com.bin.dto.vo.GradesVO;
import com.bin.service.GradesService;
import org.springframework.beans.factory.annotation.Autowired;
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
    /**
     * 查询成绩（根据用户名）
     * @param userName 用户名
     * @return 成绩列表
     */
    @GetMapping("/getByUserName")
    public List<GradesVO> getByUserName(@RequestParam("userName") String userName,
                                         @RequestParam(value = "semester", required = false) String semester) {
        return gradesService.getByUserName(userName, semester);
    }
    /**
     * 查询用户每个学期的平均成绩（根据用户名查询）
     */
    @GetMapping("/getAVG")
    public List<StudentSemesterAvgDTO> getAverageGradesByUserName(@RequestParam("userName") String userName) {
        return gradesService.getAverageGradesByUserName(userName);
    }
    /**
     * 分析用户成绩
     * @param userName 用户名
     * @return 分析报告
     */
    @GetMapping("/ai/analyze")
    public String analyzeScoresWithAi(@RequestParam("userName") String userName) {
        return gradesService.analyzeScoresWithAi(userName);
    }
}
