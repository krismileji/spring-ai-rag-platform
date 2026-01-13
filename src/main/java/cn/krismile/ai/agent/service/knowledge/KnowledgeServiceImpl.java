package cn.krismile.ai.agent.service.knowledge;

import cn.dev33.satoken.stp.StpUtil;
import cn.krismile.ai.agent.context.RequestContext;
import cn.krismile.ai.agent.model.domain.UserKnowledgeDO;
import cn.krismile.ai.agent.model.request.knowledge.KnowledgeAddEditRequest;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeVO;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static cn.krismile.ai.agent.model.domain.table.UserKnowledgeDOTableDef.USER_KNOWLEDGE_DO;

/**
 * 创建知识库
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    @Override
    public List<KnowledgeVO> list() {
        Long loginId = RequestContext.syncApply(StpUtil::getLoginIdAsLong);
        return UserKnowledgeDO.create()
                .where(USER_KNOWLEDGE_DO.REL_USER_ID.eq(loginId))
                .list().stream()
                .map(KnowledgeVO::from)
                .toList();
    }

    @Override
    public String checkName(String name) {
        Long loginId = RequestContext.syncApply(StpUtil::getLoginIdAsLong);
        return UserKnowledgeDO.create()
                .where(USER_KNOWLEDGE_DO.NAME.eq(name))
                .and(USER_KNOWLEDGE_DO.REL_USER_ID.eq(loginId))
                .oneOpt().isPresent() ? "知识库名称已存在" : null;
    }

    @Override
    public boolean addEdit(KnowledgeAddEditRequest request) {
        Long loginId = RequestContext.syncApply(StpUtil::getLoginIdAsLong);
        Long id = request.id();
        String name = request.name();
        String description = request.description();
        if (id == null) {
            Optional.ofNullable(this.checkName(name)).ifPresent(errorMessage -> {
                throw new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, errorMessage);
            });
        }
        return UserKnowledgeDO.create()
                .setName(name)
                .setDescription(description)
                .setRelUserId(loginId)
                .setId(id)
                .saveOrUpdate();
    }
}
