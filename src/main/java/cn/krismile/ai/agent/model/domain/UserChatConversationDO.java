package cn.krismile.ai.agent.model.domain;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 用户聊天会话表
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data(staticConstructor = "create")
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("user_chat_conversation")
public class UserChatConversationDO extends BaseDO<String, UserChatConversationDO> {

    /**
     * 唯一 ID
     */
    private String id;

    /**
     * 会话内容
     */
    private String content;

    /**
     * 关联用户 ID
     */
    private Long relUserId;

    /**
     * 逻辑删除
     */
    private Boolean delFlag;

}
