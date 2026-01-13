package cn.krismile.ai.agent.model.request.chat;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 聊天平台编辑请求
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Schema(description = "聊天平台编辑请求")
public class ChatPlatformEditRequest {

    @Schema(description = "聊天平台")
    @NotNull(message = "聊天平台不能为空")
    private ChatPlatformEnum platform;

    @Schema(description = "ApiKey")
    @NotNull(message = "ApiKey不能为空")
    private String apiKey;

    @Schema(description = "是否启用")
    @NotNull(message = "是否启用不能为空")
    private Boolean enabled;

    @Schema(description = "默认配置项")
    private ChatOptionsRequest defaultOptions;

}
