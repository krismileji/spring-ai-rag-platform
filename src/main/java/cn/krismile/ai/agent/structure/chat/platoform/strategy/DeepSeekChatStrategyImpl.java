package cn.krismile.ai.agent.structure.chat.platoform.strategy;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.structure.chat.ChatPlatformStrategy;
import org.springframework.stereotype.Component;

/**
 * 深度搜索聊天平台策略实现
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component(DeepSeekChatStrategyImpl.BEAN_NAME)
public class DeepSeekChatStrategyImpl extends AbstractChatPlatformStrategy implements ChatPlatformStrategy {

    public static final String BEAN_NAME = "deepseekPlatformStrategyImpl";

    @Override
    public ChatPlatformEnum platform() {
        return ChatPlatformEnum.DEEPSEEK;
    }

}
