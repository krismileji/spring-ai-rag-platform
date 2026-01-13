package cn.krismile.ai.agent.model.response.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * 聊天响应DTO
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@FieldNameConstants
@Accessors(chain = true)
public class ChatResponse {

    /**
     * AI回复内容
     */
    private String content;

    /**
     * AI思考过程（仅在启用thinking模式时有值）
     */
    private String reasoningContent;

    @JsonIgnore
    public boolean valid() {
        return !StringUtils.isAllBlank(content, reasoningContent);
    }
}
