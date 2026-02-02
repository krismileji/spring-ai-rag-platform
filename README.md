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

#### 后端技术栈

- **核心框架**: Spring Boot 3.5.9, Spring WebFlux
- **AI 框架**: Spring AI 1.1.2
- **数据库**: R2DBC (MySQL), Redis
- **向量数据库**: Qdrant
- **文档阅读**: PDF, Markdown (Spring AI Readers)
- **API 文档**: Knife4j
- **安全认证**: Spring Security, JWT

#### 前端技术栈

- **核心框架**: Vue 3.5.25 + TypeScript 5.9
- **构建工具**: Vite (Rolldown)
- **UI 组件库**: Element Plus 2.13
- **状态管理**: Pinia 3.0
- **路由管理**: Vue Router 4.6
- **HTTP 客户端**: Axios 1.13
- **Markdown 渲染**: agent-markdown-vue 1.2.5
- **代码规范**: ESLint + Prettier + Oxlint

## ✨ 功能特性

### 核心功能

- **🚀 多模型支持**: 集成 DeepSeek、Ollama、Alibaba DashScope 等多种主流大模型。
- **🧠 知识库管理**: 支持知识库的创建、编辑和管理，支持上传 PDF、Markdown 等格式文档。
- **🔍 RAG 检索增强**: 基于 Qdrant 向量数据库实现高效的语义检索，提升回答准确性。
- **💬 实时流式对话**: 基于 Server-Sent Events (SSE) 实现打字机效果的流式对话体验。
- **⚡ 响应式架构**: 全链路异步非阻塞设计，高并发下性能更优。
- **🛡️ 安全可靠**: 完善的用户认证与权限管理体系。

### 前端特性

- **🎨 现代化 UI**: 基于 Element Plus 的精美界面，支持亮色/暗色主题切换
- **📱 响应式设计**: 适配各种屏幕尺寸，提供良好的移动端体验
- **💡 智能交互**: 
  - 多会话管理，支持创建、切换、删除会话
  - 实时流式输出，打字机效果展示 AI 回复
  - 思考过程可视化，展示 AI 推理过程和耗时
  - Markdown 渲染，支持代码高亮和数学公式
- **🔧 灵活配置**:
  - 多平台 AI 模型动态切换
  - 知识库模式选择（本地/云端/不启用）
  - AI 参数自定义（温度、Token 限制等）
- **📝 知识库可视化**:
  - 文件列表展示与搜索
  - 文件内容预览
  - 拖拽上传支持

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

### 启动后端服务

运行主程序 `AiAgentApplication.java` 或使用 Maven 启动：

```bash
mvn spring-boot:run
```

后端服务启动后，访问 Knife4j 接口文档：`http://localhost:10001/doc.html`

### 启动前端应用

进入前端项目目录并启动开发服务器：

```bash
cd website
npm install  # 首次运行需要安装依赖
npm run dev
```

前端应用启动后，访问：`http://localhost:5173`

> **注意**: 前端开发服务器配置了 API 代理，会自动将 `/api` 请求转发到后端 `http://127.0.0.1:10001`

### 基本用法示例

#### 1. Web 界面使用

**聊天功能**:
1. 访问前端应用首页，自动进入聊天界面
2. 在顶部选择 AI 模型（如 DeepSeek、Ollama 等）
3. 选择知识库模式：不启用 / 本地知识库
4. 在输入框输入问题，点击发送或按 Enter 键
5. AI 回复以流式方式实时展示，支持 Markdown 渲染

**知识库管理**:
1. 点击左侧导航栏的「知识库」图标
2. 需要登录后才能访问（首次访问会提示登录）
3. 创建知识库：点击「创建知识库」按钮，填写名称和描述
4. 上传文档：选择知识库后，支持拖拽或点击上传 PDF、Markdown 文件
5. 查看文件：点击文件名可预览文档内容和分块信息

**系统设置**:
1. 点击左侧导航栏的「设置」图标
2. **AI 模型配置**：配置各平台的 API Key、模型参数
3. **通用设置**：切换主题、语言等
4. **关于**：查看系统版本和项目信息

#### 2. API 接口使用

**聊天接口**:

