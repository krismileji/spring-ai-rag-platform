package cn.krismile.ai.agent.repository.system;

import cn.krismile.ai.agent.mapper.SysDictTypeMapper;
import cn.krismile.ai.agent.model.domain.SysDictTypeDO;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * SysDictTypeRepositoryImpl
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class SysDictTypeRepositoryImpl extends ServiceImpl<SysDictTypeMapper, SysDictTypeDO> implements SysDictTypeRepository {
}