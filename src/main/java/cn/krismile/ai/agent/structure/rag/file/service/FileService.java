package cn.krismile.ai.agent.structure.rag.file.service;

import cn.krismile.ai.agent.model.request.knowledge.KnowledgeFileAddRequest;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileDetailVO;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileUploadVO;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileVO;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * FileService
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
public interface FileService {

    /**
     * 查询文件列表
     *
     * @param knowledgeId 知识库 ID
     * @return 列表
     * @since 1.0.0
     */
    Flux<KnowledgeFileVO> list(Long knowledgeId);

    /**
     * 查询文件详情
     *
     * @param fileId 文件 ID
     * @return 详情
     * @since 1.0.0
     */
    Flux<KnowledgeFileDetailVO> listFileDetails(Long fileId);

    /**
     * 上传文件
     *
     * @param knowledgeId 知识库 ID
     * @param files       文件
     * @return 上传结果
     * @since 1.0.0
     */
    Flux<KnowledgeFileUploadVO> uploads(Long knowledgeId, Flux<FilePart> files);

    /**
     * 保存文件
     *
     * @param knowledgeId 知识库 ID
     * @param requests    请求
     * @return 文件 ID 列表
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
    Mono<Boolean> del(Long fileId);
}
