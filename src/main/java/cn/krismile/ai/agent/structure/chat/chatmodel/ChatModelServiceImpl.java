package cn.krismile.ai.agent.structure.chat.chatmodel;

import cn.krismile.ai.agent.model.domain.SysDictDO;
import cn.krismile.ai.agent.model.enumeration.DictTypeEnum;
import cn.krismile.ai.agent.model.request.model.ChatModelRequest;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.krismile.ai.agent.model.domain.table.SysDictDOTableDef.SYS_DICT_DO;

/**
 * 模型服务实现类
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class ChatModelServiceImpl implements ChatModelService {


    @Override
    public List<ChatModelVO> listChatModels(ChatModelRequest query) {
        List<SysDictDO> models = SysDictDO.create()
                .where(SYS_DICT_DO.TYPE.eq(DictTypeEnum.CHAT_MODEL))
                .withRelations()
                .list();
        if (CollectionUtils.isEmpty(models)) {
            return List.of();
        }
        return models.stream()
                .map(model -> {
                    SysDictDO parent = model.getParent();
                    return new ChatModelVO()
                            .setPlatform(parent.getValue())
                            .setPlatformName(parent.getName())
                            .setModel(model.getValue())
                            .setModelName(model.getName());
                })
                .toList();
    }
}
