package cn.krismile.ai.agent.model.domain;

import cn.krismile.ai.agent.model.enumeration.UserKnowledgeFileStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 用户-知识库文件表
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("user_knowledge_file")
public class UserKnowledgeFileDO extends BaseIdDO {

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 文件状态
     */
    private UserKnowledgeFileStatusEnum status;

    /**
     * 关联用户 ID
     */
    private Long relUserId;

    /**
     * 关联知识库 ID
     */
    private Long relKnowledgeId;

    /**
     * 嵌入模型
     */
    private Long relEmbeddingModelId;

}
