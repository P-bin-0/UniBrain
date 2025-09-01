package com.bin.controller;

import com.bin.dto.SummaryRequest;
import com.bin.service.SummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    @Autowired
    private SummaryService summaryService;

    /**
     * 生成摘要
     * @param request 摘要请求参数
     * @return 摘要响应结果
     */
    @PostMapping("/generate")
    public String generateSummary(@RequestBody SummaryRequest request) {
        return summaryService.generateSummary(request.getText());
    }

}
