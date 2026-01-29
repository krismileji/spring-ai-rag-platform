package cn.krismile.ai.agent.model.domain;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.request.chat.ChatOptionsRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;
import org.springframework.data.relational.core.mapping.Table;

/**
 * AI平台表
 *
 * @author Auto Generated
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("ai_platform")
public class AiPlatformDO extends BaseIdDO {

    /**
     * 平台
     */
    private ChatPlatformEnum platform;

    /**
     * ApiKey
     */
    private @Nullable String apiKey;

    /**
     * 默认配置
     */
    private ChatOptionsRequest defaultOptions;

    /**
     * 是否启用
     */
    private Boolean enabled;

}
