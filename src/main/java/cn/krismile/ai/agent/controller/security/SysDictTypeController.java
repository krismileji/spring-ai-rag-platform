package cn.krismile.ai.agent.controller.security;

import cn.krismile.ai.agent.model.domain.SysDictDO;
import cn.krismile.ai.agent.model.domain.SysDictTypeDO;
import cn.krismile.ai.agent.model.enumeration.DictTypeEnum;
import cn.krismile.ai.agent.model.enumeration.chat.ChatModelEnum;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import host.springboot.framework3.core.response.R;
import host.springboot.framework3.core.response.vo.VO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.krismile.ai.agent.model.domain.table.SysDictDOTableDef.SYS_DICT_DO;
import static cn.krismile.ai.agent.model.domain.table.SysDictTypeDOTableDef.SYS_DICT_TYPE_DO;

/**
 * 字典类型控制器
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/dict")
public class SysDictTypeController {

    @PostMapping("/init")
    public VO<Boolean> initModels() {
        // 初始化字典类型
        Arrays.stream(DictTypeEnum.values()).forEach(dictType -> SysDictTypeDO.create()
                .where(SYS_DICT_TYPE_DO.TYPE.eq(dictType))
                .remove());
        Arrays.stream(DictTypeEnum.values()).forEach(dictType -> SysDictTypeDO.create()
                .setType(dictType)
                .setName(dictType.name())
                .save());
        // 初始化字典数据
        Arrays.stream(DictTypeEnum.values()).forEach(dictType -> SysDictDO.create()
                .where(SYS_DICT_DO.TYPE.eq(dictType))
                .remove());
        Map<ChatPlatformEnum, List<ChatModelEnum>> platform2ChatModels = Arrays.stream(ChatModelEnum.values())
                .collect(Collectors.groupingBy(ChatModelEnum::platform));
        Arrays.stream(ChatPlatformEnum.values()).forEach(chatPlatform -> {
            SysDictDO dbPlatform = SysDictDO.create()
                    .setType(DictTypeEnum.CHAT_PLATFORM)
                    .setName(chatPlatform.getReasonPhrase())
                    .setValue(chatPlatform.getValue())
                    .setLevel(1)
                    .saveOpt().orElseThrow(RuntimeException::new);
            Optional.ofNullable(platform2ChatModels.get(chatPlatform)).ifPresent(chatModels ->
                    chatModels.forEach(chatModel -> SysDictDO.create()
                            .setType(DictTypeEnum.CHAT_MODEL)
                            .setName(chatModel.getReasonPhrase())
                            .setValue(chatModel.getValue())
                            .setDescription(chatModel.getDescription())
                            .setLevel(2)
                            .setRelParentId(dbPlatform.getId())
                            .saveOpt().orElseThrow(RuntimeException::new)
                    ));
        });
        return R.data(true);
    }

    @PostMapping("/type/add")
    public VO<Boolean> typeAdd(@RequestBody SysDictTypeDO request) {
        return R.data(request.save(true));
    }

    @PostMapping("/add")
    public VO<Boolean> add(@RequestBody SysDictDO request) {
        return R.data(request.save(true));
    }
}
