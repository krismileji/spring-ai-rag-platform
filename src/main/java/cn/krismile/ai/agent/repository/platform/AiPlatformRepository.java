package cn.krismile.ai.agent.repository.platform;

import cn.krismile.ai.agent.model.domain.AiPlatformDO;
import cn.krismile.ai.agent.model.enumeration.chat.ChatPlatformEnum;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * AiPlatformRepository
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Repository
public interface AiPlatformRepository extends R2dbcRepository<AiPlatformDO, Long> {

    Mono<AiPlatformDO> findByPlatform(ChatPlatformEnum platform);

    Flux<AiPlatformDO> findByEnabledIsTrue();

    Mono<AiPlatformDO> findByPlatformAndEnabledIsTrue(ChatPlatformEnum platform);

}
