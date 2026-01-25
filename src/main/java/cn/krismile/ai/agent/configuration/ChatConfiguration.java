package cn.krismile.ai.agent.configuration;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.repository.chat.UserChatConversationRepository;
import cn.krismile.ai.agent.repository.chat.UserChatMemoryRepository;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.ChatModelFactory;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformEmbeddingOptions;
import cn.krismile.ai.agent.structure.chat.memory.MessageWindowChatMemory;
import cn.krismile.ai.agent.structure.chat.memory.MysqlChatMemoryRepository;
import cn.krismile.ai.agent.structure.rag.file.FileStorage;
import cn.krismile.ai.agent.structure.rag.file.platform.LocalFileStrategyImpl;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.reactive.TransactionalOperator;

/**
 * ChatConfiguration
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Configuration
public class ChatConfiguration {

    /**
     * 聊天记录仓库
     *
     * @param userChatConversationRepository 用户聊天会话仓库
     * @param userChatMemoryRepository       用户聊天记录仓库
     * @param transactionalOperator          事务操作符
     * @return 聊天记录仓库实例
     * @since 1.0.0
     */
    @Bean
    public ChatMemoryRepository chatMemoryRepository(
            UserChatConversationRepository userChatConversationRepository,
            UserChatMemoryRepository userChatMemoryRepository,
            TransactionalOperator transactionalOperator) {
        return new MysqlChatMemoryRepository(userChatConversationRepository, userChatMemoryRepository, transactionalOperator);
    }

    /**
     * 聊天记录
     *
     * @param chatMemoryRepository 聊天记录仓库
     * @return 聊天记录实例
     * @since 1.0.0
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }

    /**
     * 文件存储
     *
     * @return 文件存储实例
     * @since 1.0.0
     */
    @Bean
    public FileStorage fileStorage() {
        return new LocalFileStrategyImpl("D:/temp/upload");
    }

    /**
     * 嵌入模型
     *
     * @return 嵌入模型实例
     * @since 1.0.0
     */
    @Bean
    @Lazy
    public EmbeddingModel embeddingModel() {
        return ChatModelFactory.builder(ChatPlatformEnum.ALIYUN)
                .embedding("text-embedding-v3", PlatformEmbeddingOptions.builder().dimensions(1024))
                .block();
    }
}
