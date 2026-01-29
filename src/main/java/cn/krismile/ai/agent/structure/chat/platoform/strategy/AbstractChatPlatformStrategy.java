package cn.krismile.ai.agent.structure.chat.platoform.strategy;

import cn.krismile.ai.agent.configuration.security.context.SecurityUtils;
import cn.krismile.ai.agent.constant.Knowledge;
import cn.krismile.ai.agent.constant.RedisKey;
import cn.krismile.ai.agent.model.domain.AiModelDO;
import cn.krismile.ai.agent.model.domain.AiPlatformDO;
import cn.krismile.ai.agent.model.enumeration.chat.ChatModelTypeEnum;
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
import cn.krismile.ai.agent.structure.rag.embedding.builder.QdrantVectorStoreBuilder;
import cn.krismile.ai.agent.util.ObjectMapperUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
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
    @Resource
    private QdrantVectorStoreBuilder qdrantVectorStoreBuilder;

    /**
     * 查询模型
     *
     * @param type 模型类型
     * @return 模型列表
     * @since 1.0.0
     */
    protected abstract Flux<ChatModelVO> queryModels(ChatModelTypeEnum type);

    /**
     * 创建聊天客户端
     *
     * @param builder 聊天客户端构建器
     * @return 聊天客户端
     * @since 1.0.0
     */
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
    public Flux<ChatModelVO> listAllModels(ChatModelTypeEnum type) {
        RedisKey.Key cacheKey = RedisKey.chatModel(this.platform(), type);
        return this.reactiveRedisTemplate.opsForHash()
                .values(cacheKey.key())
                .map(obj -> ObjectMapperUtils.convertValue(obj, ChatModelVO.class))
                .switchIfEmpty(Flux.defer(() -> this.queryModels(type)
                        .collectList()
                        .flatMap(models -> {
                            if (models.isEmpty()) return Mono.empty();
                            return this.persistentModel(type, models).flatMap(savedModels ->
                                    this.cacheModel(cacheKey, savedModels).thenReturn(savedModels));
                        })
                        .flatMapMany(Flux::fromIterable)
                ))
                .sort(Comparator.comparingInt(ChatModelVO::getSort));
    }

    @Override
    public Mono<VectorStore> vectorStore(String model) {
        return qdrantVectorStoreBuilder.build(this.platform(), model);
    }

    /**
     * 默认的聊天客户端构建器
     *
     * @param request 聊天请求
     * @return 聊天客户端构建器
     * @since 1.0.0
     */
    protected Mono<ChatClient.Builder> defaultChatClientBuilder(ChatRequest request) {
        return SecurityUtils.getUserId()
                .onErrorResume(e -> Mono.just(-1L))
                .flatMap(loginId -> this.createChatModel(request).flatMap(chatModel -> {
                    ChatClient.Builder builder = ChatClient.builder(chatModel)
                            .defaultUser(promptUserSpec -> promptUserSpec
                                    .metadata(MessageWindowChatMemory.MODEL, request.getModel())
                                    .metadata(Knowledge.MetaData.USER_ID, loginId)
                            )
                            .defaultAdvisors(advisorSpec -> advisorSpec.param(
                                    ChatMemory.CONVERSATION_ID, request.getConversationId()))
                            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory)
                                    .scheduler(BaseAdvisor.DEFAULT_SCHEDULER)
                                    .build());
                    // 处理向量存储
                    return this.handleRagMemory(builder, request);
                }));
    }

    /**
     * 持久化模型
     *
     * @param type   模型类型
     * @param models 模型列表
     * @return 持久化后的模型列表
     * @since 1.0.0
     */
    private Mono<List<ChatModelVO>> persistentModel(ChatModelTypeEnum type, List<ChatModelVO> models) {
        if (models.isEmpty()) return Mono.just(List.of());

        return aiPlatformRepository.findByPlatform(this.platform())
                .switchIfEmpty(Mono.defer(() -> {
                    AiPlatformDO platform = new AiPlatformDO();
                    platform.setPlatform(this.platform());
                    platform.setEnabled(false);
                    return aiPlatformRepository.save(platform);
                }))
                .flatMap(platform -> {
                    Long platformId = platform.getId();
                    List<String> codes = models.stream().map(ChatModelVO::getModel).toList();

                    return aiModelRepository.findByRelPlatformIdAndTypeAndCodeIn(platformId, type, codes)
                            .collectMap(AiModelDO::getCode, Function.identity())
                            .flatMap(modelMap -> {
                                List<AiModelDO> addModels = models.stream()
                                        .peek(model -> {
                                            AiModelDO dbModel = modelMap.get(model.getModel());
                                            if (dbModel != null) {
                                                model.setId(dbModel.getId());
                                                model.setEnabled(dbModel.getEnabled());
                                            }
                                        })
                                        .filter(model -> !modelMap.containsKey(model.getModel()))
                                        .map(model -> {
                                            AiModelDO m = new AiModelDO();
                                            m.setType(model.getType());
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

                                return saveMono.thenReturn(models);
                            });
                });
    }

    /**
     * 缓存模型
     *
     * @param cacheKey 缓存键
     * @param models   模型列表
     * @return 是否缓存成功
     * @since 1.0.0
     */
    private Mono<Boolean> cacheModel(RedisKey.Key cacheKey, List<ChatModelVO> models) {
        return this.reactiveRedisTemplate.opsForHash()
                .putAll(cacheKey.key(), models.stream().collect(Collectors.toMap(
                        ChatModelVO::getModel, Function.identity())));
    }

    /**
     * 解析推理内容
     *
     * @param message 消息
     * @return 推理内容
     * @since 1.0.0
     */
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

    /**
     * 创建聊天模型
     *
     * @param request 聊天请求
     * @return 聊天模型
     * @since 1.0.0
     */
    private Mono<ChatModel> createChatModel(ChatRequest request) {
        String model = request.getModel();
        ChatOptionsRequest options = Optional.ofNullable(request.getOptions()).orElseGet(ChatOptionsRequest::new);
        return ChatModelFactory.builder(this.platform()).chat(model, PlatformChatOptions.builder()
                .enableSearch(options.getEnableSearch())
                .enableThinking(options.getEnableThinking()));
    }

    /**
     * 处理向量存储
     *
     * @param builder 聊天客户端构建器
     * @param request 聊天请求
     * @return 聊天客户端构建器
     * @since 1.0.0
     */
    private Mono<ChatClient.Builder> handleRagMemory(
            ChatClient.@NonNull Builder builder,
            @NonNull ChatRequest request) {
        if (request.getKnowledgeType() == null) {
            return Mono.just(builder);
        }
        return request.getKnowledgeType().knowledgeStrategy().chatMemoryAdvisor().map(builder::defaultAdvisors);
    }
}
