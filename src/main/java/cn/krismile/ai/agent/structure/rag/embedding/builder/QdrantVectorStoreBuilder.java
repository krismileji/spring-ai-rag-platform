package cn.krismile.ai.agent.structure.rag.embedding.builder;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.enumeration.rag.VectorStoreTypeEnum;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.ChatModelFactory;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformEmbeddingOptions;
import cn.krismile.ai.agent.structure.rag.embedding.context.CommonVectorStoreContext;
import io.micrometer.observation.ObservationRegistry;
import io.qdrant.client.QdrantClient;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Qdrant 向量存储构建器
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class QdrantVectorStoreBuilder implements VectorStoreBuilder {

    @Resource
    private CommonVectorStoreContext commonVectorStoreContext;
    @Resource
    private QdrantVectorStoreProperties properties;
    @Resource
    private QdrantClient qdrantClient;

    @Override
    public VectorStoreTypeEnum type() {
        return VectorStoreTypeEnum.QDRANT;
    }

    @Override
    public Mono<VectorStore> build(ChatPlatformEnum platform, String model) {
        Mono<EmbeddingModel> embeddingModelMono = ChatModelFactory.builder(platform)
                .embedding(model, PlatformEmbeddingOptions.builder());
        return embeddingModelMono.flatMap(embeddingModel ->
                Mono.fromCallable(() -> QdrantVectorStore.builder(qdrantClient, embeddingModel)
                        .collectionName(properties.getCollectionName())
                        .initializeSchema(properties.isInitializeSchema())
                        .observationRegistry(commonVectorStoreContext.observationRegistry().getIfUnique(() -> ObservationRegistry.NOOP))
                        .customObservationConvention(commonVectorStoreContext.customObservationConvention().getIfAvailable(() -> null))
                        .batchingStrategy(commonVectorStoreContext.batchingStrategy())
                        .build()));
    }
}
