package cn.krismile.ai.agent.model.domain;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.ai.document.Document;

/**
 * 用户-知识库文件详情表
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data(staticConstructor = "create")
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("user_knowledge_file_detail")
public class UserKnowledgeFileDetailDO extends BaseAssignDO<UserKnowledgeFileDetailDO> {

    /**
     * 文档 ID
     */
    private String documentId;

    /**
     * 内容
     */
    private String content;

    /**
     * 元数据
     */
    @Column(typeHandler = JacksonTypeHandler.class)
    private ObjectNode metaData;

    /**
     * 关联文件 ID
     */
    private Long relFileId;

    /**
     * 逻辑删除
     */
    private Boolean delFlag;

    /**
     * 转换为 Document
     *
     * @return Document
     */
    public Document toDocument() {
        Document.Builder builder = Document.builder()
                .id(this.documentId)
                .text(this.content);
        if (this.metaData != null) {
            this.metaData.forEachEntry((key, value) -> builder.metadata(key, value.asText()));
        }
        return builder.build();
    }
}