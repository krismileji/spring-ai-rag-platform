package cn.krismile.ai.agent.structure.chat.platoform.service;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.request.chat.ChatModelEditRequest;
import cn.krismile.ai.agent.model.request.chat.ChatPlatformEditRequest;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import cn.krismile.ai.agent.model.response.chat.ChatPlatformVO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 平台服务
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface PlatformService {

    String SECRET_KEY = "SpringAiRagPlatformSecretKey32";

    /**
     * 列表
     *
     * @return 模型列表
     * @since 1..0.0
     */
    List<ChatPlatformVO> listPlatforms();

    /**
     * 模型列表
     *
     * @param platform 平台
     * @return 模型列表
     * @since 1.0.0
     */
    Flux<ChatModelVO> listModels(ChatPlatformEnum platform);

    /**
     * 编辑平台
     *
     * @param request 请求参数
     * @return 是否编辑成功
     * @since 1.0.0
     */
    Boolean editPlatform(ChatPlatformEditRequest request);

    /**
     * 编辑模型
     *
     * @param request 请求参数
     * @return 是否编辑成功
     * @since 1.0.0
     */
    Mono<Boolean> editModel(ChatModelEditRequest request);

}