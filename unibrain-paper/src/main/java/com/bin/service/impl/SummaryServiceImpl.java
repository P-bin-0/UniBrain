package com.bin.service.impl;

import com.bin.service.SummaryService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

@Service
public class SummaryServiceImpl implements SummaryService {

    private final ChatLanguageModel chatLanguageModel;

    public SummaryServiceImpl(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }
    /**
     * 生成摘要
     * @param text 文本
     * @return 摘要
     */
    @Override
    public String generateSummary(String text) {
        // 输入校验，防止空字符串
        if(text == null || text.trim().isEmpty()){
            throw new IllegalArgumentException("生成摘要失败：输入文本不能为空或空白");
        }
        // 构建提示词
        String prompt = """
                请对以下内容生成摘要：
                1. 简洁明了;
                2. 覆盖核心观点，不遗漏关键信息;
                3. 语言通顺，不超过150字;
                4. 保留重要细节，不丢失信息;
                5. 总结主要结果和发现;
                """.formatted(text);
        // 调用模型生成摘要
        try {
            return chatLanguageModel.chat(prompt);
        } catch (Exception e) {
            throw new RuntimeException("生成摘要失败：" + e.getMessage(), e);
        }
    }
}
