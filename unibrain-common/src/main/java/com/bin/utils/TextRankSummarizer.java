package com.bin.utils;

import java.util.*;

public class TextRankSummarizer {
    // 摘要生成主方法
    public String summarize(String text, int maxSentences) {
        // 使用你的 TextSplitter 进行分句
        String[] sentencesArray = TextSplitter.splitBySentenceStatic(text);
        List<String> sentences = new ArrayList<>(Arrays.asList(sentencesArray));

        if (sentences.size() <= maxSentences) {
            return String.join(" ", sentences);
        }

        // 分词处理（中文按字符切分，后续可替换为 HanLP）
        List<List<String>> wordLists = new ArrayList<>();
        for (String sentence : sentences) {
            wordLists.add(tokenize(sentence));
        }

        // 构建相似度矩阵
        double[][] similarityMatrix = buildSimilarityMatrix(wordLists);
        double[] scores = computeTextRank(similarityMatrix);

        // 获取得分最高的句子索引
        List<Integer> topIndices = getTopSentenceIndices(scores, maxSentences);

        // 按原文顺序返回摘要
        Collections.sort(topIndices);
        StringBuilder summary = new StringBuilder();
        for (int idx : topIndices) {
            summary.append(sentences.get(idx)).append(" ");
        }

        return summary.toString().trim();
    }

    // 分词方法（中文按字符切分，可替换为 HanLP）
    private List<String> tokenize(String sentence) {
        return Arrays.asList(sentence.replaceAll("[^\\u4e00-\u9fa5\\w]+", "").split(""));
    }

    // 构建句子相似度矩阵
    private double[][] buildSimilarityMatrix(List<List<String>> wordLists) {
        int n = wordLists.size();
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                } else {
                    matrix[i][j] = cosineSimilarity(wordLists.get(i), wordLists.get(j));
                }
            }
        }

        return matrix;
    }

    // 余弦相似度计算
    private double cosineSimilarity(List<String> list1, List<String> list2) {
        Set<String> union = new HashSet<>(list1);
        union.addAll(list2);

        int dotProduct = 0;
        for (String word : union) {
            int count1 = Collections.frequency(list1, word);
            int count2 = Collections.frequency(list2, word);
            dotProduct += count1 * count2;
        }

        double norm1 = Math.sqrt(list1.size());
        double norm2 = Math.sqrt(list2.size());

        return norm1 * norm2 == 0 ? 0 : dotProduct / (norm1 * norm2);
    }

    // TextRank 算法迭代计算得分
    private double[] computeTextRank(double[][] matrix) {
        int n = matrix.length;
        double[] scores = new double[n];
        Arrays.fill(scores, 1.0);

        final double dampingFactor = 0.85;
        final int maxIterations = 100;
        final double eps = 0.0001;

        for (int iter = 0; iter < maxIterations; iter++) {
            double[] newScores = new double[n];
            for (int i = 0; i < n; i++) {
                double sum = 0;
                for (int j = 0; j < n; j++) {
                    if (matrix[j][i] > 0) {
                        sum += scores[j] / getOutDegree(matrix, j);
                    }
                }
                newScores[i] = (1 - dampingFactor) + dampingFactor * sum;
            }

            if (converged(scores, newScores, eps)) break;
            scores = newScores;
        }

        return scores;
    }

    private double getOutDegree(double[][] matrix, int i) {
        int count = 0;
        for (double v : matrix[i]) if (v > 0) count++;
        return count;
    }

    private boolean converged(double[] oldScores, double[] newScores, double eps) {
        for (int i = 0; i < oldScores.length; i++) {
            if (Math.abs(oldScores[i] - newScores[i]) > eps) {
                return false;
            }
        }
        return true;
    }

    // 获取得分最高的 N 个句子索引
    private List<Integer> getTopSentenceIndices(double[] scores, int topN) {
        List<Map.Entry<Integer, Double>> list = new ArrayList<>();
        for (int i = 0; i < scores.length; i++) {
            list.add(new AbstractMap.SimpleEntry<>(i, scores[i]));
        }

        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, list.size()); i++) {
            indices.add(list.get(i).getKey());
        }

        return indices;
    }
}
