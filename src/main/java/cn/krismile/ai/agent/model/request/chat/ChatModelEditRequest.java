package cn.krismile.ai.agent.model.request.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 聊天平台模型编辑请求
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Schema(description = "聊天平台模型编辑请求")
public class ChatModelEditRequest {

    @Schema(description = "模型 ID")
    private Long id;

    @Schema(description = "模型")
    @NotNull(message = "模型不能为空")
    private String model;

    @Schema(description = "是否启用")
    @NotNull(message = "是否启用不能为空")
    private Boolean enabled;

}
