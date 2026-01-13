package cn.krismile.ai.agent.model.response.knowledge;

import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDetailDO;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 知识库文件详情 VO
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Schema(description = "知识库文件详情 VO")
public record KnowledgeFileDetailVO(
        @Schema(description = "ID", type = "string")
        Long id,
        @Schema(description = "内容")
        String content,
        @Schema(description = "元数据")
        ObjectNode metaData
) {

    /**
     * 转换
     *
     * @param userKnowledgeFileDetailDO 用户知识库文件详情
     * @return 知识库文件详情 VO
     * @since 1.0.0
     */
    public static KnowledgeFileDetailVO from(UserKnowledgeFileDetailDO userKnowledgeFileDetailDO) {
        return new KnowledgeFileDetailVO(
                userKnowledgeFileDetailDO.getId(),
                userKnowledgeFileDetailDO.getContent(),
                userKnowledgeFileDetailDO.getMetaData()
        );
    }
}