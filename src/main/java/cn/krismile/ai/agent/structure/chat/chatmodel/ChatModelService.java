package cn.krismile.ai.agent.structure.chat.chatmodel;

import cn.krismile.ai.agent.model.request.model.ChatModelRequest;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import reactor.core.publisher.Flux;

/**
 * 模型服务
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface ChatModelService {

    /**
     * 列表
     *
     * @param query 查询参数
     * @return 模型列表
     */
    Flux<ChatModelVO> listChatModels(ChatModelRequest query);

}
