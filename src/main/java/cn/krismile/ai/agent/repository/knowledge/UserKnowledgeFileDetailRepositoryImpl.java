package cn.krismile.ai.agent.repository.knowledge;

import cn.krismile.ai.agent.mapper.UserKnowledgeFileDetailMapper;
import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDetailDO;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * UserKnowledgeFileDetailRepositoryImpl
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class UserKnowledgeFileDetailRepositoryImpl
        extends ServiceImpl<UserKnowledgeFileDetailMapper, UserKnowledgeFileDetailDO>
        implements UserKnowledgeFileDetailRepository {
}