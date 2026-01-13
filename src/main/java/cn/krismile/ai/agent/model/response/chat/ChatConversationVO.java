package cn.krismile.ai.agent.model.response.chat;

import cn.krismile.ai.agent.model.domain.UserChatConversationDO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 聊天会话 VO
 *
 * @param id         会话 ID
 * @param content    会话内容
 * @param createTime 会话创建时间
 * @author JiYinchuan
 * @since 1.0.0
 */
@Schema(description = "聊天会话 VO")
public record ChatConversationVO(
        @Schema(description = "会话 ID")
        String id,
        @Schema(description = "会话内容")
        String content,
        @Schema(description = "会话创建时间")
        LocalDateTime createTime
) {

    public static ChatConversationVO from(UserChatConversationDO domain) {
        return new ChatConversationVO(domain.getId(), domain.getContent(), domain.getCreateTime());
    }
}
