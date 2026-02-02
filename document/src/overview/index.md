---
outline: deep
---

# 项目概述与架构设计

## 1. 项目简介

**Spring AI RAG Platform** 是一个基于 Spring Boot 3 和 Spring AI 构建的企业级 RAG（检索增强生成）知识库平台。项目采用响应式编程范式（WebFlux + R2DBC），具备高并发处理能力，适合构建私有化 AI 知识库问答系统。

### 核心特点

- **响应式全栈架构**: 后端采用 WebFlux + R2DBC 实现全异步非阻塞 I/O，前端基于 Vue 3 Composition API
- **多模型接入**: 通过策略模式支持 Ollama、DeepSeek、通义千问等多平台 AI 模型
- **RAG 知识增强**: 集成 Qdrant 向量数据库，实现语义检索与上下文增强
- **流式对话体验**: SSE（Server-Sent Events）实现打字机效果的实时对话
- **JWT 无状态认证**: 基于 Spring Security + JWT 实现安全的 RESTful API

## 2. 技术架构

### 2.1 技术栈一览

| 组件类别 | 技术选型 | 版本 | 说明 |
| :--- | :--- | :--- | :--- |
| **后端框架** | Spring Boot | 3.5.9 | 响应式 Web 框架 |
| | Spring WebFlux | 3.5.9 | 非阻塞式 Reactive Web |
| **AI 集成** | Spring AI | 1.1.2 | 统一的 AI 模型抽象层 |
| | Spring AI Alibaba | 1.1.2.0 | 通义千问接入支持 |
| **数据存储** | MySQL (R2DBC) | 8.0+ | 关系型数据库，异步驱动 |
| | Qdrant | latest | 向量数据库，语义检索 |
| | Redis | 6.0+ | 缓存与会话存储 |
| **安全认证** | Spring Security | 3.5.9 | WebFlux 安全框架 |
| | JJWT | 0.11.5 | JWT 令牌生成与解析 |
| **API 文档** | Knife4j | 4.5.0 | Swagger UI 增强版 |
| | SpringDoc | 2.8.14 | OpenAPI 3 文档生成 |
| **前端框架** | Vue 3 | 3.5.25 | 渐进式 JavaScript 框架 |
| | TypeScript | 5.9 | 类型安全的 JavaScript |
| | Element Plus | 2.13 | Vue 3 UI 组件库 |
| | Pinia | 3.0 | Vue 3 状态管理库 |
| | Axios | 1.13 | HTTP 请求库 |

### 2.2 代码分层与模块划分

项目采用 DDD（领域驱动设计）的思想进行分层：

```
src/main/java/cn/krismile/ai/agent/
├── configuration/         # 配置层
│   ├── database/          # 数据库配置（R2DBC、Jackson 转换器）
│   ├── security/          # 安全配置（JWT Filter、异常处理器）
│   ├── doc/               # API 文档配置
│   └── ChatConfiguration  # AI 聊天配置（文件存储等）
├── controller/            # 控制器层（RESTful API）
│   ├── chat/              # 聊天相关：ChatController、ChatMemoryController
│   ├── knowledge/         # 知识库：KnowledgeController、KnowledgeFileController
│   └── user/              # 用户：UserController、LoginController
├── model/                 # 数据模型层
│   ├── domain/            # 域对象（DO，对应数据库表）
│   ├── request/           # 请求参数定义（Request DTO）
│   ├── response/          # 响应参数定义（VO）
│   └── enumeration/       # 枚举类型
├── repository/            # 数据访问层（R2DBC Repository）
├── structure/             # 业务逻辑层（核心领域逻辑）
│   ├── authorization/     # 认证授权：登录、注册、JWT 验证
│   ├── chat/              # 聊天功能
│   │   ├── chatmodel/       # 模型管理（ChatModelFactory、ChatModelService）
│   │   ├── conversation/    # 会话管理（生成与验证 ConversationId）
│   │   ├── knowledge/       # 知识库服务
│   │   ├── memory/          # 聊天记忆服务（ChatMemory）
│   │   ├── model/           # 聊天策略模式实现
│   │   │   ├── strategy/     # 各平台策略：AliYunChatStrategyImpl、DeepSeekChatStrategyImpl
│   │   │   └── ChatStrategy  # 策略接口
│   │   └── platoform/       # 平台配置管理
│   └── rag/               # RAG 检索增强
│       ├── embedding/       # 向量化：VectorStoreBuilder、QdrantVectorStoreBuilder
│       └── file/            # 文件解析：FileParser、FileService
└── util/                  # 工具类：加密、JSON处理、雪花ID生成器
```

**关键设计点**：

1. **策略模式支持多平台**：`ChatPlatformEnum` 枚举定义了 `OLLAMA`、`DEEPSEEK`、`ALIYUN` 等平台，每个平台对应一个 `ChatStrategy` 实现类，通过 `enum.strategy()` 方法获取具体策略。

2. **模型类型分离**：`ChatModelTypeEnum` 区分 `CHAT`（聊天模型）和 `EMBEDDING`（嵌入模型），实现聊天与向量化模型的独立管理。

3. **响应式编程**: 所有 Service 层返回 `Mono<T>` 或 `Flux<T>`，Repository 使用 `ReactiveCrudRepository`，实现端到端的非阻塞异步处理。

4. **安全过滤链**：`AuthenticationWebFilter` 在请求到达 Controller 前拦截，解析 JWT Token 并注入到 `ReactiveSecurityContext` 中，后续通过 `SecurityUtils.getCurrentUserId()` 获取当前用户 ID。

### 2.3 核心设计模式

