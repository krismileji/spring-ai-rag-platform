package cn.krismile.ai.agent.repository.chat;

import cn.krismile.ai.agent.model.domain.UserChatConversationDO;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * UserChatConversationRepository
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Repository
public interface UserChatConversationRepository extends R2dbcRepository<UserChatConversationDO, String> {

    Flux<UserChatConversationDO> findByRelUserId(Long relUserId);

}
