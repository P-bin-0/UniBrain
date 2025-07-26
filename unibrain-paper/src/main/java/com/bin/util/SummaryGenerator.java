package com.bin.util;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.bin.exception.EmbeddingException;
import com.bin.response.ApiResponse;
import com.bin.utils.AlibabaEmbedding;
import com.bin.utils.TextSplitter;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author P-bin
 * @date 2023/12/20 14:25
 * 摘要生成器
 */
@Component
public class SummaryGenerator {
    private final TextSplitter textSplitter;
    private final AlibabaEmbedding alibabaEmbedding;
    private final Generation generation;

    public SummaryGenerator(TextSplitter textSplitter, AlibabaEmbedding alibabaEmbedding, Generation generation) {
        this.textSplitter = textSplitter;
        this.alibabaEmbedding = alibabaEmbedding;
        this.generation = generation;
    }
    public String generateSummary(String inputText) throws EmbeddingException {
        List<String> paragraphs = textSplitter.splitText(inputText);

        //判断是否为空
        if (paragraphs.isEmpty()) {
            return "输入文本为空，无法生成摘要。";
        }
        List<float[]> embeddings = alibabaEmbedding.getEmbeddings(paragraphs);
        float[] centerVector = AlibabaEmbedding.computeAverageVector(embeddings); // 计算中心向量
        List<ParagraphWithScore> scores = new ArrayList<>();
        for (int i = 0; i < embeddings.size(); i++) {
            double score = AlibabaEmbedding.cosineSimilarity(centerVector, embeddings.get(i));
            scores.add(new ParagraphWithScore(paragraphs.get(i), score));
        }
        scores.sort((a, b) -> Double.compare(b.getScore(), a.getScore())); // 按相似度降序排序
        List<String> topParagraphs = scores.stream()
                .limit(5)
                .map(ParagraphWithScore::getText)
                .toList();
        String input = String.join("\n\n", topParagraphs);
        // 调用生成模型
        GenerationParam param = GenerationParam.builder()
                .model("qwen-max")
                .prompt("请对以下内容进行摘要，要求简洁明了，不超过150字：\n\n" + input)
                .maxTokens(500)
                .temperature(0.5F)
                .build();
        try {
            GenerationResult result = generation.call(param);
            return result.getOutput().getText();
        } catch (NoApiKeyException | InputRequiredException e) {
            // 可根据实际需求调整异常处理逻辑
            throw new RuntimeException("调用生成模型失败", e);
        }
    }
    private static class ParagraphWithScore {
        private final String text;
        private final double score;

        public ParagraphWithScore(String text, double score) {
            this.text = text;
            this.score = score;
        }

        public String getText() { return text; }
        public double getScore() { return score; }
    }
}
