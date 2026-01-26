# API 接口文档

本项目集成了 **Knife4j + Springdoc**，提供可视化的 Swagger 接口文档和在线调试能力。以下内容基于当前代码（最近一次提交）整理，便于前后端联调与集成。

## 1. 在线文档入口

- **服务端口**: `10001`（见 `application.yml` 中 `server.port`）
- **Swagger/Knife4j UI**: 打开浏览器访问：
  - `http://localhost:10001/doc.html`

接口的分组、入参与出参说明以在线文档为准，本文档主要对重要业务接口进行归纳和补充说明。

## 2. 认证与通用约定

- **鉴权方式**: 统一使用 **JWT**，登录成功后后端返回 `token`，后续接口通过请求头传递：
  - `Authorization: Bearer <token>`
- **公共前缀**: 当前接口未统一前缀，均以控制器上的 `@RequestMapping` 为准，例如：`/login`、`/chat`、`/knowledge` 等。
- **公开接口（无需登录）**（见 `SecurityConfiguration`）:
  - `/login/**`, `/register/**`
  - `/actuator/**`, `/favicon.ico`
  - `/doc.html`, `/webjars/**`, `/v3/api-docs/**`
  - `/conversation/generate`
  - `/platform/model/**`
  - `/chat/message`
- **其余接口** 默认需要携带有效的 JWT Token 才能访问。

## 3. 接口分组概览

### 3.1 用户认证（Auth）

- **POST `/login/username`**
  - 功能：根据用户名登录，返回 JWT token。
  - 请求体：`LoginByUsernameRequest`
  - 说明：登录成功后请保存返回的 token，并在后续请求中放入 `Authorization` 头。

- **GET `/register/checkUsername`**
  - 功能：校验用户名是否已注册。
  - 查询参数：`username` 用户名。

- **POST `/register/username`**
  - 功能：根据用户名注册账号。
  - 请求体：`RegisterByUsernameRequest`

### 3.2 对话与会话管理（Chat & Conversation）

- **POST `/conversation/generate`**
  - 功能：生成一个新的会话 ID。
  - 返回：`VO<String>`，内容为会话 ID。
  - 是否鉴权：**无需登录**。

- **PUT `/conversation/verify`**
  - 功能：验证会话 ID 是否有效。
  - 请求体：`String` 类型的会话 ID。
  - 返回：`VO<Boolean>`。

- **POST `/chat/message`**
  - 功能：发起一次聊天对话，支持 SSE 流式返回。
  - 请求体：`ChatRequest`，关键字段：
    - `platform`: 聊天平台枚举 `ChatPlatformEnum`（如：`ALIYUN`、`DEEPSEEK`、`OLLAMA` 等）。
    - `model`: 具体模型编码（如某个平台下的聊天模型 code）。
    - `conversationId`: 会话 ID（建议通过 `/conversation/generate` 获取）。
    - `message`: 用户输入的问题/消息。
    - `knowledgeType`: 可选，`ChatKnowledgeTypeEnum`，为空则不启用知识库 RAG。
    - `options`: 可选，对话参数（温度、最大 Token 等）。
  - 响应：`text/event-stream`，数据为 `ChatResponse` 流。
  - 是否鉴权：**无需登录**（当前安全配置下）。

- **GET `/chat/history/conversations`**
  - 功能：查询当前用户的会话列表。
  - 返回：`VO<List<ChatConversationVO>>`。

- **GET `/chat/history/memories`**
  - 功能：查询某个会话下的聊天记录。
  - 查询参数：`conversationId` 会话 ID。
  - 返回：`VO<List<ChatMemoryVO>>`。

- **DELETE `/chat/history/conversation/{conversationId}`**
  - 功能：删除指定会话及其历史记录。
  - 路径参数：`conversationId` 会话 ID。
  - 返回：`VO<Boolean>`。

### 3.3 知识库管理（Knowledge）

- **GET `/knowledge/checkName`**
  - 功能：校验知识库名称是否可用。
  - 查询参数：`name` 知识库名称。

- **GET `/knowledge/list`**
  - 功能：查询当前用户的知识库列表。
  - 返回：`VO<List<KnowledgeVO>>`。

- **POST `/knowledge/addEdit`**
  - 功能：新增或编辑知识库。
  - 请求体：`KnowledgeAddEditRequest`。

