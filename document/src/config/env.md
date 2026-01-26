# 开发环境配置指南

为了顺利运行 **Spring AI RAG Platform**，请确保您的开发环境满足以下要求。

## 1. 基础环境

- **JDK**: 21 或更高版本。
- **Maven**: 3.8+。
- **IDE**: IntelliJ IDEA（推荐）或 VS Code。
- **操作系统**: Windows / macOS / Linux 均可。

> 应用默认监听端口为 **10001**，可在 `application.yml` 中通过 `server.port` 修改。

## 2. 数据与缓存配置

### 2.1 MySQL

项目使用 MySQL 作为关系型数据库，并采用 R2DBC 驱动。

- 推荐版本: 8.0+
- 建议先创建业务库（例如 `spring_ai_rag`），并在对应环境的配置文件中设置连接信息。
- 连接池相关配置可在 `application-db.yml` 中调整：
  - `spring.r2dbc.pool.initial-size`
  - `spring.r2dbc.pool.max-size`
  - `spring.r2dbc.pool.max-idle-time`
  - `spring.r2dbc.pool.max-life-time`
  - `spring.r2dbc.pool.validation-query`

> 实际的 R2DBC 连接 URL、用户名和密码通常位于具体环境配置（如 `application-primary.yml` 或外部配置中心）中，请根据部署环境自行调整。

### 2.2 Redis

用于缓存会话和临时数据。

- 推荐版本: 6.0+
- 默认端口: 6379
- 连接与连接池相关配置可在 `application-redis.yml` 中调整：
  - `spring.data.redis.database`
  - `spring.data.redis.connect-timeout`
  - `spring.data.redis.lettuce.pool.*`

### 2.3 向量数据库 Qdrant

项目使用 **Qdrant** 作为向量数据库，用于存储知识库分片向量。

- 推荐使用 Docker 快速启动：

  ```bash
  docker run -p 6333:6333 -p 6334:6334 \
      -v $(pwd)/qdrant_storage:/qdrant/storage \
      qdrant/qdrant
  ```

- 相关配置位于 `application-ai.yml`：

  ```yaml
  spring:
    ai:
      vectorstore:
        qdrant:
          collection-name: vector_store       # 默认向量集合名称
          initialize-schema: true             # 是否在启动时自动初始化 schema
  ```

## 3. AI 模型与会话配置

### 3.1 聊天/嵌入模型管理

模型与平台信息存储在数据库中（`ai_platform`、`ai_model` 等表），并通过后台接口进行配置：

- 聊天模型与嵌入模型统一使用枚举 **`ChatModelTypeEnum`** 区分：
  - `CHAT`: 聊天模型
  - `EMBEDDING`: 嵌入模型
- 向量存储由 `VectorStoreBuilder` 与 `QdrantVectorStoreBuilder` 动态构建，不再在配置文件中硬编码具体模型。

> 在生产环境中，建议先通过平台管理接口配置好各个平台及其模型，再在知识库文件中选择对应的嵌入模型 ID。

### 3.2 会话签名配置

会话 ID 生成和校验使用签名 key，相关配置位于 `application-ai.yml`：

```yaml
project:
  conversation:
    sign-key: your-secure-32+character-secret-key
```

请务必将 `sign-key` 替换为长度 **32 字符以上** 的随机字符串，并在不同环境中使用不同的值。

### 3.3 JWT 鉴权配置

JWT 相关配置位于 `application-authorization.yml`：

```yaml
project:
  jwt:
    timeout: 2592000   # token 有效期（秒），默认约 30 天，-1 代表永久有效
    secret-key: krismile666krismile666krismile666krismile666
```

- `timeout`: 根据实际安全策略调整有效期；
- `secret-key`: 建议在生产环境中替换为随机、高强度的密钥，并通过环境变量或配置中心管理。

## 4. 文件存储配置

系统默认使用本地文件系统存储上传的原始文档：

- 在 `ChatConfiguration` 中：

  ```java
  @Bean
  public FileStorage fileStorage() {
      return new LocalFileStrategyImpl("D:/temp/upload");
  }
  ```

- 这意味着所有上传的文件会保存在 `D:/temp/upload` 目录下。
- 建议：
  - Windows 环境可直接使用默认路径；
  - Linux / macOS 环境应修改为合适的目录（例如 `/data/ai-rag/upload`），并确保运行账号有读写权限。

## 5. 示例：最小可运行环境

1. 安装并启动 MySQL、Redis、Qdrant；
2. 配置数据库连接（R2DBC）、Redis、Qdrant 以及 JWT/会话签名；
3. 确认文件存储目录存在并具备读写权限；
4. 执行：

   ```bash
   mvn spring-boot:run
   ```

5. 浏览器访问 `http://localhost:10001/doc.html` 查看在线接口文档，或访问首页应用。
