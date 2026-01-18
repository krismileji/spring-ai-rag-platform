package cn.krismile.ai.agent.repository.system;

import cn.krismile.ai.agent.model.domain.SysDictDO;
import cn.krismile.ai.agent.model.enumeration.DictTypeEnum;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * SysDictRepository
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Repository
public interface SysDictRepository extends R2dbcRepository<SysDictDO, Long> {

    Flux<SysDictDO> findByType(DictTypeEnum type);

    Flux<SysDictDO> findByTypeIn(Collection<DictTypeEnum> types);

    Mono<Void> deleteByType(DictTypeEnum type);

}
