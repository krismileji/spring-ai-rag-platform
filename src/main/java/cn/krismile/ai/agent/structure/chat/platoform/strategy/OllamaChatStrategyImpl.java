package cn.krismile.ai.agent.structure.chat.platoform.strategy;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.structure.chat.ChatPlatformStrategy;
import org.springframework.stereotype.Component;

/**
 * Ollama聊天平台策略实现
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component(OllamaChatStrategyImpl.BEAN_NAME)
public class OllamaChatStrategyImpl extends AbstractChatPlatformStrategy implements ChatPlatformStrategy {

    public static final String BEAN_NAME = "ollamaChatStrategyImpl";

    @Override
    public ChatPlatformEnum platform() {
        return ChatPlatformEnum.OLLAMA;
    }

}