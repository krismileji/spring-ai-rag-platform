package cn.krismile.ai.agent.structure.rag.file.service;

import cn.krismile.ai.agent.model.request.knowledge.KnowledgeFileAddRequest;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileDetailVO;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileUploadVO;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileVO;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * RAG 文件服务
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface FileService {

    /**
     * 获取文件列表
     *
     * @param knowledgeId 知识库 ID
     * @return 文件列表
     * @since 1.0.0
     */
    List<KnowledgeFileVO> list(Long knowledgeId);

    /**
     * 获取文件详情
     *
     * @param fileId 文件 ID
     * @return 文件详情
     * @since 1.0.0
     */
    List<KnowledgeFileDetailVO> listFileDetails(Long fileId);

    /**
     * 上传文件
     *
     * @param knowledgeId 知识库 ID
     * @param files       文件
     * @return 是否成功
     * @since 1.0.0
     */
    Flux<KnowledgeFileUploadVO> uploads(Long knowledgeId, Flux<FilePart> files);

    /**
     * 新增文件内容
     *
     * @param knowledgeId 知识库 ID
     * @param requests    请求
     * @return 失败的文件 ID 列表
     * @since 1.0.0
     */
    Flux<Long> add(Long knowledgeId, Flux<KnowledgeFileAddRequest> requests);

    /**
     * 删除文件
     *
     * @param fileId 文件 ID
     * @return 是否成功
     * @since 1.0.0
     */
    Boolean del(Long fileId);

}