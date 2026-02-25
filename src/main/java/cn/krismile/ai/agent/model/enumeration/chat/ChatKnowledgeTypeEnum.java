package cn.krismile.ai.agent.model.enumeration.chat;

import cn.krismile.ai.agent.structure.chat.knowledge.ChatKnowledgeStrategy;
import cn.krismile.ai.agent.structure.chat.knowledge.LocalChatKnowledgeStrategy;
import host.springboot.framework3.core.enumeration.BaseEnum;
import host.springboot.framework3.core.util.inner.SpringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天知识库类型枚举
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ChatKnowledgeTypeEnum implements BaseEnum<String> {

    /**
     * LOCAL
     */
    LOCAL("LOCAL", "enum.chat_knowledge_type.local", LocalChatKnowledgeStrategy.class),

    /**
     * 阿里云百炼平台
     */
    ALI_BAI_LIAN("ALI_BAI_LIAN", "enum.chat_knowledge_type.ali_bai_lian", LocalChatKnowledgeStrategy.class);

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举描述
     */
    private final String reasonPhrase;

    /**
     * 知识库策略类
     */
    private final Class<? extends ChatKnowledgeStrategy> knowledgeStrategyClass;

    /**
     * 获取知识库策略
     *
     * @return 聊天模型
     * @since 1.0.0
     */
    public ChatKnowledgeStrategy knowledgeStrategy() {
        return SpringUtils.getApplicationContext().getBean(this.knowledgeStrategyClass);
    }
}
