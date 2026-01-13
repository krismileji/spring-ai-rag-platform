package cn.krismile.ai.agent.model.request.chat;

import cn.krismile.ai.agent.model.enumeration.chat.ChatKnowledgeTypeEnum;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 聊天请求 DTO
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Schema(description = "聊天请求")
public class ChatRequest {

    @Schema(description = "聊天平台")
    @NotNull(message = "聊天平台不能为空")
    private ChatPlatformEnum platform;

    @Schema(description = "聊天模型")
    @NotBlank(message = "聊天模型不能为空")
    private String model;

    @Schema(description = "会话 ID")
    @NotBlank(message = "会话 ID 不能为空")
    private String conversationId;

    @Schema(description = "用户消息")
    @NotBlank(message = "用户消息不能为空")
    private String message;

    @Schema(description = "知识库类型，为空时不启用知识库")
    private ChatKnowledgeTypeEnum knowledgeType;

    @Schema(description = "配置项")
    private ChatOptionsRequest options = new ChatOptionsRequest();

}
