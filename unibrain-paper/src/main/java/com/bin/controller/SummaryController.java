package com.bin.controller;

import com.bin.dto.SummaryRequest;
import com.bin.util.SummaryGenerator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final SummaryGenerator summaryGenerator;

    public SummaryController(SummaryGenerator summaryGenerator) {
        this.summaryGenerator = summaryGenerator;
    }

    /**
     * 生成摘要
     * @param request 摘要请求参数
     * @return 摘要响应结果
     */
    @PostMapping
    public String generateSummary(@RequestBody SummaryRequest request) {
        try {
            return summaryGenerator.generateSummary(request.getText());
        } catch (Exception e) {
            return "生成摘要失败：" + e.getMessage();
        }
    }

}
