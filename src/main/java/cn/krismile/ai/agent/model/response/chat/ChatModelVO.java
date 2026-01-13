package cn.krismile.ai.agent.model.response.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 聊天模型返回值
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@Schema(description = "聊天模型VO")
public class ChatModelVO {

    @Schema(description = "平台")
    private String platform;

    @Schema(description = "平台名称")
    private String platformName;

    @Schema(description = "模型")
    private String model;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "描述")
    private String description;

}
