package cn.krismile.ai.agent.model.response.chat;

import cn.krismile.ai.agent.model.domain.AiModelDO;
import cn.krismile.ai.agent.model.domain.AiPlatformDO;
import cn.krismile.ai.agent.model.enumeration.chat.ChatModelTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 聊天模型返回值
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@Schema(description = "聊天模型 VO")
public class ChatModelVO implements Serializable {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "平台")
    private String platform;

    @Schema(description = "平台名称")
    private String platformName;

    @Schema(description = "模型类型")
    private ChatModelTypeEnum type;

    @Schema(description = "模型")
    private String model;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "元数据")
    private ChatModelMetaDataVO metaData;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "排序")
    private Integer sort;

    public static ChatModelVO from(AiPlatformDO platform, AiModelDO model) {
        return new ChatModelVO()
                .setId(model.getId())
                .setPlatform(platform.getPlatform().getValue())
                .setPlatformName(platform.getPlatform().getReasonPhrase())
                .setType(model.getType())
                .setModel(model.getCode())
                .setModelName(model.getName())
                .setDescription(model.getDescription())
                .setEnabled(model.getEnabled())
                .setSort(model.getSort());
    }
}
