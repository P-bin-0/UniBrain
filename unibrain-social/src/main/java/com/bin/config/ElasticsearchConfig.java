package com.bin.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 配置Elasticsearch
 */
@Configuration
public class ElasticsearchConfig {

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        // 配置ES集群地址
        RestClientBuilder builder = RestClient.builder(
                new HttpHost("localhost", 9200, "http")
                // 如需多个节点，继续添加
                // new HttpHost("localhost", 9201, "http")
        );

        return new RestHighLevelClient(builder);
    }
}
