package cn.krismile.ai.agent.repository.knowledge;

import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDetailDO;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * UserKnowledgeFileDetailRepository
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Repository
public interface UserKnowledgeFileDetailRepository extends R2dbcRepository<UserKnowledgeFileDetailDO, Long> {

    Flux<UserKnowledgeFileDetailDO> findByRelFileId(Long relFileId);

    Mono<Void> deleteByRelFileId(Long relFileId);

}
