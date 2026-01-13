package cn.krismile.ai.agent.model.domain;

import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import cn.krismile.ai.agent.model.request.chat.ChatOptionsRequest;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * AI平台表
 *
 * @author Auto Generated
 * @since 1.0.0
 */
@Data(staticConstructor = "create")
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("ai_platform")
public class AiPlatformDO extends BaseAssignDO<AiPlatformDO> {

    /**
     * 平台
     */
    private ChatPlatformEnum platform;

    /**
     * ApiKey
     */
    private String apiKey;

    /**
     * 默认配置
     */
    @Column(typeHandler = JacksonTypeHandler.class)
    private ChatOptionsRequest defaultOptions;

    /**
     * 是否启用
     */
    private Boolean enabled;

}