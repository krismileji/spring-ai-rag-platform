package cn.krismile.ai.agent.service.memory;

import cn.krismile.ai.agent.model.response.chat.ChatConversationVO;
import cn.krismile.ai.agent.model.response.chat.ChatMemoryVO;

import java.util.List;

/**
 * 会话记录服务
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface ChatMemoryService {

    /**
     * 查询会话列表
     *
     * @return 会话列表
     * @since 1.0.0
     */
    List<ChatConversationVO> listConversations();

    /**
     * 查询会话记录
     *
     * @param conversationId 会话 ID
     * @return 会话记录列表
     * @since 1.0.0
     */
    List<ChatMemoryVO> listMemories(String conversationId);

    /**
     * 删除会话
     *
     * @param conversationId 会话 ID
     * @since 1.0.0
     */
    void delConversation(String conversationId);

}
