package cn.krismile.ai.agent.model.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.activerecord.Model;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * 基础Long类型雪花ID DO
 *
 * @author JiYinchuan
 * @since 0.1.0
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BaseAssignDO<D extends Model<D>> extends BaseDO<Long, D> implements Serializable {

    /**
     * 唯一 ID
     *
     * <p>默认使用雪花 ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    @JsonSerialize(using = ToStringSerializer.class)
    protected Long id;

    @Override
    @SuppressWarnings("unchecked")
    public D setId(Long id) {
        this.id = id;
        return (D) this;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * 构造器
     *
     * @since 0.1.0
     */
    public BaseAssignDO() {
    }
}
