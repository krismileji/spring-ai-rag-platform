package cn.krismile.ai.agent.model.request.chat;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 聊天模型查询请求
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Schema(description = "聊天模型查询请求")
public class ChatModelQueryRequest {

    @Schema(description = "聊天平台")
    @NotEmpty(message = "聊天平台不能为空")
    private List<ChatPlatformEnum> platforms;

}
