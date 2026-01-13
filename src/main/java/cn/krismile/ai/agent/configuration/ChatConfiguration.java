package cn.krismile.ai.agent.configuration;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.ChatModelFactory;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformEmbeddingOptions;
import cn.krismile.ai.agent.structure.chat.memory.MessageWindowChatMemory;
import cn.krismile.ai.agent.structure.chat.memory.MysqlChatMemoryRepository;
import cn.krismile.ai.agent.structure.rag.file.FileStorage;
import cn.krismile.ai.agent.structure.rag.file.platform.LocalFileStrategyImpl;
import io.micrometer.observation.ObservationRegistry;
import io.qdrant.client.QdrantClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * ChatConfiguration
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Configuration
public class ChatConfiguration {

    @Bean
    public ChatMemoryRepository chatMemoryRepository() {
        return new MysqlChatMemoryRepository();
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }

    @Bean
    public FileStorage fileStorage() {
        return new LocalFileStrategyImpl("D:/temp/upload");
    }

    @Bean
    @Lazy
    public EmbeddingModel embeddingModel() {
        return ChatModelFactory.builder(ChatPlatformEnum.ALIYUN)
                .embedding("text-embedding-v3", PlatformEmbeddingOptions.builder().dimensions(1024));
    }

    @Bean
    public QdrantVectorStore vectorStore(
            EmbeddingModel dashscopeEmbeddingModel, QdrantVectorStoreProperties properties,
            QdrantClient qdrantClient, ObjectProvider<ObservationRegistry> observationRegistry,
            ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
            BatchingStrategy batchingStrategy) {
        return QdrantVectorStore.builder(qdrantClient, dashscopeEmbeddingModel)
                .collectionName(properties.getCollectionName())
                .initializeSchema(properties.isInitializeSchema())
                .observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
                .customObservationConvention(customObservationConvention.getIfAvailable(() -> null))
                .batchingStrategy(batchingStrategy)
                .build();
    }
}
