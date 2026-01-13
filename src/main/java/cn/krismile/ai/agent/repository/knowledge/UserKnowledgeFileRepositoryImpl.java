package cn.krismile.ai.agent.repository.knowledge;

import cn.krismile.ai.agent.mapper.UserKnowledgeFileMapper;
import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDO;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * UserKnowledgeFileRepositoryImpl
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class UserKnowledgeFileRepositoryImpl extends ServiceImpl<UserKnowledgeFileMapper, UserKnowledgeFileDO> implements UserKnowledgeFileRepository {
}