package cn.krismile.ai.agent.model.response.knowledge;

import cn.krismile.ai.agent.model.domain.UserKnowledgeDO;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 知识库 VO
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Schema(description = "知识库 VO")
public record KnowledgeVO(
        @Schema(description = "ID", type = "string")
        Long id,
        @Schema(description = "名称")
        String name,
        @Schema(description = "描述")
        String description
) {

    /**
     * 转换
     *
     * @param userKnowledgeDO 用户知识库
     * @return 知识库 VO
     * @since 1.0.0
     */
    public static KnowledgeVO from(UserKnowledgeDO userKnowledgeDO) {
        return new KnowledgeVO(
                userKnowledgeDO.getId(),
                userKnowledgeDO.getName(),
                userKnowledgeDO.getDescription()
        );
    }
}
