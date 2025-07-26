package com.bin.utils;


import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.bin.exception.EmbeddingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author P-bin
 * @since 2023/10/14 16:17
 * 阿里云的向量化服务 Embedding API ,将文本转换为向量表示
 * 依赖为新版SDK，没有找到官方文档，依旧是采用的旧版SDK的方式
 */
@Component
public class AlibabaEmbedding {
    private final String apiKey;
    private static final String MODEL_NAME = "text-embedding-v4";
    private static final int DEFAULT_DIMENSION = 1024; // 默认为1024维,向量维度
    private static final int MAX_BATCH_SIZE = 25; // 最大批量大小,每次处理25条

    public AlibabaEmbedding(@Value("#{systemEnvironment['DASHSCOPE_API_KEY']}") String apiKey){
        this.apiKey = apiKey;
    }

    /**
     * 将字符串列表转换为向量列表
     * @param texts 输入文本列表
     * @return 每个文本对应的向量列表（List<float[]>）
     * @throws EmbeddingException 调用阿里云Embedding API失败时抛出异常
     */
    public List<float[]> getEmbeddings(List<String> texts) throws EmbeddingException {
        List<float[]> allEmbeddings = new ArrayList<>(); // 存储所有文本的向量
        for(int i = 0; i < texts.size(); i += MAX_BATCH_SIZE){
            List<String> batch = texts.subList(i, Math.min(i + MAX_BATCH_SIZE,texts.size()));

            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .apiKey(apiKey)
                    .model(MODEL_NAME)
                    .texts(batch)
                    .parameter("dimension", DEFAULT_DIMENSION)
                    .build();
            TextEmbedding textEmbedding = new TextEmbedding();
            try{
                TextEmbeddingResult result = textEmbedding.call(param);
                if(result != null && result.getOutput() != null && result.getOutput().getEmbeddings() != null){
                    for (var item : result.getOutput().getEmbeddings()){
                        // 将List<Double>转换为Double[]
                        Double[] doubleArray = item.getEmbedding().toArray(new Double[0]);
                        // 调用toPrimitive方法将Double[]转换为float[]
                        float[] floatArray = toPrimitive(doubleArray);
                        allEmbeddings.add(floatArray);
                    }
                }else{
                    throw new EmbeddingException("收到空的Embedding结果，请求索引：" + i);
                }
            } catch (ApiException | NoApiKeyException e) {
                throw new EmbeddingException("调用阿里云Embedding API失败", e);
            }
        }
        return allEmbeddings;
    }

    /**
     * 将Double数组转换为float数组
     * @param array Double数组
     * @return float数组
     */
    public static float[] toPrimitive(Double[] array) {
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].floatValue();
        }
        return result;
    }
    /**
     * 计算两个向量之间的余弦相似度
     * @param a 第一个向量
     * @param b 第二个向量
     * @return 余弦相似度
     */
    public static double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 计算向量列表的平均向量
     * @param vectors 向量列表
     * @return 平均向量
     */
    public static float[] computeAverageVector(List<float[]> vectors) {
        int dim = vectors.get(0).length;
        float[] avg = new float[dim];
        for (float[] vec : vectors) {
            for (int i = 0; i < dim; i++) {
                avg[i] += vec[i];
            }
        }
        for (int i = 0; i < dim; i++) {
            avg[i] /= vectors.size();
        }
        return avg;
    }
}
