package cn.krismile.ai.agent.structure.chat.chatmodel.factory;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformEmbeddingOptions;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformChatOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import reactor.core.publisher.Mono;

/**
 * 各平台模型构建器顶层接口
 *
 */
public interface PlatformModelBuilder {

    /**
     * 当前构建器对应的平台。
     */
    ChatPlatformEnum platform();

    /**
     * 创建模型
     *
     * @param model   模型
     * @param builder 聊天模型参数
     * @return 模型
     * @since 1.0.0
     */
    Mono<ChatModel> chat(String model, PlatformChatOptions.Builder builder);

    /**
     * 创建嵌入模型
     *
     * @param model   模型
     * @param builder 嵌入模型参数
     * @return 嵌入模型
     * @since 1.0.0
     */
    default Mono<EmbeddingModel> embedding(String model, PlatformEmbeddingOptions.Builder builder) {
        throw new UnsupportedOperationException(this.platform().getValue() + " not supported expander caht model");
    }

    /**
     * 创建扩展模型
     *
     * @return 聊天模型
     * @since 1.0.0
     */
    default Mono<ChatModel> expander() {
        throw new UnsupportedOperationException(this.platform().getValue() + " not supported expander caht model");
    }
}
