package cn.krismile.ai.agent.structure.chat.platoform;

import cn.krismile.ai.agent.model.domain.AiPlatformDO;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.request.chat.ChatOptionsRequest;
import cn.krismile.ai.agent.model.request.chat.ChatPlatformEditRequest;
import cn.krismile.ai.agent.model.response.chat.ChatPlatformVO;
import cn.krismile.ai.agent.util.AESEncryptionUtil;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.krismile.ai.agent.model.domain.table.AiPlatformDOTableDef.AI_PLATFORM_DO;

/**
 * 平台服务实现
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class PlatformServiceImpl implements PlatformService {

    @Override
    public List<ChatPlatformVO> listPlatforms() {
        Map<ChatPlatformEnum, AiPlatformDO> platform2Data = AiPlatformDO.create()
                .where(AI_PLATFORM_DO.PLATFORM.in(Arrays.stream(ChatPlatformEnum.values()).map(ChatPlatformEnum::getValue).toList()))
                .list().stream()
                .collect(Collectors.toMap(AiPlatformDO::getPlatform, Function.identity()));
        return Arrays.stream(ChatPlatformEnum.values())
                .map(platform -> ChatPlatformVO.of(Optional.ofNullable(platform2Data.get(platform))
                        .orElseGet(() -> AiPlatformDO.create().setPlatform(platform))))
                .toList();
    }

    @Override
    public Boolean editPlatform(ChatPlatformEditRequest request) {
        ChatPlatformEnum platform = request.getPlatform();
        String apiKey = request.getApiKey();
        ChatOptionsRequest defaultOptions = request.getDefaultOptions();
        Boolean enabled = request.getEnabled();

        String encodedApiKey = StringUtils.isNotBlank(apiKey) ? AESEncryptionUtil.encrypt(apiKey, SECRET_KEY) : null;

        AiPlatformDO.create()
                .where(AI_PLATFORM_DO.PLATFORM.eq(platform.getValue()))
                .oneOpt()
                .orElseGet(AiPlatformDO::create)
                .setPlatform(platform)
                .setApiKey(encodedApiKey)
                .setDefaultOptions(defaultOptions)
                .setEnabled(enabled)
                .saveOrUpdateOpt()
                .orElseThrow(() -> new ApplicationException(
                        ErrorCodeEnum.DATABASE_SERVICE_ERROR, "Failed to edit platform"));
        return true;
    }
}