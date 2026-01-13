package cn.krismile.ai.agent.repository.chat;

import cn.krismile.ai.agent.mapper.UserChatConversationMapper;
import cn.krismile.ai.agent.model.domain.UserChatConversationDO;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * UserChatConversationRepositoryImpl
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class UserChatConversationRepositoryImpl extends ServiceImpl<UserChatConversationMapper, UserChatConversationDO> implements UserChatConversationRepository {
}