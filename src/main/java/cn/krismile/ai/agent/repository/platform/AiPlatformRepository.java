package cn.krismile.ai.agent.repository.platform;

import cn.krismile.ai.agent.model.domain.AiPlatformDO;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import com.mybatisflex.core.service.IService;
import org.jspecify.annotations.NonNull;

/**
 * AI 平台服务
 *
 * @author Auto Generated
 * @since 1.0.0
 */
public interface AiPlatformRepository extends IService<AiPlatformDO> {

    /**
     * 根据平台获取平台 ID
     *
     * @param platform 平台
     * @return 平台 ID
     * @since 1.0.0
     */
    @NonNull AiPlatformDO getOrInitIfNull(ChatPlatformEnum platform);

    /**
     * 初始化平台
     *
     * @param platform 平台
     * @return 平台
     * @since 1.0.0
     */
    @NonNull AiPlatformDO init(ChatPlatformEnum platform);

}