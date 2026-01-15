package cn.krismile.ai.agent.structure.chat.platoform.strategy;

import cn.dev33.satoken.stp.StpUtil;
import cn.krismile.ai.agent.constant.Knowledge;
import cn.krismile.ai.agent.context.RequestContext;
import cn.krismile.ai.agent.model.domain.AiModelDO;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.request.chat.ChatOptionsRequest;
import cn.krismile.ai.agent.model.request.chat.ChatRequest;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import cn.krismile.ai.agent.model.response.chat.ChatResponse;
import cn.krismile.ai.agent.repository.platform.AiModelRepository;
import cn.krismile.ai.agent.repository.platform.AiPlatformRepository;
import cn.krismile.ai.agent.structure.SchedulerDelegate;
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
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.krismile.ai.agent.model.domain.table.AiModelDOTableDef.AI_MODEL_DO;

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
    private ReactiveRedisTemplate<String, ChatModelVO> reactiveRedisTemplate;
    @Resource
    private AiPlatformRepository aiPlatformRepository;
    @Resource
    private AiModelRepository aiModelRepository;

    private static final Function<ChatPlatformEnum, String> REDIS_MODEL_CACHE_KEY = platform ->
            new StringJoiner(":")
                    .add("ai")
                    .add("platform")
                    .add("models")
                    .add("chat")
                    .add(platform.getValue())
                    .toString();

    // private final AsyncLoadingCache<String, List<ChatModelVO>> modelCache = Caffeine.newBuilder()
    //         .maximumSize(1)
    //         .buildAsync((key, executor) -> this.queryModels().collectList().toFuture());

    protected abstract Flux<ChatModelVO> queryModels();

    protected ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Override
    public Flux<ChatResponse> chat(ChatRequest request) {
        AtomicReference<String> finalReasoningContent = new AtomicReference<>("");
        return this.chatClient(this.defaultChatClientBuilder(request))
                .prompt()
                .user(request.getMessage())
                .options(request.getOptions().toChatOptions(request.getPlatform()))
                .stream()
                .chatResponse()
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
                .doOnComplete(() -> {
                    if (StringUtils.isNotBlank(finalReasoningContent.get())) {
                        this.mysqlChatMemoryRepository.updateReasoningContent(
                                request.getConversationId(), finalReasoningContent.get());
                    }
                });
    }

    @Override
    public Flux<ChatModelVO> listAllModels() {
        String cacheKey = REDIS_MODEL_CACHE_KEY.apply(this.platform());
        return this.reactiveRedisTemplate.opsForZSet()
                .range(cacheKey, Range.unbounded())
                .switchIfEmpty(Flux.defer(() -> this.queryModels()
                        .publishOn(SchedulerDelegate.create(Schedulers.boundedElastic()))
                        .collectList()
                        .flatMapMany(models -> {
                            if (models.isEmpty()) return Flux.empty();

                            // 持久化模型
                            List<ChatModelVO> savedModels = this.persistentModel(models);

                            // 缓存模型
                            return this.cacheModel(cacheKey, savedModels).thenMany(Flux.fromIterable(savedModels));
                        })
                ));
    }

    protected ChatClient.Builder defaultChatClientBuilder(ChatRequest request) {
        Long loginId = RequestContext.syncApply(StpUtil::getLoginIdAsLong);
        ChatClient.Builder builder = ChatClient.builder(this.createChatModel(request))
                // .defaultAdvisors(this.vectorStoreChatMemoryAdvisorBuilder().build())
                .defaultUser(promptUserSpec -> promptUserSpec
                        .metadata(MessageWindowChatMemory.MODEL, request.getModel())
                        .metadata(Knowledge.MetaData.USER_ID, loginId)
                )
                .defaultAdvisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, request.getConversationId()))
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .scheduler(SchedulerDelegate.create(BaseAdvisor.DEFAULT_SCHEDULER))
                        .build());
        builder = this.handleVectorStore(builder, request);
        return builder;
    }

    private List<ChatModelVO> persistentModel(List<ChatModelVO> models) {
        if (models.isEmpty()) return List.of();
        Long platformId = this.aiPlatformRepository.getOrInitIfNull(this.platform()).getId();
        List<String> codes = models.stream().map(ChatModelVO::getModel).toList();
        Set<String> existingCodes = AiModelDO.create()
                .select(AI_MODEL_DO.CODE)
                .where(AI_MODEL_DO.REL_PLATFORM_ID.in(platformId).and(AI_MODEL_DO.CODE.in(codes)))
                .list().stream()
                .map(AiModelDO::getCode)
                .collect(Collectors.toSet());
        List<AiModelDO> addModels = models.stream()
                .filter(model -> !existingCodes.contains(model.getModel()))
                .map(model -> AiModelDO.create()
                        .setCode(model.getModel())
                        .setName(model.getModelName())
                        .setDescription(model.getDescription())
                        .setEnabled(true)
                        .setSort(model.getSort())
                        .setRelPlatformId(platformId))
                .toList();
        if (!addModels.isEmpty()) {
            this.aiModelRepository.saveBatch(addModels);
            Map<String, ChatModelVO> model2Vo = models.stream().collect(Collectors.toMap(
                    ChatModelVO::getModel, Function.identity()));
            addModels.forEach(model -> model2Vo.get(model.getCode()).setId(model.getId()));
        }
        return models;
    }

    private Mono<Long> cacheModel(String cacheKey, List<ChatModelVO> models) {
        return this.reactiveRedisTemplate.opsForZSet()
                .addAll(cacheKey, models.stream()
                        .map(vo -> ZSetOperations.TypedTuple.of(vo, vo.getSort().doubleValue()))
                        .toList());
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

    private ChatModel createChatModel(ChatRequest request) {
        String model = request.getModel();
        ChatOptionsRequest options = Optional.ofNullable(request.getOptions()).orElseGet(ChatOptionsRequest::new);
        return ChatModelFactory.builder(this.platform()).chat(model, PlatformChatOptions.builder()
                .enableSearch(options.getEnableSearch())
                .enableThinking(options.getEnableThinking()));
    }

    private ChatClient.Builder handleVectorStore(
            ChatClient.@NonNull Builder builder,
            @NonNull ChatRequest request) {
        if (request.getKnowledgeType() == null) {
            return builder;
        }
        return builder.defaultAdvisors(request.getKnowledgeType().knowledgeStrategy().chatMemoryAdvisor());
    }
}
