package cn.krismile.ai.agent.model.domain;

import cn.krismile.ai.agent.model.request.chat.ChatOptionsRequest;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * AI 模型表
 *
 * @author Auto Generated
 * @since 1.0.0
 */
@Data(staticConstructor = "create")
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("ai_model")
public class AiModelDO extends BaseAssignDO<AiModelDO> {

    /**
     * 模型编码
     */
    private String code;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 默认配置
     */
    @Column(typeHandler = JacksonTypeHandler.class)
    private ChatOptionsRequest defaultOptions;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 关联平台 ID
     */
    private Long relPlatformId;

}