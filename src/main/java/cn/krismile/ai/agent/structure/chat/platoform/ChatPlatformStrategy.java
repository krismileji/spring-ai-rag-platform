package cn.krismile.ai.agent.structure.chat.platoform;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.request.chat.ChatRequest;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import cn.krismile.ai.agent.model.response.chat.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * 聊天平台策略
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface ChatPlatformStrategy {

    /**
     * 获取平台枚举
     *
     * @return 平台枚举
     * @since 1.0.0
     */
    ChatPlatformEnum platform();

    /**
     * 获取所有模型
     *
     * @return 模型列表
     * @since 1.0.0
     */
    default Flux<ChatModelVO> listAllModels() {
        return Flux.empty();
    }

    /**
     * 聊天
     *
     * @param request 请求
     * @return 响应
     * @since 1.0.0
     */
    Flux<ChatResponse> chat(ChatRequest request);

}