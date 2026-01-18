package cn.krismile.ai.agent.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础 DO
 *
 * @author JiYinchuan
 * @since 0.1.0
 */
@Data
@Accessors(chain = true)
public abstract class BaseDO implements Serializable {

    /**
     * 创建时间
     */
    @CreatedDate
    protected LocalDateTime createTime;

    /**
     * 创建人
     */
    @CreatedBy
    protected Long createUser;

    /**
     * 最后修改时间
     */
    @LastModifiedDate
    protected LocalDateTime updateTime;

    /**
     * 最后修改人
     */
    @LastModifiedBy
    protected Long updateUser;

}
