package cn.krismile.ai.agent.structure.chat.platoform.strategy;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import cn.krismile.ai.agent.structure.chat.platoform.ChatPlatformStrategy;
import jakarta.annotation.Resource;
import org.springframework.ai.model.ollama.autoconfigure.OllamaConnectionDetails;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ollama 聊天平台策略实现
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component(OllamaChatStrategyImpl.BEAN_NAME)
public class OllamaChatStrategyImpl extends AbstractChatPlatformStrategy implements ChatPlatformStrategy {

    public static final String BEAN_NAME = "ollamaChatStrategyImpl";

    @Resource
    private OllamaConnectionDetails connectionDetails;
    @Resource
    private WebClient.Builder webClientBuilder;

    @Override
    public ChatPlatformEnum platform() {
        return ChatPlatformEnum.OLLAMA;
    }

    @Override
    protected Flux<ChatModelVO> queryModels() {
        AtomicInteger modelIndex = new AtomicInteger(0);
        return this.webClientBuilder.baseUrl(connectionDetails.getBaseUrl())
                .build()
                .get()
                .uri("/api/tags")
                .retrieve()
                .bodyToMono(OllamaApi.ListModelResponse.class)
                .map(OllamaApi.ListModelResponse::models)
                .flatMapMany(Flux::fromIterable)
                .map(model -> new ChatModelVO()
                        .setPlatform(this.platform().getValue())
                        .setPlatformName(this.platform().getReasonPhrase())
                        .setModel(model.name())
                        .setModelName(model.name())
                        .setEnabled(true)
                        .setSort(modelIndex.getAndIncrement())
                );
    }
}