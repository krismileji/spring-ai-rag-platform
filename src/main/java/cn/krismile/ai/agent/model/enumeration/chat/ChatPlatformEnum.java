package cn.krismile.ai.agent.model.enumeration.chat;

import cn.krismile.ai.agent.structure.chat.platoform.ChatPlatformStrategy;
import cn.krismile.ai.agent.structure.chat.platoform.strategy.AliYunChatStrategyImpl;
import cn.krismile.ai.agent.structure.chat.platoform.strategy.DeepSeekChatStrategyImpl;
import cn.krismile.ai.agent.structure.chat.platoform.strategy.OllamaChatStrategyImpl;
import host.springboot.framework3.core.enumeration.BaseEnum;
import host.springboot.framework3.core.util.inner.SpringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天平台枚举
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ChatPlatformEnum implements BaseEnum<String> {

    /**
     * Ollama
     */
    OLLAMA("ollama", "Ollama", OllamaChatStrategyImpl.BEAN_NAME),

    /**
     * 阿里云
     */
    ALIYUN("aliyun", "阿里云", AliYunChatStrategyImpl.BEAN_NAME),

    /**
     * Deepseek
     */
    DEEPSEEK("deepseek", "Deepseek", DeepSeekChatStrategyImpl.BEAN_NAME);

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举描述
     */
    private final String reasonPhrase;

    /**
     * 策略 bean 名称
     */
    private final String strategyBeanName;

    /**
     * 获取策略
     *
     * @return 策略
     * @since 1.0.0
     */
    public ChatPlatformStrategy strategy() {
        return SpringUtils.getApplicationContext().getBean(this.strategyBeanName, ChatPlatformStrategy.class);
    }
}