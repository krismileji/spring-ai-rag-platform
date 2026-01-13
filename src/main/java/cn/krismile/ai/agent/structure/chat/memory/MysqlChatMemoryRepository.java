package cn.krismile.ai.agent.structure.chat.memory;

import cn.dev33.satoken.stp.StpUtil;
import cn.krismile.ai.agent.context.RequestContext;
import cn.krismile.ai.agent.model.domain.UserChatConversationDO;
import cn.krismile.ai.agent.model.domain.UserChatMemoryDO;
import cn.krismile.ai.agent.model.enumeration.chat.ChatModelEnum;
import com.mybatisflex.core.query.QueryMethods;
import host.springboot.framework3.core.enumeration.BaseEnum;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.krismile.ai.agent.model.domain.table.UserChatConversationDOTableDef.USER_CHAT_CONVERSATION_DO;
import static cn.krismile.ai.agent.model.domain.table.UserChatMemoryDOTableDef.USER_CHAT_MEMORY_DO;

/**
 * Mysql 聊天记录存储
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public class MysqlChatMemoryRepository implements ChatMemoryRepository {

    private static final int MAX_MESSAGES = 30;

    public void updateReasoningContent(@NonNull String conversationId, @NonNull String reasoningContent) {
        Long userId = RequestContext.syncApply(() -> StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null);
        UserChatMemoryDO.create()
                .select(USER_CHAT_MEMORY_DO.ID)
                .where(USER_CHAT_MEMORY_DO.REL_CONVERSATION_ID.eq(conversationId)
                        .and(USER_CHAT_MEMORY_DO.TYPE.eq(MessageType.ASSISTANT))
                        .and(USER_CHAT_MEMORY_DO.REL_USER_ID.eq(userId)))
                .orderBy(USER_CHAT_MEMORY_DO.CREATE_TIME.desc())
                .limit(1)
                .oneOpt().ifPresent(chatMemory -> {
                    UserChatMemoryDO.create()
                            .where(USER_CHAT_MEMORY_DO.ID.eq(chatMemory.getId()))
                            .setReasoningContent(reasoningContent).update();
                });
    }

    @Override
    public @NonNull List<String> findConversationIds() {
        if (!RequestContext.syncApply(StpUtil::isLogin)) {
            return List.of();
        }
        return UserChatMemoryDO.create()
                .select(QueryMethods.distinct(USER_CHAT_MEMORY_DO.REL_CONVERSATION_ID))
                .list().stream()
                .map(UserChatMemoryDO::getRelConversationId)
                .toList();
    }

    @Override
    public @NonNull List<Message> findByConversationId(@NonNull String conversationId) {
        return UserChatMemoryDO.create()
                .where(USER_CHAT_MEMORY_DO.REL_CONVERSATION_ID.eq(conversationId))
                .orderBy(USER_CHAT_MEMORY_DO.CREATE_TIME.desc())
                .limit(MAX_MESSAGES)
                .list().stream()
                .map(item -> switch (item.getType()) {
                    case USER -> new UserMessage(item.getContent());
                    case ASSISTANT -> new AssistantMessage(item.getContent());
                    case SYSTEM -> new SystemMessage(item.getContent());
                    case TOOL -> ToolResponseMessage.builder().responses(List.of()).build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(@NonNull String conversationId, @NonNull List<Message> messages) {
        Long userId = RequestContext.syncApply(() -> StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null);
        if (!UserChatConversationDO.create().where(USER_CHAT_CONVERSATION_DO.ID.eq(conversationId)).exists()) {
            UserChatConversationDO.create()
                    .setId(conversationId)
                    .setContent(messages.getFirst().getText())
                    .setRelUserId(userId)
                    .save();
        }
        UserChatMemoryDO.create().baseMapper().insertBatch(messages.stream()
                .map(message -> UserChatMemoryDO.create()
                        .setModel(this.parseModel(message))
                        .setContent(message.getText())
                        .setType(message.getMessageType())
                        .setRelConversationId(conversationId)
                        .setRelUserId(userId))
                .toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByConversationId(@NonNull String conversationId) {
        UserChatConversationDO.create().where(USER_CHAT_CONVERSATION_DO.ID.eq(conversationId)).remove();
        UserChatMemoryDO.create().where(USER_CHAT_MEMORY_DO.ID.eq(conversationId)).remove();
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
