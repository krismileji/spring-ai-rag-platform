package cn.krismile.ai.agent.model.response.knowledge;

import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDO;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import host.springboot.framework3.core.constant.PatternConstant;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 知识库文件 VO
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Schema(description = "知识库文件 VO")
public record KnowledgeFileVO(
        @Schema(description = "ID", type = "string")
        Long id,
        @Schema(description = "文件名")
        String fileName,
        @Schema(description = "文件描述")
        String description,
        @Schema(description = "关联的嵌入模型")
        ChatModelVO relEmbeddingModel,
        @Schema(description = "创建时间", format = PatternConstant.Date.NORM_DATETIME_PATTERN)
        LocalDateTime createTime
) {

    /**
     * 转换
     *
     * @param file              用户知识库文件
     * @param relEmbeddingModel 关联的嵌入模型
     * @return 知识库文件 VO
     * @since 1.0.0
     */
    public static KnowledgeFileVO from(UserKnowledgeFileDO file, ChatModelVO relEmbeddingModel) {
        return new KnowledgeFileVO(
                file.getId(),
                file.getFileName(),
                file.getDescription(),
                relEmbeddingModel,
                file.getCreateTime()
        );
    }
}