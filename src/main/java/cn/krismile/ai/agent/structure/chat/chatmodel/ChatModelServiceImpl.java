package cn.krismile.ai.agent.structure.chat.chatmodel;

import cn.krismile.ai.agent.model.enumeration.chat.ChatModelTypeEnum;
import cn.krismile.ai.agent.model.request.model.ChatModelRequest;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import cn.krismile.ai.agent.repository.platform.AiPlatformRepository;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 模型服务实现类
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class ChatModelServiceImpl implements ChatModelService {

    @Resource
    private AiPlatformRepository aiPlatformRepository;

    @Override
    public Flux<ChatModelVO> listChatModels(ChatModelRequest query) {
        return aiPlatformRepository.findByEnabled(true)
                .flatMap(platform -> platform.getPlatform().strategy().listAllModels(ChatModelTypeEnum.CHAT))
                .filter(model -> BooleanUtils.isTrue(model.getEnabled()));
    }
}
