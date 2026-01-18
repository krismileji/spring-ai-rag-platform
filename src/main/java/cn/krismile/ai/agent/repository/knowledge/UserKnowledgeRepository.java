package cn.krismile.ai.agent.repository.knowledge;

import cn.krismile.ai.agent.model.domain.UserKnowledgeDO;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * UserKnowledgeRepository
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Repository
public interface UserKnowledgeRepository extends R2dbcRepository<UserKnowledgeDO, Long> {

    Flux<UserKnowledgeDO> findByRelUserId(Long relUserId);

    Mono<UserKnowledgeDO> findByIdAndRelUserId(Long id, Long relUserId);

    Mono<UserKnowledgeDO> findByNameAndRelUserId(String name, Long relUserId);

    Mono<Boolean> existsByIdAndRelUserId(Long id, Long relUserId);

}
