package cn.krismile.ai.agent.model.domain;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.ai.chat.messages.MessageType;

/**
 * 用户聊天记录表
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data(staticConstructor = "create")
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("user_chat_memory")
public class UserChatMemoryDO extends BaseAssignDO<UserChatMemoryDO> {

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

    /**
     * 逻辑删除
     */
    private Boolean delFlag;

}
