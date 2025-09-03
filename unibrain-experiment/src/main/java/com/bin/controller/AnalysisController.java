package com.bin.controller;

import com.bin.dto.vo.AnalysisVO;
import com.bin.service.AnalysisService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 实验分析controller
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    @Autowired
    private AnalysisService analysisService;
    /**
     * 查询实验分析数据(根据实验人姓名)
     * @param name 实验人姓名
     * @return 实验分析数据
     */
    @GetMapping("/list")
    public List<AnalysisVO> list(@RequestParam("name")@NonNull String name) {
        return analysisService.getByName(name);
    }
}
