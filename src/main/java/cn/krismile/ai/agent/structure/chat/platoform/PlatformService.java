package cn.krismile.ai.agent.structure.chat.platoform;

import cn.krismile.ai.agent.model.request.chat.ChatPlatformEditRequest;
import cn.krismile.ai.agent.model.response.chat.ChatPlatformVO;

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
     * 编辑平台
     *
     * @param request 请求参数
     * @return 是否编辑成功
     * @since 1.0.0
     */
    Boolean editPlatform(ChatPlatformEditRequest request);

}