package cn.krismile.ai.agent.mapper;

import cn.krismile.ai.agent.model.domain.UserChatConversationDO;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserChatConversationMapper
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Mapper
public interface UserChatConversationMapper extends BaseMapper<UserChatConversationDO> {
}