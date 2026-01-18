package cn.krismile.ai.agent.model.domain;

import cn.krismile.ai.agent.model.enumeration.DictTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 系统字典类型表
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("sys_dict_type")
public class SysDictTypeDO extends BaseIdDO {

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
     * 字典描述
     */
    private String description;

}
