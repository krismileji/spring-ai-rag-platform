package cn.krismile.ai.agent.model.response.knowledge;

import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDO;
import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDetailDO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 知识库文件上传 VO
 *
 * @param id          ID
 * @param fileName    文件名
 * @param description 描述
 * @author JiYinchuan
 * @since 1.0.0
 */
@Schema(description = "知识库 VO")
public record KnowledgeFileUploadVO(
        @Schema(description = "ID", type = "string")
        Long id,
        @Schema(description = "文件名")
        String fileName,
        @Schema(description = "描述")
        String description,
        @Schema(description = "详情")
        List<KnowledgeFileDetailVO> details
) {

    /**
     * 转换
     *
     * @param file    文件
     * @param details 详情
     * @return 知识库 VO
     * @since 1.0.0
     */
    public static KnowledgeFileUploadVO from(UserKnowledgeFileDO file, List<UserKnowledgeFileDetailDO> details) {
        return new KnowledgeFileUploadVO(
                file.getId(),
                file.getFileName(),
                file.getDescription(),
                details.stream().map(KnowledgeFileDetailVO::from).toList()
        );
    }
}
