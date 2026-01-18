package cn.krismile.ai.agent.repository.knowledge;

import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDO;
import cn.krismile.ai.agent.model.enumeration.UserKnowledgeFileStatusEnum;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * UserKnowledgeFileRepository
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Repository
public interface UserKnowledgeFileRepository extends R2dbcRepository<UserKnowledgeFileDO, Long> {

    Flux<UserKnowledgeFileDO> findByRelKnowledgeIdAndRelUserIdAndStatus(Long relKnowledgeId, Long relUserId, UserKnowledgeFileStatusEnum status);

    Mono<UserKnowledgeFileDO> findByIdAndRelUserId(Long id, Long relUserId);

}
