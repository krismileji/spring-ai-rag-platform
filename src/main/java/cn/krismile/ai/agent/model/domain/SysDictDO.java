package cn.krismile.ai.agent.model.domain;

import cn.krismile.ai.agent.model.enumeration.DictTypeEnum;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.RelationOneToOne;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 系统字典表
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data(staticConstructor = "create")
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("sys_dict")
public class SysDictDO extends BaseAssignDO<SysDictDO> {

    /**
     * 字典类型
     */
    private DictTypeEnum type;

    /**
     * 字典名称
     */
    private String name;

    /**
     * 字典值
     */
    private String value;

    /**
     * 字典级别
     */
    private Integer level;

    /**
     * 字典描述
     */
    private String description;

    /**
     * 上级ID
     */
    private Long relParentId;

    /**
     * 上级字典
     */
    @Column(ignore = true)
    @RelationOneToOne(selfField = "relParentId", targetField = "id")
    private SysDictDO parent;

}
