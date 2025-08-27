package com.bin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bin.dto.Grades;
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
    List<GradesVO> getByUserName(String userName);
}
