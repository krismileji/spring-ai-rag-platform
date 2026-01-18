package cn.krismile.ai.agent.repository.chat;

import cn.krismile.ai.agent.model.domain.UserChatMemoryDO;
import org.jspecify.annotations.NonNull;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * UserChatMemoryRepository
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Repository
public interface UserChatMemoryRepository extends R2dbcRepository<UserChatMemoryDO, Long> {

    @Query("SELECT DISTINCT rel_conversation_id FROM user_chat_memory WHERE rel_user_id = :relUserId")
    Flux<String> findDistinctRelConversationIdByRelUserId(@NonNull Long relUserId);

    Flux<UserChatMemoryDO> findByRelConversationIdAndRelUserId(String relConversationId, Long relUserId);

    Mono<Void> deleteByRelConversationId(String relConversationId);

}
