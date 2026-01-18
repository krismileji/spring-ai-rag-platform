package cn.krismile.ai.agent.structure.chat.memory.service;

import cn.krismile.ai.agent.configuration.security.context.SecurityUtils;
import cn.krismile.ai.agent.model.response.chat.ChatConversationVO;
import cn.krismile.ai.agent.model.response.chat.ChatMemoryVO;
import cn.krismile.ai.agent.repository.chat.UserChatConversationRepository;
import cn.krismile.ai.agent.repository.chat.UserChatMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 聊天记录服务实现类
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ChatMemoryServiceImpl implements ChatMemoryService {

    private final UserChatConversationRepository userChatConversationRepository;
    private final UserChatMemoryRepository userChatMemoryRepository;

    @Override
    public Flux<ChatConversationVO> listConversations() {
        return SecurityUtils.getUserId()
                .flatMapMany(userChatConversationRepository::findByRelUserId)
                .map(ChatConversationVO::from);
    }

    @Override
    public Flux<ChatMemoryVO> listMemories(String conversationId) {
        return SecurityUtils.getUserId()
                .flatMapMany(userId -> userChatMemoryRepository.findByRelConversationIdAndRelUserId(conversationId, userId))
                .map(ChatMemoryVO::from);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> delConversation(String conversationId) {
        return userChatConversationRepository.deleteById(conversationId)
                .then(userChatMemoryRepository.deleteByRelConversationId(conversationId));
    }
}
