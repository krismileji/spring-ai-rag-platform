package cn.krismile.ai.agent.structure.chat.memory.service;

import cn.krismile.ai.agent.model.response.chat.ChatConversationVO;
import cn.krismile.ai.agent.model.response.chat.ChatMemoryVO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 聊天记录服务
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface ChatMemoryService {

    /**
     * 查询会话
     *
     * @return 会话列表
     */
    Flux<ChatConversationVO> listConversations();

    /**
     * 查询会话记录
     *
     * @param conversationId 会话 ID
     * @return 会话记录列表
     */
    Flux<ChatMemoryVO> listMemories(String conversationId);

    /**
     * 删除会话
     *
     * @param conversationId 会话 ID
     */
    Mono<Void> delConversation(String conversationId);
}
