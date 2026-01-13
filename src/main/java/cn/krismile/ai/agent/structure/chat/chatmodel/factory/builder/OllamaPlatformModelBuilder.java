package cn.krismile.ai.agent.structure.chat.chatmodel.factory.builder;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.PlatformModelBuilder;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.context.CommonChatModelContext;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformChatOptions;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.ollama.autoconfigure.OllamaChatProperties;
import org.springframework.ai.model.ollama.autoconfigure.OllamaInitializationProperties;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.stereotype.Component;

/**
 * Ollama平台下 Chat 模型的构建器
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class OllamaPlatformModelBuilder extends AbstractPlatformModelBuilder implements PlatformModelBuilder {

    private final CommonChatModelContext commonContext;

    private final OllamaChatProperties chatProperties;

    private final OllamaInitializationProperties initProperties;

    private final OllamaApi ollamaApi;

    @Override
    public ChatPlatformEnum platform() {
        return ChatPlatformEnum.OLLAMA;
    }

    @Override
    public ChatModel chat(PlatformChatOptions options) {
        PullModelStrategy chatModelPullStrategy = this.initProperties.getChat().isInclude()
                ? this.initProperties.getPullModelStrategy()
                : PullModelStrategy.NEVER;
        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(this.ollamaApi)
                .defaultOptions(options.getOllamaOptions())
                .toolCallingManager(this.commonContext.toolCallingManager())
                .toolExecutionEligibilityPredicate(this.commonContext.toolExecutionEligibilityPredicate()
                        .getIfUnique(DefaultToolExecutionEligibilityPredicate::new))
                .observationRegistry(this.commonContext.observationRegistry().getIfUnique(() -> ObservationRegistry.NOOP))
                .modelManagementOptions(new ModelManagementOptions(chatModelPullStrategy,
                        this.initProperties.getChat().getAdditionalModels(),
                        this.initProperties.getTimeout(), this.initProperties.getMaxRetries()))
                .retryTemplate(this.commonContext.retryTemplate())
                .build();
        this.commonContext.observationConvention().ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }
}
