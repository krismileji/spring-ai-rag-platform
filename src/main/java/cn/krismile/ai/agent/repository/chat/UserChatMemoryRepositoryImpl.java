package cn.krismile.ai.agent.repository.chat;

import cn.krismile.ai.agent.mapper.UserChatMemoryMapper;
import cn.krismile.ai.agent.model.domain.UserChatMemoryDO;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * UserChatMemoryRepositoryImpl
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class UserChatMemoryRepositoryImpl extends ServiceImpl<UserChatMemoryMapper, UserChatMemoryDO> implements UserChatMemoryRepository {
}