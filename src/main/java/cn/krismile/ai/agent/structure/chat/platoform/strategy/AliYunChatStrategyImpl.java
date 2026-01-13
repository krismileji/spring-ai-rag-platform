package cn.krismile.ai.agent.structure.chat.platoform.strategy;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.structure.chat.ChatPlatformStrategy;
import org.springframework.stereotype.Component;

/**
 * 阿里云聊天平台策略实现
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component(AliYunChatStrategyImpl.BEAN_NAME)
public class AliYunChatStrategyImpl extends AbstractChatPlatformStrategy implements ChatPlatformStrategy {

    public static final String BEAN_NAME = "aliYunPlatformStrategyImpl";

    @Override
    public ChatPlatformEnum platform() {
        return ChatPlatformEnum.ALIYUN;
    }

}