### 3.4 知识库文件管理（Knowledge File）

> 路径前缀：`/knowledge/file`

- **GET `/knowledge/file/list/{knowledgeId}`**
  - 功能：查询指定知识库下的文件列表。
  - 路径参数：`knowledgeId` 知识库 ID。
  - 返回：`VO<List<KnowledgeFileVO>>`，其中包含关联的嵌入模型信息 `relEmbeddingModel`。

- **GET `/knowledge/file/listFileDetails/{fileId}`**
  - 功能：查询单个文件的切片详情。
  - 路径参数：`fileId` 文件 ID。
  - 返回：`VO<List<KnowledgeFileDetailVO>>`。

- **POST `/knowledge/file/uploadFile/{knowledgeId}`**
  - 功能：上传原始文档文件并进行解析与切片。
  - 路径参数：`knowledgeId` 知识库 ID。
  - 请求体：`multipart/form-data`，字段 `files`（`Flux<FilePart>`）。
  - 返回：`VO<List<KnowledgeFileUploadVO>>`，包含解析后的片段信息。

- **PUT `/knowledge/file/add/{knowledgeId}`**
  - 功能：基于前一步解析结果，保存文件及其切片，并写入向量库。
  - 路径参数：`knowledgeId` 知识库 ID。
  - 请求体：`Flux<KnowledgeFileAddRequest>`，关键字段：
    - `id`: 文件 ID（上传接口返回）。
    - `description`: 文件描述。
    - `details`: 片段列表 `KnowLedgeFileDetailAddRequest`（可包含新增或编辑）。
    - `embeddingModelId`: 选择的 **嵌入模型 ID**，用于构建向量存储并写入 Qdrant。
  - 返回：`VO<List<Long>>`，保存成功的文件 ID 列表或失败的详情 ID 列表（视实现而定）。

- **DELETE `/knowledge/file/del/{fileId}`**
  - 功能：删除文件及其在向量库中的所有文档。
  - 路径参数：`fileId` 文件 ID。
  - 返回：`VO<Boolean>`。

### 3.5 模型与平台管理（Model & Platform）

- **GET `/model/chat/list`**
  - 功能：查询所有可用的聊天模型列表（仅返回启用的模型）。
  - 返回：`VO<List<ChatModelVO>>`，其中包含：平台、模型编码、模型名称、类型 `ChatModelTypeEnum`、是否启用、排序等。

- **GET `/platform/chat/list`**
  - 功能：查询聊天平台列表及其配置信息。
  - 返回：`VO<List<ChatPlatformVO>>`。

- **PUT `/platform/chat/edit`**
  - 功能：编辑聊天平台配置。
  - 请求体：`ChatPlatformEditRequest`。

- **GET `/platform/model/{type}/list`**
  - 功能：根据模型类型查询模型列表。
  - 路径参数：`type`，取值为 `CHAT`（聊天模型）或 `EMBEDDING`（嵌入模型），枚举 `ChatModelTypeEnum`。
  - 返回：`VO<List<ChatModelVO>>`。

- **PUT `/platform/model/{type}/edit`**
  - 功能：编辑指定类型的模型配置。
  - 路径参数：`type`（同上）。
  - 请求体：`ChatModelEditRequest`。
  - 说明：当前安全配置中 `/platform/model/**` 为公开接口，生产环境建议结合网关或调整安全策略限制访问。

## 4. 调用示例

### 4.1 登录示例

```bash
curl -X POST "http://localhost:10001/login/username" \
  -H "Content-Type: application/json" \
  -d '{
        "username": "admin",
        "password": "password"
      }'
```

### 4.2 发起一次聊天示例

```bash
# 1. 先生成会话 ID（无需登录）
curl -X POST "http://localhost:10001/conversation/generate"

# 2. 使用会话 ID 发起聊天（SSE 流式返回）
curl -X POST "http://localhost:10001/chat/message" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <Your-Token>" \
  -d '{
        "platform": "ALIYUN",
        "model": "qwen-plus",
        "conversationId": "<上一步返回的会话ID>",
        "message": "你好，请介绍一下 RAG 技术",
        "knowledgeType": null,
        "options": {
          "temperature": 0.7
        }
      }'
```

> 更多接口定义、枚举类型与字段说明，请以 `http://localhost:10001/doc.html` 中自动生成的在线文档为准。
