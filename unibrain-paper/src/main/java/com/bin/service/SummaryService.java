package com.bin.service;


public interface SummaryService {
    /**
     * 生成摘要
     * @param text 文本
     * @return 摘要
     */
    String generateSummary(String text);
}
