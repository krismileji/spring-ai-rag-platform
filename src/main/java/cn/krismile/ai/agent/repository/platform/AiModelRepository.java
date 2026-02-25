package cn.krismile.ai.agent.repository.platform;

import cn.krismile.ai.agent.model.domain.AiModelDO;
import cn.krismile.ai.agent.model.enumeration.chat.ChatModelTypeEnum;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * AiModelRepository
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Repository
public interface AiModelRepository extends R2dbcRepository<AiModelDO, Long> {

    Mono<AiModelDO> findByRelPlatformIdAndTypeAndCode(
            Long relPlatformId,
            ChatModelTypeEnum type,
            String code);

    Flux<AiModelDO> findByRelPlatformIdAndType(
            Long relPlatformId,
            ChatModelTypeEnum type);

    Mono<Boolean> removeByIdIn(Collection<Long> ids);

}
