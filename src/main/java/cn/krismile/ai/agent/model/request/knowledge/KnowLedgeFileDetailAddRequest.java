package cn.krismile.ai.agent.model.request.knowledge;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 新增知识库文件详情请求参数
 *
 * @param content  内容
 * @param metaData 元数据
 * @author JiYinchuan
 * @since 1.0.0
 */
public record KnowLedgeFileDetailAddRequest(
        @Schema(description = "文档 ID，新增详情时为空")
        Long id,
        @Schema(description = "内容")
        @NotBlank(message = "内容不能为空")
        String content,
        @Schema(description = "元数据")
        ObjectNode metaData
) {
}
