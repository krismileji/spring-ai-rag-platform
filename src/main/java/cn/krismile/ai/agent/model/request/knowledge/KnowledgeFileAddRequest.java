package cn.krismile.ai.agent.model.request.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 新增知识库文件请求参数
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Schema(description = "新增知识库文件请求参数")
public record KnowledgeFileAddRequest(
        @Schema(description = "ID")
        @NotNull(message = "ID 不能为空")
        Long id,
        @Schema(description = "描述")
        String description,
        @Schema(description = "详情")
        @NotEmpty(message = "详情不能为空")
        @Valid
        List<KnowLedgeFileDetailAddRequest> details,
        @Schema(description = "嵌入模型")
        @NotNull(message = "嵌入模型不能为空")
        Long embeddingModelId
) {
}
