# 项目概述与架构设计

## 1. 项目简介

**Spring AI RAG Platform** 是一个基于 Spring Boot 3 和 Spring AI 构建的企业级 RAG（检索增强生成）知识库平台。它旨在帮助企业和开发者快速搭建私有化的 AI 知识库问答系统，支持多种大模型接入、文档解析、向量检索以及智能对话功能。

## 2. 核心架构

本项目采用现代化的单体模块化架构，基于响应式编程模型（**WebFlux + R2DBC**）构建，在高并发场景下具备良好的吞吐能力。

### 2.1 技术栈

| 组件 | 版本 | 说明 |
| :--- | :--- | :--- |
| **Java** | 21 | 基础开发语言 |
| **Spring Boot** | 3.5.9 | 核心框架 |
| **Spring AI** | 1.1.2 | AI 能力集成（Ollama、DeepSeek、DashScope 等） |
| **Database** | MySQL (R2DBC) | 关系型数据库，全异步响应式驱动 |
| **Vector Store** | Qdrant | 向量数据库，用于知识库检索 |
| **Cache** | Redis | 缓存支持 |
| **API Docs** | Knife4j | 接口文档管理 |
| **Security** | Spring Security | JWT 认证与授权 |

### 2.2 模块划分

项目主要包含以下核心模块（按业务功能拆分）：

- **authorization**: 登录、注册与权限认证模块（JWT）。
- **chat**: 对话服务模块，包含会话 ID 管理、聊天历史记忆等能力。
- **knowledge**: 知识库管理模块，负责知识库实体及其文件的生命周期管理。
- **rag**: 文档解析、切片与向量化模块，封装向量库读写逻辑。
- **platform**: 模型与平台管理模块，支持多平台、多模型的动态配置与启用。
- **user**: 用户域模型与基础信息管理。

### 2.3 模型与平台管理

为了支持多模型、多平台以及不同模型类型（聊天/嵌入）之间的解耦，项目在领域层中引入了以下关键设计：

- 使用 **`ChatModelTypeEnum`** 区分模型类型：
  - `CHAT`: 聊天模型，用于对话生成；
  - `EMBEDDING`: 嵌入模型，用于向量化知识片段；
- 模型与平台关系由 `AiPlatformDO` 与 `AiModelDO` 维护，每个模型记录所属平台、类型、默认参数、启用状态等信息；
- 通过 `ChatModelService` 和 `ChatModelFactory` 提供统一的模型访问入口，前端可通过接口查询当前可用的聊天/嵌入模型列表；
- 平台侧通过策略模式（如 `AliYunChatStrategyImpl`、`DeepSeekChatStrategyImpl`、`OllamaChatStrategyImpl` 等）实现不同平台的适配。

## 3. 核心业务流程

### 3.1 RAG 流程

1. **文档上传**：用户通过 `/knowledge/file/uploadFile/{knowledgeId}` 上传 PDF、Markdown 等文档；
2. **文档解析**：后端根据文件后缀选择合适的 `FileParser`，完成文本抽取与预处理；
3. **文本切片**：将长文本切分为适合的片段（`Document`），并附加元数据（如用户 ID、文件 ID 等）；
4. **向量化模型选择**：用户在保存文件时通过 `embeddingModelId` 显式选择使用的 **嵌入模型**；
5. **向量化**：系统调用对应平台下的嵌入模型，将文本片段转为向量；
6. **向量存储**：通过 `VectorStoreBuilder` 及 `QdrantVectorStoreBuilder` 构建 Qdrant 向量库，并将向量写入指定集合；
7. **检索与生成**：用户发起聊天请求时，根据是否启用知识库（`knowledgeType`）决定是否执行向量检索，将检索到的片段作为上下文与用户问题一起发送至聊天模型生成回复；
8. **聊天记忆**：会话内容通过 `ChatMemory`（基于数据库实现）进行持久化，用于后续多轮对话。

### 3.2 聊天与会话流程

1. **生成会话 ID**：前端调用 `/conversation/generate` 获取会话 ID，并在后续请求中复用；
2. **发起对话**：前端调用 `/chat/message`，传入平台、模型编码、会话 ID、用户消息以及可选的知识库配置；
3. **流式响应**：接口以 SSE 流形式返回 `ChatResponse`，前端可边接收边渲染；
4. **历史记录查询**：通过 `/chat/history/conversations` 与 `/chat/history/memories` 查询会话列表和具体历史记录；
5. **会话清理**：通过 `/chat/history/conversation/{conversationId}` 删除不再需要的会话及其历史信息。

> 更详细的字段说明和接口入参/出参，请参考在线文档 `http://localhost:10001/doc.html`。
