package cn.krismile.ai.agent.model.domain;

import cn.krismile.ai.agent.model.enumeration.chat.ChatModelTypeEnum;
import cn.krismile.ai.agent.model.request.chat.ChatOptionsRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

/**
 * AI 模型表
 *
 * @author Auto Generated
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("ai_model")
public class AiModelDO extends BaseIdDO {

    /**
     * 模型类型
     */
    private ChatModelTypeEnum type;

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
    private ChatOptionsRequest defaultOptions;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 关联平台 ID
     */
    private Long relPlatformId;

}
