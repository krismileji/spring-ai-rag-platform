package cn.krismile.ai.agent.repository.platform;

import cn.krismile.ai.agent.model.domain.AiModelDO;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Collection;

/**
 * AiModelRepository
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Repository
public interface AiModelRepository extends R2dbcRepository<AiModelDO, Long> {

    Flux<AiModelDO> findByRelPlatformIdAndCodeIn(Long relPlatformId, Collection<String> codes);

}
