package cn.krismile.ai.agent.model.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 用户聊天记录表
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("user_chat_memory")
public class UserChatMemoryDO extends BaseIdDO {

    /**
     * 模型
     */
    private String model;

    /**
     * 会话内容
     */
    private String content;

    /**
     * 深度思考内容
     */
    private String reasoningContent;

    /**
     * 会话类型
     */
    private MessageType type;

    /**
     * 关联会话 ID
     */
    private String relConversationId;

    /**
     * 关联用户 ID
     */
    private Long relUserId;

}
