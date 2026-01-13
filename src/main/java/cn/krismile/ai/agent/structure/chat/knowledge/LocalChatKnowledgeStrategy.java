package cn.krismile.ai.agent.structure.chat.knowledge;

import cn.dev33.satoken.stp.StpUtil;
import cn.krismile.ai.agent.constant.Knowledge;
import cn.krismile.ai.agent.context.RequestContext;
import cn.krismile.ai.agent.model.enumeration.chat.ChatKnowledgeTypeEnum;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.structure.SchedulerDelegate;
import cn.krismile.ai.agent.structure.chat.chatmodel.factory.ChatModelFactory;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

/**
 * 本地知识策略实现
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component
public class LocalChatKnowledgeStrategy implements ChatKnowledgeStrategy {

    @Resource
    private VectorStore vectorStore;

    @Override
    public ChatKnowledgeTypeEnum knowledgeType() {
        return ChatKnowledgeTypeEnum.LOCAL;
    }

    @Override
    public BaseAdvisor chatMemoryAdvisor() {
        Long loginId = RequestContext.syncApply(StpUtil::getLoginIdAsLong);
        return RetrievalAugmentationAdvisor.builder()
                .scheduler(SchedulerDelegate.create(BaseAdvisor.DEFAULT_SCHEDULER))
                .queryExpander(MultiQueryExpander.builder()
                        .chatClientBuilder(ChatClient.builder(ChatModelFactory.builder(ChatPlatformEnum.ALIYUN).expander()))
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(0.4)
                        // 过滤用户自己的知识库，序列化会将 Long 转为 String，所以此处需要转换
                        .filterExpression(() -> new FilterExpressionBuilder()
                                .eq(Knowledge.MetaData.USER_ID, String.valueOf(loginId))
                                .build())
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                .build();
    }
}
