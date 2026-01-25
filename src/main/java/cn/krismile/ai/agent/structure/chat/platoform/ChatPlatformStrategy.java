package cn.krismile.ai.agent.structure.chat.platoform;

import cn.krismile.ai.agent.model.enumeration.chat.ChatModelTypeEnum;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.request.chat.ChatRequest;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import cn.krismile.ai.agent.model.response.chat.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
     * @param type 模型类型
     * @return 模型列表
     * @since 1.0.0
     */
    Flux<ChatModelVO> listAllModels(ChatModelTypeEnum type);

    /**
     * 聊天
     *
     * @param request 请求
     * @return 响应
     * @since 1.0.0
     */
    Flux<ChatResponse> chat(ChatRequest request);

    /**
     * 获取向量存储
     *
     * @param model 模型
     * @return 向量存储
     * @since 1.0.0
     */
    Mono<VectorStore> vectorStore(String model);

}