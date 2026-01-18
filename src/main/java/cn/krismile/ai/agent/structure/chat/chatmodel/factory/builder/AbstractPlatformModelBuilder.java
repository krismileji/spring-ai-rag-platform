package cn.krismile.ai.agent.structure.chat.chatmodel.factory.builder;

import cn.krismile.ai.agent.repository.platform.AiPlatformRepository;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.PlatformModelBuilder;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformChatOptions;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformEmbeddingOptions;
import cn.krismile.ai.agent.structure.chat.model.ChatPlatformDTO;
import cn.krismile.ai.agent.structure.chat.platoform.service.PlatformService;
import cn.krismile.ai.agent.util.AESEncryptionUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 抽象平台模型构建器
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public abstract class AbstractPlatformModelBuilder implements PlatformModelBuilder {

    @Resource
    private AiPlatformRepository aiPlatformRepository;

    /**
     * 创建聊天模型
     *
     * @param options 平台选项
     * @return 聊天模型
     * @since 1.0.0
     */
    protected abstract ChatModel chat(PlatformChatOptions options);

    /**
     * 创建嵌入模型
     *
     * @param options 平台选项
     * @return 嵌入模型
     * @since 1.0.0
     */
    protected EmbeddingModel embedding(PlatformEmbeddingOptions options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<ChatModel> chat(String model, PlatformChatOptions.Builder builder) {
        return this.queryPlatformConfig().map(platformConfig -> {
            PlatformChatOptions.Builder finalBuilder = Optional.ofNullable(builder).orElseGet(PlatformChatOptions::builder);
            finalBuilder.model(model);
            finalBuilder.platform(platformConfig);
            return this.chat(finalBuilder.build());
        });
    }

    @Override
    public Mono<EmbeddingModel> embedding(String model, PlatformEmbeddingOptions.Builder builder) {
        return this.queryPlatformConfig().map(platformConfig -> {
            PlatformEmbeddingOptions.Builder finalBuilder = Optional.ofNullable(builder).orElseGet(PlatformEmbeddingOptions::builder);
            finalBuilder.model(model);
            finalBuilder.platform(platformConfig);
            return this.embedding(finalBuilder.build());
        });
    }

    /**
     * 查询平台配置
     *
     * @return 平台配置
     * @since 1.0.0
     */
    protected Mono<ChatPlatformDTO> queryPlatformConfig() {
        return this.aiPlatformRepository.findByPlatform(this.platform())
                .map(p -> ChatPlatformDTO.builder()
                        .platform(p.getPlatform())
                        .apiKey(AESEncryptionUtil.decrypt(p.getApiKey(), PlatformService.SECRET_KEY))
                        .build())
                .switchIfEmpty(Mono.defer(() -> Mono.just(ChatPlatformDTO.builder().platform(this.platform()).build())))
                .map(ChatPlatformDTO::validate);
    }
}
