package cn.krismile.ai.agent.structure.chat.chatmodel.factory.builder;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.PlatformModelBuilder;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.context.CommonChatModelContext;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformChatOptions;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatProperties;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekConnectionProperties;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * DeepSeek平台下 Chat 模型的构建器
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class DeepSeekPlatformModelBuilder extends AbstractPlatformModelBuilder implements PlatformModelBuilder {

    private final CommonChatModelContext commonContext;

    private final DeepSeekChatProperties chatProperties;

    private final DeepSeekConnectionProperties connectionProperties;

    @Override
    public ChatPlatformEnum platform() {
        return ChatPlatformEnum.DEEPSEEK;
    }

    @Override
    public ChatModel chat(PlatformChatOptions options) {
        DeepSeekApi api = DeepSeekApi.builder()
                .baseUrl(StringUtils.hasText(this.chatProperties.getBaseUrl())
                        ? this.chatProperties.getBaseUrl()
                        : connectionProperties.getBaseUrl())
                .apiKey(new SimpleApiKey(options.getPlatform().getApiKey()))
                .completionsPath(this.chatProperties.getCompletionsPath())
                .betaPrefixPath(this.chatProperties.getBetaPrefixPath())
                .restClientBuilder(this.commonContext.restClientBuilderProvider().getIfAvailable(RestClient::builder))
                .webClientBuilder(this.commonContext.webClientBuilderProvider().getIfAvailable(WebClient::builder))
                .responseErrorHandler(this.commonContext.responseErrorHandler())
                .build();
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder()
                .deepSeekApi(api)
                .defaultOptions(options.getDeepSeekOptions())
                .toolCallingManager(this.commonContext.toolCallingManager())
                .toolExecutionEligibilityPredicate(this.commonContext.toolExecutionEligibilityPredicate()
                        .getIfUnique(DefaultToolExecutionEligibilityPredicate::new))
                .retryTemplate(this.commonContext.retryTemplate())
                .observationRegistry(this.commonContext.observationRegistry().getIfUnique(() -> ObservationRegistry.NOOP))
                .build();
        this.commonContext.observationConvention().ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }
}
