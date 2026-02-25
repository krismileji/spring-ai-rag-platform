package cn.krismile.ai.agent.structure.chat.model;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 聊天平台数据传输对象
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Builder
public class ChatPlatformDTO {

    /**
     * 聊天平台ID
     */
    private Long id;

    /**
     * 聊天平台
     */
    private ChatPlatformEnum platform;

    /**
     * API密钥
     */
    private String apiKey;

    public ChatPlatformDTO validate() {
        if (platform == null) {
            throw new IllegalArgumentException("Platform cannot be null");
        }
        if (StringUtils.isBlank(this.apiKey)) {
            throw new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, "请先配置API密钥");
        }
        return this;
    }
}
