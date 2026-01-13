package cn.krismile.ai.agent.model.domain;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 用户知识库表
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data(staticConstructor = "create")
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("user_knowledge")
public class UserKnowledgeDO extends BaseAssignDO<UserKnowledgeDO> {

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 关联用户 ID
     */
    private Long relUserId;

    /**
     * 逻辑删除
     */
    private Boolean delFlag;

}
