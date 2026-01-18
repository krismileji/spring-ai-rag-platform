package cn.krismile.ai.agent.structure.chat.chatmodel.factory.builder;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.PlatformModelBuilder;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.context.CommonChatModelContext;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformEmbeddingOptions;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformChatOptions;
import cn.krismile.ai.agent.structure.chat.model.ChatPlatformDTO;
import com.alibaba.cloud.ai.autoconfigure.dashscope.*;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 阿里云平台下 Chat 模型的构建器
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class AliyunPlatformModelBuilder extends AbstractPlatformModelBuilder implements PlatformModelBuilder {

    private final CommonChatModelContext commonContext;

    private final DashScopeChatProperties chatProperties;

    private final DashScopeEmbeddingProperties embeddingProperties;

    private final DashScopeConnectionProperties connectionProperties;

    private final ObjectProvider<EmbeddingModelObservationConvention> observationConvention;

    @Override
    public ChatPlatformEnum platform() {
        return ChatPlatformEnum.ALIYUN;
    }

    @Override
    protected ChatModel chat(PlatformChatOptions options) {
        DashScopeApi api = this.api(options.getPlatform(), "chat");
        DashScopeChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(api)
                .retryTemplate(this.commonContext.retryTemplate())
                .toolCallingManager(this.commonContext.toolCallingManager())
                .defaultOptions(options.getDashScopeOptions())
                .observationRegistry(this.commonContext.observationRegistry().getIfUnique(() -> ObservationRegistry.NOOP))
                .toolExecutionEligibilityPredicate(this.commonContext.toolExecutionEligibilityPredicate()
                        .getIfUnique(DefaultToolExecutionEligibilityPredicate::new))
                .build();
        this.commonContext.observationConvention().ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }

    @Override
    protected EmbeddingModel embedding(PlatformEmbeddingOptions options) {
        DashScopeApi api = this.api(options.getPlatform(), "embedding");
        DashScopeEmbeddingModel embeddingModel = new DashScopeEmbeddingModel(api,
                embeddingProperties.getMetadataMode(),
                options.getDashScopeOptions(),
                commonContext.retryTemplate(),
                commonContext.observationRegistry().getIfUnique(() -> ObservationRegistry.NOOP));
        this.observationConvention.ifAvailable(embeddingModel::setObservationConvention);
        return embeddingModel;
    }

    @Override
    public Mono<ChatModel> expander() {
        return this.chat("qwen-turbo", PlatformChatOptions.builder().temperature(0.1));
    }

    /**
     * 获取 API
     *
     * @param platform  平台
     * @param modelType 模型类型
     * @return API
     * @since 1.0.0
     */
    private DashScopeApi api(ChatPlatformDTO platform, String modelType) {
        ResolvedConnectionProperties resolved = DashScopeConnectionUtils.resolveConnectionProperties(
                this.connectionProperties, this.chatProperties, modelType);
        return DashScopeApi.builder()
                .apiKey(platform.getApiKey())
                .headers(resolved.headers())
                .baseUrl(resolved.baseUrl())
                .webClientBuilder(this.commonContext.webClientBuilderProvider().getIfAvailable(WebClient::builder))
                .workSpaceId(resolved.workspaceId())
                .restClientBuilder(this.commonContext.restClientBuilderProvider().getIfAvailable(RestClient::builder))
                .responseErrorHandler(this.commonContext.responseErrorHandler())
                .build();
    }
}
