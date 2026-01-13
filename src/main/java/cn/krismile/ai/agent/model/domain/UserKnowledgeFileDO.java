package cn.krismile.ai.agent.model.domain;

import cn.krismile.ai.agent.model.enumeration.UserKnowledgeFileStatusEnum;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 用户-知识库文件表
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data(staticConstructor = "create")
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("user_knowledge_file")
public class UserKnowledgeFileDO extends BaseAssignDO<UserKnowledgeFileDO> {

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
     * 逻辑删除
     */
    private Boolean delFlag;

    // public static List<UserKnowledgeFileDO> toAdd(List<Document> documents) {
    //     return documents.stream()
    //             .map(document -> UserKnowledgeFileDO.create()
    //                     .setFileName(document.getMetadata().get("fileName"))
    //                     .setPath(document.getMetadata().get("path"))
    //                     .setRelUserId(document.getMetadata().get("relUserId"))
    //                     .setRelKnowledgeId(document.getMetadata().get("relKnowledgeId"))
    //                     .setDelFlag(false))
    //             .toList();
    // }
}