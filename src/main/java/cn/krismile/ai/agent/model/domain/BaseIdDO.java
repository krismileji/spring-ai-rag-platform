package cn.krismile.ai.agent.model.domain;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

/**
 * 带ID 的基础DO
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BaseIdDO extends BaseDO {

    /**
     * ID
     */
    @Id
    protected Long id;

}
