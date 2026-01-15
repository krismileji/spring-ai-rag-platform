package cn.krismile.ai.agent.repository.platform;

import cn.krismile.ai.agent.mapper.AiPlatformMapper;
import cn.krismile.ai.agent.model.domain.AiPlatformDO;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import static cn.krismile.ai.agent.model.domain.table.AiPlatformDOTableDef.AI_PLATFORM_DO;

/**
 * AiPlatformRepositoryImpl
 *
 * @author Auto Generated
 * @since 1.0.0
 */
@Service
public class AiPlatformRepositoryImpl extends ServiceImpl<AiPlatformMapper, AiPlatformDO> implements AiPlatformRepository {

    @Override
    public @NonNull AiPlatformDO getOrInitIfNull(ChatPlatformEnum platform) {
        return AiPlatformDO.create()
                .select(AI_PLATFORM_DO.ID)
                .where(AI_PLATFORM_DO.PLATFORM.eq(platform.getValue()))
                .oneOpt()
                .orElseGet(() -> this.init(platform));
    }

    @Override
    public @NonNull AiPlatformDO init(ChatPlatformEnum platform) {
        return AiPlatformDO.create()
                .where(AI_PLATFORM_DO.PLATFORM.eq(platform.getValue()))
                .oneOpt()
                .orElseGet(AiPlatformDO::create)
                .setPlatform(platform)
                .setEnabled(true)
                .saveOrUpdateOpt()
                .orElseThrow(() -> new ApplicationException(
                        ErrorCodeEnum.DATABASE_SERVICE_ERROR, "Failed to edit platform"));
    }
}