package com.bin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bin.dto.Grades;
import com.bin.dto.StudentSemesterAvgDTO;
import com.bin.dto.vo.GradesVO;

import java.util.List;

/**
 * 成绩Service
 */
public interface GradesService extends IService<Grades> {
    /**
     * 根据用户名查询成绩
     * @param userName 用户名
     * @return 成绩列表
     */
    List<GradesVO> getByUserName(String userName, String semester);

    /**
     * 查询用户每个学期的平均成绩（根据用户名查询）
     * @param userName 用户名
     * @return 成绩列表
     */
    List<StudentSemesterAvgDTO> getAverageGradesByUserName(String userName);

    /**
     * 分析用户成绩
     * @param userName 用户名
     * @return 分析报告
     */
    String analyzeScoresWithAi(String userName);
}
