package cn.krismile.ai.agent.structure.chat.platoform.strategy;

import cn.krismile.ai.agent.model.enumeration.chat.ChatModelTypeEnum;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import cn.krismile.ai.agent.structure.chat.platoform.ChatPlatformStrategy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

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

    @Override
    protected Flux<ChatModelVO> queryModels(ChatModelTypeEnum type) {
        if (type != ChatModelTypeEnum.CHAT) {
            return Flux.empty();
        }
        AtomicInteger modelIndex = new AtomicInteger(0);
        return Flux.just(
                new ChatModelVO()
                        .setType(ChatModelTypeEnum.CHAT)
                        .setPlatform(this.platform().getValue())
                        .setPlatformName(this.platform().getReasonPhrase())
                        .setModel("deepseek-chat")
                        .setModelName("DeepSeek-V3.2（非思考模式）")
                        .setSort(modelIndex.getAndIncrement()),
                new ChatModelVO()
                        .setType(ChatModelTypeEnum.CHAT)
                        .setPlatform(this.platform().getValue())
                        .setPlatformName(this.platform().getReasonPhrase())
                        .setModel("deepseek-reasoner")
                        .setModelName("DeepSeek-V3.2（思考模式）")
                        .setEnabled(true)
                        .setSort(modelIndex.getAndIncrement()));
    }
}