**API 端点**: `POST /chat/message`

**请求示例**:

```json
{
  "platform": "ALIYUN",
  "model": "qwen-plus",
  "conversationId": "<会话ID，例如通过 /conversation/generate 获取>",
  "message": "什么是 RAG 技术？",
  "knowledgeType": "LOCAL",
  "options": {
    "enableThinking": true
  }
}
```

**响应**: 返回 SSE 流式文本，包含 `content`（回复内容）和 `reasoningContent`（思考过程）。

**知识库管理**:

**API 端点**: `GET /knowledge/list`

**响应示例**:

```json
{
  "errorCode": "00000",
  "data": [
    {
      "id": "1",
      "name": "公司规章制度",
      "description": "包含所有内部管理规定"
    }
  ],
  "userTip": "操作成功"
}
```

## 👨‍💻 开发指南

### 开发环境设置

#### 后端开发环境

1. 确保已安装并启动 Docker（推荐用于运行 Qdrant 和 Redis）。
2. 使用 IDE（IntelliJ IDEA 推荐）导入 Maven 项目。
3. 配置好 JDK 21。

#### 前端开发环境

1. 安装 Node.js 20.19.0+ 或 22.12.0+
2. 使用现代 IDE（推荐 VS Code 或 WebStorm）
3. 安装推荐的 VSCode 插件：
   - Vue Language Features (Volar)
   - TypeScript Vue Plugin (Volar)
   - ESLint
   - Prettier

### 构建和测试

#### 后端构建

运行单元测试：
```bash
mvn test
```

构建生产环境包：
```bash
mvn clean package -DskipTests
```

#### 前端构建

类型检查：
```bash
cd website
npm run type-check
```

代码检查和格式化：
```bash
npm run lint     # 运行 ESLint 和 Oxlint
npm run format   # 格式化代码
```

构建生产版本：
```bash
npm run build
```

预览生产构建：
```bash
npm run preview
```

### 项目结构说明

#### 前端目录结构

```
website/
├── src/
│   ├── api/              # API 接口定义
│   │   ├── api-client.ts   # Axios 客户端配置
│   │   ├── auth-api.ts     # 认证接口
│   │   ├── chat-api.ts     # 聊天接口
│   │   ├── knowledge-api.ts # 知识库接口
│   │   └── platform-api.ts  # 平台配置接口
│   ├── components/       # 组件
│   │   ├── chat/           # 聊天相关组件
│   │   ├── knowledge/      # 知识库相关组件
│   │   ├── settings/       # 设置相关组件
│   │   └── common/         # 通用组件
│   ├── composables/      # 组合式函数
│   ├── stores/           # Pinia 状态管理
│   │   ├── auth.ts         # 认证状态
│   │   └── chat.ts         # 聊天状态
│   ├── views/            # 页面视图
│   ├── router/           # 路由配置
│   └── assets/           # 静态资源
├── vite.config.ts        # Vite 配置
└── package.json          # 依赖管理
```

### 前后端交互说明

#### API 通信

- **基础 URL**: 开发环境通过 Vite 代理转发到 `http://127.0.0.1:10001`
- **认证方式**: JWT Token，存储在 `localStorage`，通过 `Authorization: Bearer <token>` 头部传递
- **响应格式**: 统一 VO 结构
  ```typescript
  interface VO<T> {
    errorCode: string    // "00000" 表示成功
    errorMessage: string // 错误信息（开发用）
    userTip: string      // 用户提示信息
    data: T              // 响应数据
  }
  ```

#### SSE 流式通信

聊天接口采用 Server-Sent Events 实现流式响应：

```typescript
// 前端使用 Fetch API 处理 SSE
const response = await fetch('/api/chat/message', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(chatRequest)
})

const reader = response.body.getReader()
// 逐行读取 data: {"content":"...","reasoningContent":"..."}
```

#### 状态管理

- **认证状态** (`stores/auth.ts`): 管理用户登录状态、Token、用户信息
- **聊天状态** (`stores/chat.ts`): 管理会话列表、消息历史、模型选择
- 刷新页面自动从 `localStorage` 恢复登录状态
- 会话切换时自动加载历史消息

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
