package cn.krismile.ai.agent.structure.chat.knowledge.service;

import cn.krismile.ai.agent.model.request.knowledge.KnowledgeAddEditRequest;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeVO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 知识库服务
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface KnowledgeService {

    /**
     * 查询知识库列表
     *
     * @return 列表
     * @since 1.0.0
     */
    Flux<KnowledgeVO> list();

    /**
     * 校验名称
     *
     * @param name 名称
     * @return 错误信息
     * @since 1.0.0
     */
    Mono<String> checkName(String name);

    /**
     * 创建/编辑知识库
     *
     * @param request 参数
     * @return token
     * @since 1.0.0
     */
    Mono<Boolean> addEdit(KnowledgeAddEditRequest request);

    /**
     * 删除知识库
     *
     * @param id 知识库ID
     * @return 是否删除成功
     * @since 1.0.0
     */
    Mono<Boolean> delete(Long id);

}
