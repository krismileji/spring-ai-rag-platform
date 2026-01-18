package cn.krismile.ai.agent.repository.system;

import cn.krismile.ai.agent.model.domain.SysDictTypeDO;
import cn.krismile.ai.agent.model.enumeration.DictTypeEnum;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * SysDictTypeRepository
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Repository
public interface SysDictTypeRepository extends R2dbcRepository<SysDictTypeDO, Long> {

    Mono<SysDictTypeDO> findByType(DictTypeEnum type);

    Mono<Void> deleteByType(DictTypeEnum type);

}
