UniBrain 项目说明
项目简介
UniBrain 是一个基于 Spring Boot 的多模块 Java 项目，专注于提供教育领域相关功能支持，涵盖论文管理、课程管理、实验管理及社交互动等核心模块，集成了主流技术栈以保证系统的稳定性、可扩展性和安全性。
技术栈
基础框架：Spring Boot 3.5.3
开发语言：Java 17
构建工具：Maven
ORM 框架：MyBatis-Plus 3.5.12
数据库：MySQL 8.0.33
缓存：Redis（通过 Redisson 整合）
消息队列：RabbitMQ
搜索引擎：Elasticsearch 7.17.0
安全认证：Spring Security + JWT（基于 JJWT 实现）
API 文档：SpringDoc OpenAPI 2.8.9
工具类：Hutool 5.8.38、Lombok 1.18.38
Excel 处理：EasyExcel 4.0.3
AI 集成：LangChain4j、阿里云 DashScope SDK
项目结构
plaintext
UniBrain/
├── unibrain-common/          # 通用模块（基础工具、依赖）
├── unibrain-paper/           # 论文相关功能模块
├── unibrain-course/          # 课程相关功能模块
├── unibrain-experiment/      # 实验相关功能模块
├── unibrain-social/          # 社交相关功能模块（集成 Elasticsearch）
├── unibrain-api/             # API 接口模块（聚合其他模块，对外提供服务）
└── unibrain-security/        # 安全模块（认证授权）
模块说明
unibrain-common
核心通用模块，包含 Web 基础配置、Redis 工具、MyBatis-Plus 配置、AI 工具类等，被其他所有模块依赖。
unibrain-security
基于 Spring Security 和 JWT 实现认证授权功能，提供用户身份验证、权限管理支持，被多个业务模块依赖。
unibrain-api
系统对外提供 API 的入口模块，聚合所有业务模块，包含主配置文件 application.yml 和测试代码。
unibrain-course
课程管理模块，支持课程数据导入导出（依赖 EasyExcel）、缓存（Redis）和消息通知（RabbitMQ）。
unibrain-experiment
实验管理模块，主要涉及实验数据的 Excel 导入导出功能。
unibrain-paper
论文管理模块，依赖通用模块和安全模块，提供论文相关业务功能。
unibrain-social
社交互动模块，集成 Elasticsearch 实现用户、内容搜索等功能，支持数据校验（jakarta.validation）。
环境配置
基础环境
JDK 17+
Maven 3.6+
MySQL 8.0.33
Redis 5.0+
RabbitMQ 3.8+（可选，课程模块使用）
Elasticsearch 7.17.0（可选，社交模块使用）
配置文件
核心配置位于 unibrain-api/src/main/resources/application.yml，主要配置项包括：
yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/unibrain?useSSL=false&serverTimezone=UTC
    username: root
    password: 123456

# Redis 配置
  data:
    redis:
      host: localhost
      port: 6379
      password: 123456

# Elasticsearch 配置
  elasticsearch:
    uris: http://localhost:9200

# JWT 配置
jwt:
  secret: a1B2c3D4e5F6g7H8i9J0k1L2m3N4o5P6q7R8s9T0u1V2w3X4y5Z6
  expiration: 60000  # 1分钟

# AI 模型配置（阿里云 DashScope）
langchain4j:
  community:
    dashscope:
      chat-model:
        api-key: ${DASHSCOPE_API_KEY}  # 通过环境变量配置
        model-name: qwen-plus
