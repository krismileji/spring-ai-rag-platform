package cn.krismile.ai.agent.repository.system;

import cn.krismile.ai.agent.mapper.SysDictMapper;
import cn.krismile.ai.agent.model.domain.SysDictDO;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * SysDictRepositoryImpl
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class SysDictRepositoryImpl extends ServiceImpl<SysDictMapper, SysDictDO> implements SysDictRepository {
}