# Spring AI RAG Platform

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-green)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.2-blue)
![License](https://img.shields.io/badge/license-Apache%202.0-blue)

## 📖 项目概述

**Spring AI RAG Platform** 是一个基于 Spring AI 和 Spring Boot WebFlux 构建的高性能、响应式 AI 知识库平台。它利用 RAG（检索增强生成）技术，结合向量数据库（Qdrant）和多种大语言模型（DeepSeek, Ollama, Alibaba DashScope），为用户提供智能问答和知识管理服务。

本项目旨在解决传统 LLM 在垂直领域知识匮乏的问题，通过构建私有知识库，使 AI 能够基于特定文档回答问题。适用于企业内部知识库、智能客服、文档问答等场景。

### 🛠 主要技术栈

- **核心框架**: Spring Boot 3.5.9, Spring WebFlux
- **AI 框架**: Spring AI 1.1.2
- **数据库**: R2DBC (MySQL), Redis
- **向量数据库**: Qdrant
- **文档阅读**: PDF, Markdown (Spring AI Readers)
- **API 文档**: Knife4j
- **安全认证**: Spring Security, JWT

## ✨ 功能特性

- **🚀 多模型支持**: 集成 DeepSeek、Ollama、Alibaba DashScope 等多种主流大模型。
- **🧠 知识库管理**: 支持知识库的创建、编辑和管理，支持上传 PDF、Markdown 等格式文档。
- **🔍 RAG 检索增强**: 基于 Qdrant 向量数据库实现高效的语义检索，提升回答准确性。
- **💬 实时流式对话**: 基于 Server-Sent Events (SSE) 实现打字机效果的流式对话体验。
- **⚡ 响应式架构**: 全链路异步非阻塞设计，高并发下性能更优。
- **🛡️ 安全可靠**: 完善的用户认证与权限管理体系。

## 💻 安装指南

### 系统要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis
- Qdrant (向量数据库)

### 依赖安装

1. 克隆项目到本地：
   ```bash
   git clone https://github.com/your-username/spring-ai-rag-platform.git
   ```

2. 进入项目目录并安装依赖：
   ```bash
   cd spring-ai-rag-platform
   mvn clean install
   ```

### 配置说明

修改 `src/main/resources/application.yml` 及其引用的配置文件（如 `application-db.yml`, `application-ai.yml` 等），配置数据库连接和 API 密钥。

**数据库配置（示例，实际以环境配置文件为准）:**
```yaml
spring:
  r2dbc:
    pool:
      initial-size: 5
      max-size: 20
      max-idle-time: 5m
      max-life-time: 30m
      validation-query: SELECT 1
```

> 实际的 R2DBC 连接 URL、用户名和密码请在对应环境的 `application-*.yml` 或外部配置中心中配置。

**AI 配置 (`application-ai.yml`):**
```yaml
spring:
  ai:
    vectorstore:
      qdrant:
        collection-name: vector_store
        initialize-schema: true
project:
  conversation:
    sign-key: your-secure-32+character-secret-key
```

## 🚀 使用说明

### 启动项目

运行主程序 `AiAgentApplication.java` 或使用 Maven 启动：

```bash
mvn spring-boot:run
```

项目启动后，访问 Knife4j 接口文档：`http://localhost:10001/doc.html`

### 基本用法示例

#### 1. 聊天接口

**API 端点**: `POST /chat/message`

**请求示例**:

```json
{
  "platform": "ALIYUN",
  "model": "qwen-plus",
  "conversationId": "<会话ID，例如通过 /conversation/generate 获取>",
  "message": "什么是 RAG 技术？",
  "knowledgeType": null
}
```

**响应**: 返回 SSE 流式文本。

#### 2. 知识库管理

**API 端点**: `GET /knowledge/list`

**响应示例**:

```json
{
  "code": 200,
  "data": [
    {
      "id": "1",
      "name": "公司规章制度",
      "description": "包含所有内部管理规定"
    }
  ],
  "msg": "操作成功"
}
```

## 👨‍💻 开发指南

### 开发环境设置

1. 确保已安装并启动 Docker（推荐用于运行 Qdrant 和 Redis）。
2. 使用 IDE（IntelliJ IDEA 推荐）导入 Maven 项目。
3. 配置好 JDK 21。

### 构建和测试

运行单元测试：
```bash
mvn test
```

构建生产环境包：
```bash
mvn clean package -DskipTests
```

### 代码贡献规范

1. Fork 本仓库。
2. 创建新的特性分支 (`git checkout -b feature/AmazingFeature`)。
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)。
4. 推送到分支 (`git push origin feature/AmazingFeature`)。
5. 提交 Pull Request。

## 📄 许可证信息

本项目采用 [Apache License 2.0](LICENSE) 许可证。

Copyright (c) 2025-2026 Krismile.

## 📞 联系方式

- **问题反馈**: 请在 GitHub Issues 中提交。
- **邮件联系**: jyc@krismile.cn
- **维护者**: JiYinchuan

---
