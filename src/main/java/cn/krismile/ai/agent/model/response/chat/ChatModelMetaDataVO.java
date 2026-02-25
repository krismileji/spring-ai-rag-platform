package cn.krismile.ai.agent.model.response.chat;

import cn.krismile.ai.agent.model.enumeration.chat.ChatModelModalityEnum;
import cn.krismile.ai.agent.model.interfaces.JsonType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 聊天模型元数据 VO
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@Schema(description = "聊天模型元数据 VO")
public class ChatModelMetaDataVO implements JsonType {

    @Schema(description = "请求模态")
    private List<ChatModelModalityEnum> requestModalities;

}
