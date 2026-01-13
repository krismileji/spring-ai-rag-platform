package cn.krismile.ai.agent.model.request.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 新增/编辑知识库请求参数
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Schema(description = "新增/编辑知识库请求参数")
public record KnowledgeAddEditRequest(
        @Schema(description = "ID")
        Long id,
        @Schema(description = "名称")
        @NotBlank(message = "名称不能为空")
        String name,
        @Schema(description = "描述")
        String description
) {
}
