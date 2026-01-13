package cn.krismile.ai.agent.repository.knowledge;

import cn.krismile.ai.agent.mapper.UserKnowledgeMapper;
import cn.krismile.ai.agent.model.domain.UserKnowledgeDO;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * UserKnowledgeRepositoryImpl
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class UserKnowledgeRepositoryImpl extends ServiceImpl<UserKnowledgeMapper, UserKnowledgeDO> implements UserKnowledgeRepository {
}