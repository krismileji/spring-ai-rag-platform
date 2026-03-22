---
outline: deep
---

# 常见问题解决方案 (FAQ)

## 1. 环境配置相关

### Q: 启动时报错 "Connection refused" 连接数据库失败？
**A**: 请检查以下配置：
1. 确认 MySQL 服务已启动，且端口可访问
2. 检查 `application-db.yml` 中的 R2DBC URL 格式：`r2dbc:mysql://localhost:3306/database_name`
3. 注意 R2DBC 与 JDBC URL 格式不同，前缀为 `r2dbc:mysql` 而非 `jdbc:mysql`
4. 验证用户名和密码是否正确

### Q: 报错 "OllamaConnectionException" 或 "Connection reset"？
**A**: 常见原因和解决方案：
1. **Ollama 未启动**：确认 Ollama 服务运行中，访问 `http://localhost:11434` 验证
2. **网络超时**：在 `application-ai.yml` 中增加超时配置：
   ```yaml
   spring:
     ai:
       ollama:
         base-url: http://localhost:11434
         chat:
           options:
             timeout: 120s  # 增加超时时间
   ```
3. **通义千问等在线服务**：网络不稳定时建议配置重试机制，参考 `WebClientConfiguration` 中的 Retry 配置

### Q: Qdrant 向量数据库连接失败？
**A**: 
1. 使用 Docker 启动 Qdrant：
   ```bash
   docker run -d --name qdrant -p 6333:6333 -p 6334:6334 qdrant/qdrant:latest
   ```
2. 检查 `application-ai.yml` 中的 Qdrant 配置：
   ```yaml
   spring:
     ai:
       vectorstore:
         qdrant:
           host: localhost
           port: 6333
   ```

## 2. 功能使用相关

### Q: 上传文档后无法检索到内容？
**A**: 排查步骤：
1. **检查文件是否保存成功**：查看 `/knowledge/file/add` 接口返回，确认向量化完成
2. **验证 Qdrant 中是否有数据**：访问 `http://localhost:6333/collections`，查看 collection 中的 point 数量
3. **确认嵌入模型配置**：上传时必须选择正确的 `embeddingModelId`，且该模型已启用
4. **查看后端日志**：搜索关键词 "Embedding" 或 "VectorStore"，查看是否有异常

### Q: 聊天时不使用知识库，如何配置？
**A**: 在聊天请求中将 `knowledgeType` 设为 `null` 或不传该字段：
```json
{
  "platform": "DEEPSEEK",
  "model": "deepseek-chat",
  "conversationId": "xxx",
  "message": "你好",
  "knowledgeType": null  // 不启用知识库
}
```

### Q: 如何启用 AI 思考过程（reasoningContent）？
**A**: 在聊天请求的 `options` 中添加：
```json
{
  "message": "问题",
  "options": {
    "enableThinking": true  // 启用思考模式
  }
}
```
流式响应会包含 `reasoningContent` 字段，展示 AI 的推理过程。

### Q: 对话回复速度很慢？
**A**: 原因分析：
1. **本地模型**（Ollama）：取决于硬件配置，建议使用 GPU 加速
2. **在线模型**：受网络延迟影响，可切换到国内平台（如通义千问）
3. **RAG 检索**：TOP-K 过大会增加检索时间，建议设置为 3-5
4. **数据库连接池**：检查 R2DBC 连接池配置，适当增加 `max-size`

## 3. 国际化相关

### Q: 如何切换系统语言？
**A**: 
1. **前端**：点击侧边栏底部的语言切换按钮，或前往设置页面切换
2. **API 调用**：在请求头中添加 `Accept-Language: en-US` 或 `Accept-Language: zh-CN`
3. 语言设置会保存在浏览器的 `localStorage` 中

### Q: 后端返回的错误消息是中文，如何改为英文？
**A**: 在请求头中添加 `Accept-Language: en-US`，后端会返回英文错误消息。

### Q: 如何添加新的语言支持？
**A**: 参考环境配置文档中的 [国际化配置](/config/env.html#_5-3-添加新的语言支持) 章节。

## 4. 网页访问工具相关

### Q: AI 访问网页时报错 "RATE_LIMIT"？
**A**: 网页访问工具内置了限流保护，默认配置为每 500ms 最多 50 次请求。如果触发限流，请稍后重试。

### Q: AI 访问网页时报错 "TIMEOUT"？
**A**: 默认超时时间为 20 秒。可能原因：
1. 目标网站响应过慢
2. 网络连接不稳定

建议让 AI 尝试访问其他网站或稍后重试。

### Q: AI 访问网页时返回的内容不完整？
**A**: 网页访问工具会自动提取网页的核心内容，包括：
- 标题
- 描述
- 前 5 个段落
- 最长的 10 个段落

如果内容被截断，可以：
1. 让 AI 访问具体的子页面
2. 分多次询问不同的问题

### Q: 如何禁用 AI 的网页访问功能？
**A**: 目前网页访问工具默认注册到聊天模型中。如需禁用，可在 `PlatformChatOptions` 构建时不添加 `toolCallbacks`。

## 5. 部署与开发

### Q: Maven 下载依赖失败？
**A**: 
1. 项目使用了 Spring Snapshot 仓库，请确保网络可访问 `repo.spring.io`
2. 使用国内镜像时注意 Snapshot 仓库的支持，建议在 `settings.xml` 中配置：
   ```xml
   <mirror>
     <id>aliyun</id>
     <mirrorOf>*,!spring-snapshots</mirrorOf>  <!-- 排除 Snapshot 仓库 -->
     <url>https://maven.aliyun.com/repository/public</url>
   </mirror>
   ```

### Q: 前端访问 API 报错 CORS？
**A**: 
1. 开发环境下前端已配置 Vite 代理，会自动转发到后端
2. 生产环境建议使用 Nginx 反向代理，统一前后端域名
3. 如需开启 CORS，修改 `SecurityConfiguration`：
   ```java
   .cors(cors -> cors.configurationSource(corsConfigurationSource()))
   ```

### Q: JWT Token 过期后如何处理？
**A**: 
1. 后端返回 `A0301` 错误码时，前端会自动清除 localStorage 中的 token
2. 可在 `application-authorization.yml` 中调整 `jwt.timeout`（单位：秒）

## 6. 性能优化

### Q: 如何提升并发性能？
**A**: 优化建议：
1. **R2DBC 连接池**：调整 `application-db.yml` 中的 `initial-size` 和 `max-size`
2. **Redis 连接池**：修改 `application-redis.yml` 中的 Lettuce 连接池配置
3. **向量检索优化**：减少 TOP-K 值，优化 Embedding 模型的维度
4. **启用 WebFlux 全链路异步**：确保所有 Service 层使用 Reactor 类型

---

**其他问题**：如遇到未列出的问题，请提交 GitHub Issues 或联系开发团队（jyc@krismile.cn）。
