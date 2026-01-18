package cn.krismile.ai.agent.model.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 用户聊天会话表
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table("user_chat_conversation")
public class UserChatConversationDO extends BaseDO implements Persistable<String> {

    /**
     * 唯一 ID
     */
    @Id
    private String id;

    /**
     * 会话内容
     */
    private String content;

    /**
     * 关联用户 ID
     */
    private Long relUserId;

    @Transient
    private boolean isNew;

    @Override
    public boolean isNew() {
        return this.isNew || this.id == null;
    }
}
