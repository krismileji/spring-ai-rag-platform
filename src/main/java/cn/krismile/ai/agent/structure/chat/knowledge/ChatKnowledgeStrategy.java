package cn.krismile.ai.agent.structure.chat.knowledge;

import cn.krismile.ai.agent.model.enumeration.chat.ChatKnowledgeTypeEnum;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

import reactor.core.publisher.Mono;

/**
 * 聊天知识库策略
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface ChatKnowledgeStrategy {

    /**
     * 获取知识库类型
     *
     * @return 知识库类型
     * @since 1.0.0
     */
    ChatKnowledgeTypeEnum knowledgeType();

    /**
     * 获取知识库内存顾问
     *
     * @return 知识库内存顾问
     * @since 1.0.0
     */
    Mono<BaseAdvisor> chatMemoryAdvisor();

}
