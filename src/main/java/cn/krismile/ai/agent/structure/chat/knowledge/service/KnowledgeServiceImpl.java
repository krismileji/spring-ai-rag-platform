package cn.krismile.ai.agent.structure.chat.knowledge.service;

import cn.krismile.ai.agent.configuration.security.context.SecurityUtils;
import cn.krismile.ai.agent.model.domain.UserKnowledgeDO;
import cn.krismile.ai.agent.model.request.knowledge.KnowledgeAddEditRequest;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeVO;
import cn.krismile.ai.agent.repository.knowledge.UserKnowledgeRepository;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 创建知识库
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final UserKnowledgeRepository userKnowledgeRepository;

    @Override
    public Flux<KnowledgeVO> list() {
        return SecurityUtils.getUserId()
                .flatMapMany(userKnowledgeRepository::findByRelUserId)
                .map(KnowledgeVO::from);
    }

    @Override
    public Mono<String> checkName(String name) {
        return SecurityUtils.getUserId()
                .flatMap(userId -> userKnowledgeRepository.findByNameAndRelUserId(name, userId))
                .map(k -> "知识库名称已存在");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Mono<Boolean> addEdit(KnowledgeAddEditRequest request) {
        return SecurityUtils.getUserId()
                .flatMap(userId -> {
                    Long id = request.id();
                    String name = request.name();
                    String description = request.description();
                    
                    if (id == null) {
                        return this.checkName(name)
                                .flatMap(errorMessage -> Mono.error(new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, errorMessage)))
                                .switchIfEmpty(Mono.defer(() -> {
                                    UserKnowledgeDO entity = new UserKnowledgeDO();
                                    entity.setName(name)
                                          .setDescription(description)
                                          .setRelUserId(userId);
                                    return userKnowledgeRepository.save(entity);
                                }));
                    } else {
                        return userKnowledgeRepository.findByIdAndRelUserId(id, userId)
                                .switchIfEmpty(Mono.error(new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, "知识库不存在")))
                                .flatMap(existing -> {
                                    existing.setName(name).setDescription(description);
                                    return userKnowledgeRepository.save(existing);
                                });
                    }
                })
                .thenReturn(true);
    }
}
