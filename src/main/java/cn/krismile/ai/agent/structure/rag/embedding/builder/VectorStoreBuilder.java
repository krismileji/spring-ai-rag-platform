package cn.krismile.ai.agent.structure.rag.embedding.builder;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.enumeration.rag.VectorStoreTypeEnum;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Mono;

/**
 * 向量存储构建器接口
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface VectorStoreBuilder {

    /**
     * 获取向量存储类型
     *
     * @return 向量存储类型
     * @since 1.0.0
     */
    VectorStoreTypeEnum type();

    /**
     * 构建向量存储
     *
     * @param platform 平台
     * @param model    向量存储模型
     * @return 向量存储
     * @since 1.0.0
     */
    Mono<VectorStore> build(ChatPlatformEnum platform, String model);

}
