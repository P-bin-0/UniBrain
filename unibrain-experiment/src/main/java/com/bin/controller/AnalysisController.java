package com.bin.controller;

import com.bin.dto.vo.AnalysisVO;
import com.bin.service.AnalysisService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    /**
     * 调用大模型分析数据，判断实验数据是否有不合理的地方
     * @param name 实验人姓名
     * @return 分析结果
     */
    @GetMapping("/model")
    public String model(@RequestParam("name")@NonNull String name) {
        return analysisService.model(name);
    }
}
