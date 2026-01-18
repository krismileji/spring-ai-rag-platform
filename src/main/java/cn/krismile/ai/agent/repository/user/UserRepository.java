package cn.krismile.ai.agent.repository.user;

import cn.krismile.ai.agent.model.domain.UserDO;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * UserRepository
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends R2dbcRepository<UserDO, Long> {

    Mono<UserDO> findByUsername(String username);

}
