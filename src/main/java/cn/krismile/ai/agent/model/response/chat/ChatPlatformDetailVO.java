package cn.krismile.ai.agent.model.response.chat;

import cn.krismile.ai.agent.model.request.chat.ChatOptionsRequest;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 聊天平台详情 VO
 *
 * @param platform      平台类型
 * @param apiKey        API密钥（已解密）
 * @param defaultOptions 默认配置
 * @author
 * @since 1.0.0
 */
@Schema(description = "聊天平台详情 VO")
public record ChatPlatformDetailVO(
        @Schema(description = "平台类型值")
        String platform,
        @Schema(description = "API密钥")
        String apiKey,
        @Schema(description = "默认配置")
        ChatOptionsRequest defaultOptions
) {

    /**
     * 构造方法
     *
     * @param platform       平台类型
     * @param apiKey         API密钥
     * @param defaultOptions 默认配置
     * @return ChatPlatformDetailVO 实例
     */
    public static ChatPlatformDetailVO of(String platform, String apiKey, ChatOptionsRequest defaultOptions) {
        return new ChatPlatformDetailVO(platform, apiKey, defaultOptions);
    }
}