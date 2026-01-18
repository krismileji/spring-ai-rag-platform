package cn.krismile.ai.agent.structure.chat.platoform.strategy;

import cn.krismile.ai.agent.configuration.security.context.SecurityUtils;
import cn.krismile.ai.agent.constant.Knowledge;
import cn.krismile.ai.agent.model.domain.AiModelDO;
import cn.krismile.ai.agent.model.domain.AiPlatformDO;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.request.chat.ChatOptionsRequest;
import cn.krismile.ai.agent.model.request.chat.ChatRequest;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import cn.krismile.ai.agent.model.response.chat.ChatResponse;
import cn.krismile.ai.agent.repository.platform.AiModelRepository;
import cn.krismile.ai.agent.repository.platform.AiPlatformRepository;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.ChatModelFactory;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformChatOptions;
import cn.krismile.ai.agent.structure.chat.memory.MessageWindowChatMemory;
import cn.krismile.ai.agent.structure.chat.memory.MysqlChatMemoryRepository;
import cn.krismile.ai.agent.structure.chat.platoform.ChatPlatformStrategy;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 聊天平台策略抽象类
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public abstract class AbstractChatPlatformStrategy implements ChatPlatformStrategy {

    @Resource
    private ChatMemory chatMemory;
    @Resource
    private MysqlChatMemoryRepository mysqlChatMemoryRepository;
    @Resource
    private ReactiveRedisTemplate<String, ?> reactiveRedisTemplate;
    @Resource
    private AiPlatformRepository aiPlatformRepository;
    @Resource
    private AiModelRepository aiModelRepository;

    public static final Function<ChatPlatformEnum, String> REDIS_MODEL_CACHE_KEY = platform ->
            new StringJoiner(":")
                    .add("ai")
                    .add("platform")
                    .add("models")
                    .add("chat")
                    .add(platform.getValue())
                    .toString();

    protected abstract Flux<ChatModelVO> queryModels();

    protected ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Override
    public Flux<ChatResponse> chat(ChatRequest request) {
        AtomicReference<String> finalReasoningContent = new AtomicReference<>("");
        return this.defaultChatClientBuilder(request)
                .flatMapMany(builder -> this.chatClient(builder)
                        .prompt()
                        .user(request.getMessage())
                        .options(request.getOptions().toChatOptions(request.getPlatform()))
                        .stream()
                        .chatResponse()
                        .subscribeOn(Schedulers.boundedElastic()))
                .filter(Objects::nonNull)
                .mapNotNull(response -> {
                    String text = response.getResult().getOutput().getText();
                    String reasoningContent = this.parseReasoningContent(response.getResult().getOutput());

                    // 累计深度思考结果
                    finalReasoningContent.set(finalReasoningContent.get() +
                            Optional.ofNullable(reasoningContent).orElse(""));

                    ChatResponse chatResponse = new ChatResponse();
                    chatResponse.setContent(text);
                    chatResponse.setReasoningContent(reasoningContent);
                    return chatResponse.valid() ? chatResponse : null;
                })
                .publishOn(Schedulers.boundedElastic())
                .doOnComplete(() -> {
                    if (StringUtils.isNotBlank(finalReasoningContent.get())) {
                        this.mysqlChatMemoryRepository.updateReasoningContent(
                                request.getConversationId(), finalReasoningContent.get()).subscribe();
                    }
                });
    }

    @Override
    public Flux<ChatModelVO> listAllModels() {
        String cacheKey = REDIS_MODEL_CACHE_KEY.apply(this.platform());
        return this.reactiveRedisTemplate.<String, ChatModelVO>opsForHash()
                .values(cacheKey)
                .switchIfEmpty(Flux.defer(() -> this.queryModels()
                        .collectList()
                        .flatMap(models -> {
                            if (models.isEmpty()) return Mono.empty();
                            return this.persistentModel(models).flatMap(savedModels ->
                                    this.cacheModel(cacheKey, savedModels).thenReturn(savedModels));
                        })
                        .flatMapMany(Flux::fromIterable)
                ))
                .sort(Comparator.comparingInt(ChatModelVO::getSort));
    }

    protected Mono<ChatClient.Builder> defaultChatClientBuilder(ChatRequest request) {
        return SecurityUtils.getUserId()
                .onErrorResume(e -> Mono.just(-1L))
                .flatMap(loginId -> this.createChatModel(request).flatMap(chatModel -> {
                    ChatClient.Builder builder = ChatClient.builder(chatModel)
                            .defaultUser(promptUserSpec -> promptUserSpec
                                    .metadata(MessageWindowChatMemory.MODEL, request.getModel())
                                    .metadata(Knowledge.MetaData.USER_ID, loginId)
                            )
                            .defaultAdvisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, request.getConversationId()))
                            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory)
                                    .scheduler(BaseAdvisor.DEFAULT_SCHEDULER)
                                    .build());
                    return this.handleVectorStore(builder, request);
                }));
    }

    private Mono<List<ChatModelVO>> persistentModel(List<ChatModelVO> models) {
        if (models.isEmpty()) return Mono.just(List.of());

        return aiPlatformRepository.findByPlatform(this.platform())
                .switchIfEmpty(Mono.defer(() -> {
                    AiPlatformDO platform = new AiPlatformDO();
                    platform.setPlatform(this.platform());
                    return aiPlatformRepository.save(platform);
                }))
                .flatMap(platform -> {
                    Long platformId = platform.getId();
                    List<String> codes = models.stream().map(ChatModelVO::getModel).toList();

                    return aiModelRepository.findByRelPlatformIdAndCodeIn(platformId, codes)
                            .collectMap(AiModelDO::getCode, Function.identity())
                            .flatMap(modelMap -> {
                                List<AiModelDO> addModels = models.stream()
                                        .filter(model -> !modelMap.containsKey(model.getModel()))
                                        .map(model -> {
                                            AiModelDO m = new AiModelDO();
                                            m.setCode(model.getModel());
                                            m.setName(model.getModelName());
                                            m.setDescription(model.getDescription());
                                            m.setEnabled(true);
                                            m.setSort(model.getSort());
                                            m.setRelPlatformId(platformId);
                                            return m;
                                        })
                                        .toList();

                                Mono<List<AiModelDO>> saveMono = addModels.isEmpty() ?
                                        Mono.just(Collections.emptyList()) :
                                        aiModelRepository.saveAll(addModels).collectList();

                                return saveMono.map(savedList -> {
                                    Map<String, Long> codeToIdMap = new HashMap<>();
                                    modelMap.forEach((code, doObj) -> codeToIdMap.put(code, doObj.getId()));
                                    savedList.forEach(doObj -> codeToIdMap.put(doObj.getCode(), doObj.getId()));
                                    models.forEach(model -> model.setId(codeToIdMap.get(model.getModel())));
                                    return models;
                                });
                            });
                });
    }

    private Mono<Boolean> cacheModel(String cacheKey, List<ChatModelVO> models) {
        return this.reactiveRedisTemplate.opsForHash()
                .putAll(cacheKey, models.stream().collect(Collectors.toMap(
                        ChatModelVO::getModel, Function.identity())));
    }

    private String parseReasoningContent(AbstractMessage message) {
        Map<String, Object> metadata = message.getMetadata();
        if (metadata.get("reasoningContent") instanceof String reasoningContent) {
            return reasoningContent;
        }
        if (metadata.get("thinking") instanceof String reasoningContent) {
            return reasoningContent;
        }
        return null;
    }

    private Mono<ChatModel> createChatModel(ChatRequest request) {
        String model = request.getModel();
        ChatOptionsRequest options = Optional.ofNullable(request.getOptions()).orElseGet(ChatOptionsRequest::new);
        return ChatModelFactory.builder(this.platform()).chat(model, PlatformChatOptions.builder()
                .enableSearch(options.getEnableSearch())
                .enableThinking(options.getEnableThinking()));
    }

    private Mono<ChatClient.Builder> handleVectorStore(
            ChatClient.@NonNull Builder builder,
            @NonNull ChatRequest request) {
        if (request.getKnowledgeType() == null) {
            return Mono.just(builder);
        }
        return request.getKnowledgeType().knowledgeStrategy().chatMemoryAdvisor()
                .map(builder::defaultAdvisors);
    }
}
