---
outline: deep
---

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

## 5. 国际化配置

### 5.1 后端国际化

后端国际化配置位于 `I18nConfiguration.java`：

```java
@Configuration
public class I18nConfiguration {

    /**
     * 支持的语言列表
     */
    public static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(Locale.CHINA, Locale.US);

    /**
     * 默认语言
     */
    public static final Locale DEFAULT_LOCALE = Locale.CHINA;
}
```

语言资源文件位于 `src/main/resources/i18n/` 目录：

```
src/main/resources/i18n/
├── messages.properties        # 默认资源文件
├── messages_zh_CN.properties  # 简体中文
└── messages_en_US.properties  # 英文
```

### 5.2 前端国际化

前端使用 Vue I18n，语言文件位于 `website/src/i18n/locales/`：

```
website/src/i18n/
├── index.ts           # i18n 配置入口
└── locales/
    ├── zh-CN.ts       # 简体中文
    └── en-US.ts       # 英文
```

### 5.3 添加新的语言支持

**后端步骤**：

1. 在 `src/main/resources/i18n/` 创建新的资源文件，如 `messages_ja_JP.properties`
2. 在 `I18nConfiguration.SUPPORTED_LOCALES` 中添加 `Locale.JAPAN`

**前端步骤**：

1. 在 `website/src/i18n/locales/` 创建新的语言文件，如 `ja-JP.ts`
2. 在 `website/src/i18n/index.ts` 中导入并注册新语言

## 6. 示例：最小可运行环境

1. 安装并启动 MySQL、Redis、Qdrant；
2. 配置数据库连接（R2DBC）、Redis、Qdrant 以及 JWT/会话签名；
3. 确认文件存储目录存在并具备读写权限；
4. 执行：

   ```bash
   mvn spring-boot:run
   ```

5. 浏览器访问 `http://localhost:10001/doc.html` 查看在线接口文档，或访问首页应用。

## 7. 生产环境部署建议

### 7.1 Docker Compose 部署（推荐）

使用 Docker Compose 可以一键启动所有依赖服务：

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: yourpassword
      MYSQL_DATABASE: ai_agent
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init-mysql.sql:/docker-entrypoint-initdb.d/init.sql
  
  redis:
    image: redis:latest
    ports:
      - "6379:6379"
  
  qdrant:
    image: qdrant/qdrant:latest
    ports:
      - "6333:6333"
      - "6334:6334"
    volumes:
      - qdrant_data:/qdrant/storage

volumes:
  mysql_data:
  qdrant_data:
```

### 7.2 Nginx 反向代理配置

生产环境建议使用 Nginx 统一前后端：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态资源
    location / {
        root /var/www/website/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:10001/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # SSE 流式响应配置
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 300s;
    }
}
```

### 7.3 生产环境优化

1. **数据库连接池**：
   ```yaml
   spring:
     r2dbc:
       pool:
         initial-size: 10
         max-size: 50
         max-idle-time: 30m
   ```

2. **JVM 参数优化**：
   ```bash
   java -Xms2g -Xmx4g -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=200 \
        -jar spring-ai-rag-platform.jar
   ```

3. **安全配置**：
   - 修改 JWT secret-key 为高强度密钥
   - 修改会话 sign-key
   - 限制 `/platform/model/**` 接口访问权限

4. **日志配置**：
   ```yaml
   logging:
     level:
       root: INFO
       cn.krismile.ai.agent: DEBUG
     file:
       name: /var/log/ai-agent/application.log
   ```
