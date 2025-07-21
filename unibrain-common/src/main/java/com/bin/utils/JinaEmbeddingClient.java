package com.bin.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @author P-bin
 * @since 2023/10/14 16:17
 * Jina Embedding API 客户端
 */
public class JinaEmbeddingClient {
    private final String apiKey;
    private final String apiUrl = "https://api.jina.ai/v1/embeddings";
    private final RestTemplate restTemplate = new RestTemplate();

    public JinaEmbeddingClient(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 获取多个文本的 Embedding 向量
     * @param texts 输入文本列表
     * @return 向量列表（List<float[]>）
     */
    public List<float[]> getEmbeddings(List<String> texts) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        // 根据实际使用的模型调整 model 参数
        requestBody.put("model", "jina-embeddings-v2-base-en");
        requestBody.put("input", texts);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

        List<float[]> embeddings = new ArrayList<>();
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");

        for (Map<String, Object> item : data) {
            List<Double> embeddingList = (List<Double>) item.get("embedding");
            float[] embeddingArray = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                embeddingArray[i] = embeddingList.get(i).floatValue();
            }
            embeddings.add(embeddingArray);
        }

        return embeddings;
    }

    /**
     * 获取单个文本的 Embedding 向量
     *
     * @param text 输入文本
     * @return 向量（float[]）
     */
    public float[] getEmbedding(String text) {
        return getEmbeddings(Collections.singletonList(text)).get(0);
    }
    /*public static void main(String[] args) {
        String apiKey = "jina_ba1575d3e11146869cb87065fc8c9da8PcJ8R9SwFuxM3DJzGWjSgsmG0fxB"; // 替换为你的 API Key
        JinaEmbeddingClient client = new JinaEmbeddingClient(apiKey);

        List<String> texts = Arrays.asList(
                "人工智能导论",
                "机器学习基础",
                "深度学习与神经网络"
        );

        List<float[]> embeddings = client.getEmbeddings(texts);

        for (int i = 0; i < texts.size(); i++) {
            System.out.println("文本: " + texts.get(i));
            System.out.println("Embedding: " + Arrays.toString(embeddings.get(i)));
        }
    }*/
}
