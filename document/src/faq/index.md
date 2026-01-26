# 常见问题解决方案 (FAQ)

## 1. 启动相关

### Q: 启动时报错 "Connection refused" 连接数据库失败？
**A**: 请检查 `application-db.yml` 中的数据库配置，确保 MySQL 服务已启动，且 R2DBC URL (`r2dbc:mysql://...`) 正确。注意 R2DBC 与 JDBC URL 格式略有不同。

### Q: 报错 "OllamaConnectionException"？
**A**: 请确保本地已安装并启动了 Ollama 服务（默认端口 11434）。可以通过浏览器访问 `http://localhost:11434` 验证。

## 2. 运行时问题

### Q: 上传文档后无法检索到内容？
**A**: 
1. 检查 Qdrant 向量数据库是否正常运行。
2. 查看日志，确认文档解析和 Embedding 过程是否报错。
3. 确认使用的 Embedding 模型是否需要 API Key（如 OpenAI 格式）。

### Q: 对话回复速度很慢？
**A**: 
1. 如果使用本地模型（如 Ollama），取决于本地硬件配置（GPU/CPU）。
2. 如果使用在线模型，受网络延迟影响。
3. RAG 检索过程（查询向量库）通常需要几百毫秒，属于正常范围。

## 3. 依赖问题

### Q: Maven 下载依赖失败？
**A**: 项目使用了 Spring Snapshot 仓库（见 `pom.xml`），请确保网络环境可以访问 `repo.spring.io`。建议配置国内镜像源时注意 Snapshot 仓库的支持。

---
*如果您遇到其他问题，请提交 Issue 或联系开发团队。*
