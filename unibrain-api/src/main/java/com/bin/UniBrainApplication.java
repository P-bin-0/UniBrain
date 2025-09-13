package com.bin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "com.bin.repository")
public class UniBrainApplication {
    public static void main(String[] args) {
        SpringApplication.run(UniBrainApplication.class, args);
    }
}