#### 策略模式：多平台 AI 模型适配

项目通过策略模式实现了多 AI 平台的统一接入：

```java
// ChatPlatformEnum 枚举定义平台与其策略实现的绑定
public enum ChatPlatformEnum {
    OLLAMA("ollama", "Ollama", OllamaChatStrategyImpl.BEAN_NAME),
    DEEPSEEK("deepseek", "Deepseek", DeepSeekChatStrategyImpl.BEAN_NAME),
    ALIYUN("aliyun", "阿里云", AliYunChatStrategyImpl.BEAN_NAME);
    
    // 通过 enum.strategy() 直接获取策略实例
    public ChatPlatformStrategy strategy() {
        return SpringUtils.getApplicationContext()
            .getBean(this.strategyBeanName, ChatPlatformStrategy.class);
    }
}

// Controller 层直接调用策略
@PostMapping("/message")
public Flux<ChatResponse> chat(@RequestBody ChatRequest request) {
    return request.getPlatform().strategy().chat(request);
}
```

**设计优势**：
- 新增平台无需修改 Controller，只需添加枚举值和对应的 Strategy 实现
- 各平台逻辑隔离，互不影响
- 符合开闭原则（OCP）

#### 工厂模式：动态构建 AI 模型

`ChatModelFactory` 根据平台和模型类型动态构建 ChatModel 或 EmbeddingModel：

```java
// 构建聊天模型
Mono<ChatModel> chatModel = ChatModelFactory.builder(ChatPlatformEnum.DEEPSEEK)
    .chat("deepseek-chat", options);

// 构建嵌入模型
Mono<EmbeddingModel> embeddingModel = ChatModelFactory.builder(ChatPlatformEnum.ALIYUN)
    .embedding("text-embedding-v2", options);
```

这种设计使得 RAG 流程中的向量化模型可以由用户在上传文件时自由选择，而非硬编码在配置文件中。

### 2.4 前端架构设计

前端采用 Vue 3 + TypeScript + Pinia 的现代化架构：

#### 目录结构

```
website/src/
├── api/                  # API 接口封装
│   ├── api-client.ts     # Axios 客户端配置（拦截器、错误处理）
│   ├── auth-api.ts       # 认证相关接口
│   ├── chat-api.ts       # 聊天接口（包含 SSE 流式处理）
│   ├── knowledge-api.ts  # 知识库接口
│   └── model.ts          # 类型定义和统一响应格式 VO<T>
├── components/          # Vue 组件
│   ├── chat/             # 聊天相关：ChatArea、MessageBubble、SessionList
│   ├── knowledge/        # 知识库：FileUpload、KnowledgeDetail
│   ├── settings/         # 设置页面：AIProviderSettings、AIModelList
│   └── common/           # 通用组件：SidebarNav、LoginDialog
├── stores/              # Pinia 状态管理
│   ├── auth.ts           # 认证状态：token、登录/注册逻辑
│   └── chat.ts           # 聊天状态：会话列表、消息历史、模型选择
├── views/               # 页面视图
│   ├── ChatView.vue      # 聊天页面
│   ├── KnowledgeBase.vue # 知识库管理
│   └── SettingsView.vue  # 设置页面
└── router/              # Vue Router 路由配置
```

#### 核心特性

**1. 状态管理**：

- `auth.ts`：管理用户登录状态、JWT Token，自动从 `localStorage` 恢复登录状态
- `chat.ts`：管理会话列表、消息历史、当前选中模型，实现会话切换时自动加载历史

**2. SSE 流式响应处理**：

```typescript
// chat-api.ts 中使用 Fetch API 处理 SSE
const response = await fetch('/api/chat/message', {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${token}` },
  body: JSON.stringify(chatRequest)
});

const reader = response.body.getReader();
const decoder = new TextDecoder();

// 逐行解析 SSE 流，处理 data: {"content":"...","reasoningContent":"..."}
```

**3. API 请求拦截**：

- 请求拦截器：自动添加 `Authorization` 头
- 响应拦截器：统一处理错误码，`A0301` 认证失败时清除 token
- 错误提示：优先展示 `userTip` 字段，用户友好

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

### 3.3 前后端交互详解

#### SSE 流式响应处理

后端通过 `Flux<ChatResponse>` 返回流式数据，前端使用 Fetch API 逐行解析：

```typescript
// 前端处理逻辑（chat.ts store）
await sendChatMessage(chatRequest, {
  onMessage: (content: string, reasoningContent?: string) => {
    // 处理 reasoningContent（AI 思考过程）
    if (reasoningContent) {
      message.reasoningContent += reasoningContent;
      message.isThinking = true;
    }
    
    // 处理 content（正式回复）
    if (content) {
      message.isThinking = false; // 结束思考
      message.content += content;
    }
  },
  onError: (error, userTip) => {
    // 显示错误提示
    ElMessage.error(userTip || '请求失败');
  },
  onComplete: () => {
    // 流结束，清除加载状态
    message.isLoading = false;
  }
});
```

#### 统一响应格式

所有 API 接口的响应都遵循 `VO<T>` 结构：

```typescript
interface VO<T> {
  errorCode: string;    // "00000" 表示成功
  errorMessage: string; // 开发者错误信息
  userTip: string;      // 用户友好提示
  data: T;              // 实际数据
}
```

前端统一在 Axios 响应拦截器中处理：
- `errorCode === "00000"` 表示成功
- `errorCode === "A0301"` 表示认证失败，自动清除 token
- 其他错误码展示 `userTip` 提示用户

> 更详细的字段说明和接口入参/出参，请参考接口文档 `http://localhost:10001/doc.html`。
