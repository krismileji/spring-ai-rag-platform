package cn.krismile.ai.agent.structure.chat.platoform.strategy;

import cn.dev33.satoken.stp.StpUtil;
import cn.krismile.ai.agent.constant.Knowledge;
import cn.krismile.ai.agent.context.RequestContext;
import cn.krismile.ai.agent.model.request.chat.ChatOptionsRequest;
import cn.krismile.ai.agent.model.request.chat.ChatRequest;
import cn.krismile.ai.agent.model.response.chat.ChatResponse;
import cn.krismile.ai.agent.structure.SchedulerDelegate;
import cn.krismile.ai.agent.structure.chat.ChatPlatformStrategy;
import cn.krismile.ai.agent.structure.chat.memory.MessageWindowChatMemory;
import cn.krismile.ai.agent.structure.chat.memory.MysqlChatMemoryRepository;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.ChatModelFactory;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.options.PlatformChatOptions;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatModel;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
