项目介绍

UniBrain 是一个基于 Spring Boot 3.5.3 构建的多模块 Java 项目，采用 Java 17 开发，通过 Maven 进行依赖管理与构建。项目聚焦于教育领域，整合了主流技术栈，旨在提供全面的学术与教学支持功能。
项目采用模块化设计，包含七大核心模块：

unibrain-common：通用基础模块，提供工具类、依赖管理及核心配置，为其他模块提供底层支持

unibrain-paper：论文管理模块，处理论文相关业务逻辑

unibrain-course：课程管理模块，支持课程数据处理、缓存及消息通知

unibrain-experiment：实验管理模块，专注于实验数据的处理与管理

unibrain-social：社交互动模块，集成搜索引擎实现相关内容检索功能

unibrain-api：API 聚合模块，作为系统对外提供服务的统一入口

unibrain-security：安全认证模块，基于 Spring Security 与 JWT 实现身份验证与权限管理

技术上，项目整合了 MyBatis-Plus 作为 ORM 框架，MySQL 8.0 作为数据库，Redis 用于缓存，Elasticsearch 提供搜索能力，同时集成了 AI 能力（基于 LangChain4j 和阿里云 DashScope SDK）、Excel 处理（EasyExcel）等功能，形成了一套完整的教育领域解决方案。
