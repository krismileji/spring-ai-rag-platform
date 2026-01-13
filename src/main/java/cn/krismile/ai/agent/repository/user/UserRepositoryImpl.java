package cn.krismile.ai.agent.repository.user;

import cn.krismile.ai.agent.mapper.UserMapper;
import cn.krismile.ai.agent.model.domain.UserDO;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * UserRepositoryImpl
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class UserRepositoryImpl extends ServiceImpl<UserMapper, UserDO> implements UserRepository {
}