package cn.krismile.ai.agent.structure.chat.chatmodel.factory.builder;

import cn.krismile.ai.agent.model.domain.AiPlatformDO;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.PlatformModelBuilder;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformChatOptions;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformEmbeddingOptions;
import cn.krismile.ai.agent.structure.chat.model.ChatPlatformDTO;
import cn.krismile.ai.agent.structure.chat.platoform.PlatformService;
import cn.krismile.ai.agent.util.AESEncryptionUtil;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.Optional;

import static cn.krismile.ai.agent.model.domain.table.AiPlatformDOTableDef.AI_PLATFORM_DO;

/**
 * 抽象平台模型构建器
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public abstract class AbstractPlatformModelBuilder implements PlatformModelBuilder {

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
    public ChatModel chat(String model, PlatformChatOptions.Builder builder) {
        ChatPlatformDTO platformConfig = this.queryPlatformConfig();
        builder = Optional.ofNullable(builder).orElseGet(PlatformChatOptions::builder);
        builder.model(model);
        builder.platform(platformConfig);
        return this.chat(builder.build());
    }

    @Override
    public EmbeddingModel embedding(String model, PlatformEmbeddingOptions.Builder builder) {
        ChatPlatformDTO platformConfig = this.queryPlatformConfig();
        builder = Optional.ofNullable(builder).orElseGet(PlatformEmbeddingOptions::builder);
        builder.model(model);
        builder.platform(platformConfig);
        return this.embedding(builder.build());
    }

    /**
     * 查询平台配置
     *
     * @return 平台配置
     * @since 1.0.0
     */
    protected ChatPlatformDTO queryPlatformConfig() {
        return AiPlatformDO.create()
                .where(AI_PLATFORM_DO.PLATFORM.eq(this.platform().getValue()))
                .oneOpt()
                .map(platform -> ChatPlatformDTO.builder()
                        .platform(platform.getPlatform())
                        .apiKey(AESEncryptionUtil.decrypt(platform.getApiKey(), PlatformService.SECRET_KEY))
                        .build())
                .orElseGet(() -> ChatPlatformDTO.builder()
                        .platform(this.platform())
                        .build())
                .validate();
    }
}
