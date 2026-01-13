package cn.krismile.ai.agent.service.memory;

import cn.dev33.satoken.stp.StpUtil;
import cn.krismile.ai.agent.context.RequestContext;
import cn.krismile.ai.agent.model.domain.UserChatConversationDO;
import cn.krismile.ai.agent.model.domain.UserChatMemoryDO;
import cn.krismile.ai.agent.model.response.chat.ChatConversationVO;
import cn.krismile.ai.agent.model.response.chat.ChatMemoryVO;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.krismile.ai.agent.model.domain.table.UserChatConversationDOTableDef.USER_CHAT_CONVERSATION_DO;
import static cn.krismile.ai.agent.model.domain.table.UserChatMemoryDOTableDef.USER_CHAT_MEMORY_DO;

/**
 * 聊天记录服务实现类
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class ChatMemoryServiceImpl implements ChatMemoryService {

    @Override
    public List<ChatConversationVO> listConversations() {
        Long loginId = RequestContext.syncApply(StpUtil::getLoginIdAsLong);
        return UserChatConversationDO.create()
                .where(USER_CHAT_CONVERSATION_DO.REL_USER_ID.eq(loginId))
                .orderBy(USER_CHAT_CONVERSATION_DO.CREATE_TIME.desc())
                .list().stream()
                .map(ChatConversationVO::from)
                .toList();
    }

    @Override
    public List<ChatMemoryVO> listMemories(String conversationId) {
        Long loginId = RequestContext.syncApply(StpUtil::getLoginIdAsLong);
        return UserChatMemoryDO.create()
                .where(USER_CHAT_MEMORY_DO.REL_CONVERSATION_ID.eq(conversationId))
                .where(USER_CHAT_MEMORY_DO.REL_USER_ID.eq(loginId))
                .list().stream()
                .map(ChatMemoryVO::from)
                .toList();
    }

    @Override
    public void delConversation(String conversationId) {
        UserChatConversationDO.create().where(USER_CHAT_CONVERSATION_DO.ID.eq(conversationId)).remove();
        UserChatMemoryDO.create().where(USER_CHAT_MEMORY_DO.REL_CONVERSATION_ID.eq(conversationId)).remove();
    }
}
