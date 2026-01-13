package cn.krismile.ai.agent.model.response.chat;

import cn.krismile.ai.agent.model.domain.UserChatMemoryDO;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.ai.chat.messages.MessageType;

import java.time.LocalDateTime;

/**
 * 聊天记录 VO
 *
 * @param id         聊天记录 ID
 * @param mode       聊天记录模型
 * @param context    聊天记录内容
 * @param type       聊天记录类型
 * @param createTime 聊天记录创建时间
 * @author JiYinchuan
 * @since 1.0.0
 */
@Schema(description = "聊天记录 VO")
public record ChatMemoryVO(
        @Schema(description = "聊天记录 ID", type = "string")
        Long id,
        @Schema(description = "聊天记录模型")
        String mode,
        @Schema(description = "聊天记录内容")
        String context,
        @Schema(description = "聊天记录推理内容")
        String reasoningContent,
        @Schema(description = "聊天记录类型")
        MessageType type,
        @Schema(description = "聊天记录时间")
        LocalDateTime createTime
) {

    public static ChatMemoryVO from(UserChatMemoryDO domain) {
        return new ChatMemoryVO(
                domain.getId(), domain.getModel(),
                domain.getContent(), domain.getReasoningContent(),
                domain.getType(), domain.getCreateTime());
    }
}
