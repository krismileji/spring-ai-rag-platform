package cn.krismile.ai.agent.structure.chat.memory;

import cn.krismile.ai.agent.configuration.security.context.SecurityUtils;
import cn.krismile.ai.agent.model.domain.BaseDO;
import cn.krismile.ai.agent.model.domain.UserChatConversationDO;
import cn.krismile.ai.agent.model.domain.UserChatMemoryDO;
import cn.krismile.ai.agent.repository.chat.UserChatConversationRepository;
import cn.krismile.ai.agent.repository.chat.UserChatMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.*;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Mysql 聊天记录存储
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class MysqlChatMemoryRepository implements ChatMemoryRepository {

    private final UserChatConversationRepository userChatConversationRepository;
    private final UserChatMemoryRepository userChatMemoryRepository;
    private final TransactionalOperator transactionalOperator;

    private static final int MAX_MESSAGES = 30;

    public Mono<Void> updateReasoningContent(@NonNull String conversationId, @NonNull String reasoningContent) {
        Long userId;
        try {
            userId = SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
            return Mono.empty();
        }

        return userChatMemoryRepository.findByRelConversationIdAndRelUserId(conversationId, userId)
                .filter(m -> m.getType() == MessageType.ASSISTANT)
                .sort((m1, m2) -> m2.getCreateTime().compareTo(m1.getCreateTime()))
                .flatMap(chatMemory -> {
                    chatMemory.setReasoningContent(reasoningContent);
                    return userChatMemoryRepository.save(chatMemory);
                })
                .then();
    }

    @Override
    public @NonNull List<String> findConversationIds() {
        Long userId;
        try {
            userId = SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
            return List.of();
        }

        return userChatMemoryRepository.findDistinctRelConversationIdByRelUserId(userId)
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    @Override
    public @NonNull List<Message> findByConversationId(@NonNull String conversationId) {
        Long userId;
        try {
            userId = SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
            return List.of();
        }

        return userChatMemoryRepository.findByRelConversationIdAndRelUserId(conversationId, userId)
                .sort(Comparator.comparing(BaseDO::getCreateTime))
                .take(MAX_MESSAGES)
                .map(item -> (Message) switch (item.getType()) {
                    case USER -> new UserMessage(item.getContent());
                    case ASSISTANT -> new AssistantMessage(item.getContent());
                    case SYSTEM -> new SystemMessage(item.getContent());
                    case TOOL -> ToolResponseMessage.builder().responses(List.of()).build();
                })
                .collectList()
                .blockOptional()
                .orElse(List.of());
    }

    @Override
    public void saveAll(@NonNull String conversationId, @NonNull List<Message> messages) {
        Long userId;
        try {
            userId = SecurityUtils.getCurrentUserId();
        } catch (Exception e) {
            return;
        }

        userChatConversationRepository.existsById(conversationId)
                .flatMap(exists -> {
                    if (BooleanUtils.isNotTrue(exists)) {
                        UserChatConversationDO conversation = new UserChatConversationDO();
                        conversation.setId(conversationId);
                        conversation.setContent(messages.getFirst().getText());
                        conversation.setRelUserId(userId);
                        conversation.setNew(true);
                        return userChatConversationRepository.save(conversation).then();
                    }
                    return Mono.empty();
                })
                .then(Mono.defer(() -> {
                    List<UserChatMemoryDO> memoryDOs = messages.stream()
                            .map(message -> {
                                UserChatMemoryDO memory = new UserChatMemoryDO();
                                memory.setModel(this.parseModel(message));
                                memory.setContent(message.getText());
                                memory.setType(message.getMessageType());
                                memory.setRelConversationId(conversationId);
                                memory.setRelUserId(userId);
                                return memory;
                            })
                            .toList();
                    return userChatMemoryRepository.saveAll(memoryDOs).collectList().then();
                }))
                .as(transactionalOperator::transactional)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    @Override
    public void deleteByConversationId(@NonNull String conversationId) {
        userChatConversationRepository.deleteById(conversationId)
                .then(userChatMemoryRepository.deleteByRelConversationId(conversationId))
                .as(transactionalOperator::transactional)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }


    private String parseModel(Message message) {
        Map<String, Object> metadata = message.getMetadata();
        if (MapUtils.isEmpty(metadata)) {
            return null;
        }
        Object model = metadata.get(MessageWindowChatMemory.MODEL);
        if (model instanceof String modelString) {
            return modelString;
        }
        return null;
    }
}
