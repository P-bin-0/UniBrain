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
     * 查询实验分析数据(根据批次id)
     * @return 实验分析数据
     */
    @GetMapping("/list")
    public List<AnalysisVO> list(@RequestParam("batchId")@NonNull String batchId) {
        return analysisService.getAnalysis(batchId);
    }
    /**
     * 调用大模型分析数据，判断实验数据是否有不合理的地方
     * @param batchId 实验人姓名
     * @return 分析结果
     */
    @GetMapping("/model")
    public String model(@RequestParam("batchId")@NonNull String batchId) {
        return analysisService.model(batchId);
    }
}
