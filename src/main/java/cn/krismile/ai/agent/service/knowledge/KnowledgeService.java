package cn.krismile.ai.agent.service.knowledge;

import cn.krismile.ai.agent.model.request.knowledge.KnowledgeAddEditRequest;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeVO;

import java.util.List;

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
    List<KnowledgeVO> list();

    /**
     * 校验名称
     *
     * @param name 名称
     * @return 错误信息
     * @since 1.0.0
     */
    String checkName(String name);

    /**
     * 创建/编辑知识库
     *
     * @param request 参数
     * @return token
     * @since 1.0.0
     */
    boolean addEdit(KnowledgeAddEditRequest request);;

}