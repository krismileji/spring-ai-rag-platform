package cn.krismile.ai.agent.model.domain;

import com.mybatisflex.core.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础DO
 *
 * @param <ID> ID类型
 * @author JiYinchuan
 * @since 0.1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BaseDO<ID, D extends Model<D>> extends Model<D> implements Serializable {

    /**
     * ID字段名称
     */
    public static final String FIELD_ID = "id";

    /**
     * 创建时间字段名称
     */
    public static final String FIELD_CREATE_TIME = "createTime";

    /**
     * 创建人字段名称
     */
    public static final String FIELD_CREATE_USER = "createUser";

    /**
     * 最后修改时间字段名称
     */
    public static final String FIELD_UPDATE_TIME = "updateTime";

    /**
     * 最后修改人字段名称
     */
    public static final String FIELD_UPDATE_USER = "updateUser";

    /**
     * 逻辑删除字段名称
     */
    public static final String FIELD_LOGIC_DELETE = "del_flag";

    /**
     * 版本字段名称
     */
    public static final String FIELD_VERSION = "version";

    /**
     * 设置 ID
     *
     * @param id ID
     * @return this
     * @since 0.1.0
     */
    public abstract D setId(ID id);

    /**
     * 获取 ID
     *
     * @return ID
     * @since 0.1.0
     */
    public abstract ID getId();

    /**
     * 创建时间
     *
     * <p>新增时将自动填充当前时间
     */
    protected LocalDateTime createTime;

    /**
     * 创建人
     */
    protected Long createUser;

    /**
     * 最后修改时间
     *
     * <p>新增与修改时自动填充当前时间
     */
    protected LocalDateTime updateTime;

    /**
     * 最后修改时间
     */
    protected Long updateUser;

    /**
     * 构造器
     *
     * @since 0.1.0
     */
    public BaseDO() {
    }
}
