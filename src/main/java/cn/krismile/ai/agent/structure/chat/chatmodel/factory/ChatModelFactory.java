package cn.krismile.ai.agent.structure.chat.chatmodel.factory;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import host.springboot.framework3.core.util.inner.SpringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 聊天/扩展模型的统一入口工厂
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatModelFactory {

    /**
     * 阿里云平台构建器
     *
     * @return 阿里云平台模型构建器
     * @since 1.0.0
     */
    public static PlatformModelBuilder builder(ChatPlatformEnum paltform) {
        return SpringUtils.getApplicationContext().getBeansOfType(PlatformModelBuilder.class).values().stream()
                .filter(builder -> builder.platform() == paltform)
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, "无法找到平台模型构建器"));
    }
}
