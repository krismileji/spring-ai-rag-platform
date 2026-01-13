package cn.krismile.ai.agent.model.response.chat;

import cn.krismile.ai.agent.model.domain.AiPlatformDO;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.request.chat.ChatOptionsRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 聊天平台 VO
 *
 * @param platform 平台
 * @param existApiKey  是否存在 API 密钥
 * @param defaultOptions 默认选项
 * @param enabled 是否启用
 * @author JiYinchuan
 * @since 1.0.0
 */
@Schema(description = "聊天平台 VO")
public record ChatPlatformVO(
        @Schema(description = "平台")
        ChatPlatformEnum platform,
        @Schema(description = "是否存在 API 密钥")
        Boolean existApiKey,
        @Schema(description = "默认选项")
        ChatOptionsRequest defaultOptions,
        @Schema(description = "是否启用")
        Boolean enabled
) {

    /**
     * 构造方法
     *
     * @param platfrom 平台
     * @return ChatPlatformVO 实例
     * @since 1.0.0
     */
    public static ChatPlatformVO of(AiPlatformDO platfrom) {
        return new ChatPlatformVO(
                platfrom.getPlatform(),
                StringUtils.isNotBlank(platfrom.getApiKey()),
                platfrom.getDefaultOptions(),
                BooleanUtils.isTrue(platfrom.getEnabled()));
    }
}
