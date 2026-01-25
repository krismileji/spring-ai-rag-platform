package cn.krismile.ai.agent.structure.rag.file.service;

import cn.krismile.ai.agent.configuration.security.context.SecurityUtils;
import cn.krismile.ai.agent.constant.Knowledge;
import cn.krismile.ai.agent.model.domain.AiModelDO;
import cn.krismile.ai.agent.model.domain.AiPlatformDO;
import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDO;
import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDetailDO;
import cn.krismile.ai.agent.model.enumeration.UserKnowledgeFileStatusEnum;
import cn.krismile.ai.agent.model.request.knowledge.KnowledgeFileAddRequest;
import cn.krismile.ai.agent.model.response.chat.ChatModelVO;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileDetailVO;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileUploadVO;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileVO;
import cn.krismile.ai.agent.repository.knowledge.UserKnowledgeFileDetailRepository;
import cn.krismile.ai.agent.repository.knowledge.UserKnowledgeFileRepository;
import cn.krismile.ai.agent.repository.knowledge.UserKnowledgeRepository;
import cn.krismile.ai.agent.repository.platform.AiModelRepository;
import cn.krismile.ai.agent.repository.platform.AiPlatformRepository;
import cn.krismile.ai.agent.structure.rag.file.FileParser;
import cn.krismile.ai.agent.structure.rag.file.context.BatchOperationContext;
import cn.krismile.ai.agent.util.ObjectMapperUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import host.springboot.framework3.core.logging.LoggingComponent;
import host.springboot.framework3.core.model.Pair;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件服务实现类
 * 响应式重构版 - 基于 Project Reactor
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService, LoggingComponent {

    private final TransactionalOperator transactionalOperator;
    private final UserKnowledgeFileRepository userKnowledgeFileRepository;
    private final UserKnowledgeFileDetailRepository userKnowledgeFileDetailRepository;
    private final UserKnowledgeRepository userKnowledgeRepository;
    private final AiModelRepository aiModelRepository;
    private final AiPlatformRepository aiPlatformRepository;

    @Override
    public Flux<KnowledgeFileVO> list(Long knowledgeId) {
        return SecurityUtils.getUserId()
                .flatMapMany(loginId -> userKnowledgeFileRepository.findByRelKnowledgeIdAndRelUserIdAndStatus(
                        knowledgeId, loginId, UserKnowledgeFileStatusEnum.SAVED))
                .flatMap(file -> aiModelRepository.findById(file.getRelEmbeddingModelId())
                        .switchIfEmpty(Mono.defer(() -> {
                            logInstance().error("未找到对应的模型", Pair.of("modelId", file.getRelEmbeddingModelId()));
                            return Mono.error(new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, "未找到对应的模型"));
                        }))
                        .flatMap(model -> aiPlatformRepository.findById(model.getRelPlatformId())
                                .switchIfEmpty(Mono.defer(() -> {
                                    logInstance().error("未找到对应的模型平台", Pair.of("platformId", model.getRelPlatformId()));
                                    return Mono.error(new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, "未找到对应的模型平台"));
                                }))
                                .flatMap(platform -> Mono.just(KnowledgeFileVO.from(file, ChatModelVO.from(platform, model))))
                        ));
    }

    @Override
    public Flux<KnowledgeFileDetailVO> listFileDetails(Long fileId) {
        return SecurityUtils.getUserId().flatMapMany(loginId -> userKnowledgeFileRepository.findByIdAndRelUserId(fileId, loginId)
                        .flatMapMany(file -> userKnowledgeFileDetailRepository.findByRelFileId(fileId)))
                .map(KnowledgeFileDetailVO::from);
    }

    @Override
    public Flux<KnowledgeFileUploadVO> uploads(Long knowledgeId, Flux<FilePart> files) {
        return SecurityUtils.getUserId().flatMapMany(loginId -> this.checkKnowledgeId(knowledgeId, loginId)
                .thenMany(files.flatMap(file -> this.processUpload(knowledgeId, loginId, file))));
    }

    @Override
    public Flux<Long> add(Long knowledgeId, Flux<KnowledgeFileAddRequest> requests) {
        return SecurityUtils.getUserId().flatMapMany(loginId -> this.checkKnowledgeId(knowledgeId, loginId)
                .thenMany(requests.collectList().flatMapMany(reqList -> this.processBatchAdd(loginId, reqList))));
    }

    @Override
    public Mono<Boolean> del(Long fileId) {
        return SecurityUtils.getUserId()
                .flatMap(loginId -> userKnowledgeFileRepository.findByIdAndRelUserId(fileId, loginId)
                        .flatMap(file -> userKnowledgeFileDetailRepository.findByRelFileId(fileId)
                                .collectList()
                                .flatMap(details -> this.deleteFileAndVectorData(file, details))))
                .as(transactionalOperator::transactional);
    }

    /**
     * 根据模型ID创建向量存储
     *
     * @param modelId 模型ID
     * @return 向量存储
     * @since 1.0.0
     */
    private Mono<VectorStore> createVectorStoreByModelId(Long modelId) {
        logInstance().info("开始创建向量存储", List.of(Pair.of("modelId", modelId)));
        return aiModelRepository.findById(modelId)
                .switchIfEmpty(Mono.error(new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, "未找到对应的模型: " + modelId)))
                .flatMap(model -> aiPlatformRepository.findById(model.getRelPlatformId())
                        .switchIfEmpty(Mono.error(new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, "未找到对应的平台: " + model.getRelPlatformId())))
                        .flatMap(platform -> platform.getPlatform().strategy().vectorStore(model.getCode())))
                .doOnError(e -> logInstance().error("创建向量存储失败", e, List.of(Pair.of("modelId", modelId))));
    }

    /**
     * 处理文件上传
     *
     * @param knowledgeId 知识库 ID
     * @param loginId     登录用户 ID
     * @param file        文件分片
     * @return 上传结果
     * @since 1.0.0
     */
    private Mono<KnowledgeFileUploadVO> processUpload(Long knowledgeId, Long loginId, FilePart file) {
        return this.validateAndParseFile(file)
                .flatMap(documents -> this.saveFileRecord(knowledgeId, loginId, file.filename())
                        .flatMap(savedFile -> this.saveFileDetails(savedFile.getId(), loginId, documents)
                                .map(savedDetails -> KnowledgeFileUploadVO.from(savedFile, savedDetails)))
                ).as(transactionalOperator::transactional);
    }

    /**
     * 校验并解析文件
     *
     * @param file 文件分片
     * @return 解析后的文档列表
     * @since 1.0.0
     */
    private Mono<List<Document>> validateAndParseFile(FilePart file) {
        String filename = file.filename();
        if (StringUtils.isBlank(filename)) {
            return Mono.error(new ApplicationException(ErrorCodeEnum.USER_UPLOAD_FILE_ERROR, "文件名不能为空"));
        }
        String extension = FilenameUtils.getExtension(filename);
        if (StringUtils.isBlank(extension)) {
            return Mono.error(new ApplicationException(ErrorCodeEnum.USER_UPLOAD_FILE_ERROR, "文件扩展名不能为空"));
        }
        FileParser fileParser = FileParser.create(extension);
        return fileParser.transform(fileParser.parse(file))
                .collectList()
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 保存文件记录
     *
     * @param knowledgeId 知识库 ID
     * @param loginId     用户 ID
     * @param filename    文件名
     * @return 保存后的文件记录
     * @since 1.0.0
     */
    private Mono<UserKnowledgeFileDO> saveFileRecord(Long knowledgeId, Long loginId, String filename) {
        UserKnowledgeFileDO fileDO = new UserKnowledgeFileDO();
        fileDO.setFileName(filename)
                .setStatus(UserKnowledgeFileStatusEnum.UNSAVED)
                .setRelUserId(loginId)
                .setRelKnowledgeId(knowledgeId);
        return userKnowledgeFileRepository.save(fileDO);
    }

    /**
     * 保存文件详情
     *
     * @param fileId    文件 ID
     * @param loginId   用户 ID
     * @param documents 文档列表
     * @return 保存后的文件详情列表
     * @since 1.0.0
     */
    private Mono<List<UserKnowledgeFileDetailDO>> saveFileDetails(Long fileId, Long loginId, List<Document> documents) {
        List<UserKnowledgeFileDetailDO> details = documents.stream().map(document -> {
            // 添加用户 ID 元数据
            document.getMetadata().put(Knowledge.MetaData.USER_ID, loginId);
            UserKnowledgeFileDetailDO detail = new UserKnowledgeFileDetailDO();
            detail.setDocumentId(document.getId());
            detail.setContent(document.getText());
            detail.setMetaData(ObjectMapperUtils.convertValue(document.getMetadata(), ObjectNode.class));
            detail.setRelFileId(fileId);
            return detail;
        }).collect(Collectors.toList());
        return userKnowledgeFileDetailRepository.saveAll(details).collectList();
    }

    /**
     * 批量处理文件添加请求
     *
     * @param loginId  登录用户 ID
     * @param requests 请求列表
     * @return 文件 ID 流
     * @since 1.0.0
     */
    private Flux<Long> processBatchAdd(Long loginId, List<KnowledgeFileAddRequest> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            return Flux.empty();
        }
        return this.loadBatchContext(loginId, requests)
                .flatMapMany(context -> {
                    for (KnowledgeFileAddRequest request : requests) {
                        context.applyRequest(request);
                    }
                    // 待更新文件
                    List<UserKnowledgeFileDO> filesToUpdate = context.getFilesToUpdate();
                    // 待删除文件详情
                    List<UserKnowledgeFileDetailDO> detailsToDelete = context.getDetailsToDelete();
                    // 待保存文件详情
                    List<UserKnowledgeFileDetailDO> detailsToSave = context.getDetailsToSave();
                    // 待添加文档
                    Map<Long, List<Document>> documentsToAddByModel = context.getDocumentsToAddByModel();
                    // 待删除文档 ID
                    Map<Long, List<String>> documentIdsToDeleteByModel = context.getDocumentIdsToDeleteByModel();

                    Set<Long> usedModelIds = new HashSet<>();
                    usedModelIds.addAll(documentsToAddByModel.keySet());
                    usedModelIds.addAll(documentIdsToDeleteByModel.keySet());
                    Mono<Void> vectorResult = Flux.merge(usedModelIds.stream().map(modelId -> {
                                AiModelDO model = context.getModelMap().get(modelId);
                                if (model == null) {
                                    return Mono.<Void>empty();
                                }
                                AiPlatformDO platform = context.getPlatformMap().get(model.getRelPlatformId());
                                if (platform == null) {
                                    return Mono.<Void>empty();
                                }
                                return platform.getPlatform().strategy().vectorStore(model.getCode())
                                        .flatMap(vectorStore -> {
                                            Mono<Void> deleteMono = this.deleteVectorDocs(vectorStore,
                                                    documentIdsToDeleteByModel.get(modelId));
                                            Mono<Void> addMono = this.addVectorDocs(vectorStore,
                                                    documentsToAddByModel.get(modelId));
                                            return deleteMono.then(addMono);
                                        });
                            })
                            .toList()).then();
                    Flux<Long> deleteFailedIds = Flux.fromIterable(detailsToDelete)
                            .concatMap(detail -> userKnowledgeFileDetailRepository.deleteById(detail.getId())
                                    .then(Mono.<Long>empty())
                                    .onErrorResume(e -> {
                                        logInstance().error("删除文件详情失败", e, List.of(Pair.of("detailId", detail.getId())));
                                        return Mono.just(detail.getId());
                                    }));
                    Flux<Long> saveFailedIds = Flux.fromIterable(detailsToSave)
                            .concatMap(detail -> userKnowledgeFileDetailRepository.save(detail)
                                    .then(Mono.<Long>empty())
                                    .onErrorResume(e -> {
                                        logInstance().error("保存文件详情失败", e, List.of(
                                                Pair.of("detailId", detail.getId()),
                                                Pair.of("relFileId", detail.getRelFileId())));
                                        return Mono.just(detail.getId());
                                    }));
                    return userKnowledgeFileRepository.saveAll(filesToUpdate)
                            .thenMany(Flux.concat(deleteFailedIds, saveFailedIds))
                            .collectList()
                            .as(transactionalOperator::transactional)
                            .flatMapMany(failedIds -> CollectionUtils.isEmpty(failedIds)
                                    ? vectorResult.thenMany(Flux.empty())
                                    : Flux.fromIterable(failedIds));
                });
    }

    /**
     * 加载批量处理上下文
     *
     * @param loginId  登录用户 ID
     * @param requests 请求列表
     * @return 批量处理上下文
     * @since 1.0.0
     */
    private Mono<BatchOperationContext> loadBatchContext(Long loginId, List<KnowledgeFileAddRequest> requests) {
        // 文件 ID
        Set<Long> fileIds = requests.stream()
                .map(KnowledgeFileAddRequest::id)
                .collect(Collectors.toSet());
        // 嵌入模型 ID
        Set<Long> embeddingModelIds = requests.stream()
                .map(KnowledgeFileAddRequest::embeddingModelId)
                .collect(Collectors.toSet());
        // 文件映射
        Mono<Map<Long, UserKnowledgeFileDO>> fileMapMono = userKnowledgeFileRepository
                .findByRelUserIdAndIdIn(loginId, fileIds)
                .collectMap(UserKnowledgeFileDO::getId);
        // 模型映射
        Mono<Map<Long, AiModelDO>> modelMapMono = aiModelRepository
                .findAllById(embeddingModelIds)
                .collectMap(AiModelDO::getId);
        // 文件详情映射
        Mono<Map<Long, List<UserKnowledgeFileDetailDO>>> detailsMapMono = userKnowledgeFileDetailRepository
                .findByRelFileIdIn(fileIds)
                .collect(Collectors.groupingBy(UserKnowledgeFileDetailDO::getRelFileId));

        return Mono.zip(fileMapMono, modelMapMono, detailsMapMono)
                .flatMap(tuple -> {
                    Map<Long, UserKnowledgeFileDO> fileMap = tuple.getT1();
                    Map<Long, AiModelDO> modelMap = tuple.getT2();
                    Map<Long, List<UserKnowledgeFileDetailDO>> detailsMap = tuple.getT3();
                    // 平台 ID
                    Set<Long> platformIds = modelMap.values().stream()
                            .map(AiModelDO::getRelPlatformId)
                            .collect(Collectors.toSet());
                    return aiPlatformRepository.findAllById(platformIds)
                            .collectMap(AiPlatformDO::getId)
                            .map(platformMap -> new BatchOperationContext(
                                    fileMap, modelMap, detailsMap, platformMap));
                });
    }

    /**
     * 删除向量文档
     *
     * @param vectorStore 向量库
     * @param idsToDelete 待删除 ID 列表
     * @return 删除操作
     * @since 1.0.0
     */
    private Mono<Void> deleteVectorDocs(VectorStore vectorStore, List<String> idsToDelete) {
        if (CollectionUtils.isEmpty(idsToDelete)) {
            return Mono.empty();
        }
        return Mono.fromRunnable(() -> vectorStore.delete(idsToDelete))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    /**
     * 新增向量文档
     *
     * @param vectorStore 向量库
     * @param docsToAdd   待新增文档
     * @return 新增操作
     * @since 1.0.0
     */
    private Mono<Void> addVectorDocs(VectorStore vectorStore, List<Document> docsToAdd) {
        if (CollectionUtils.isEmpty(docsToAdd)) {
            return Mono.empty();
        }
        return Flux.fromIterable(docsToAdd)
                .buffer(10)
                .flatMap(batch -> Mono.fromRunnable(() -> vectorStore.add(batch))
                        .subscribeOn(Schedulers.boundedElastic()), 1)
                .then();
    }


    /**
     * 删除文件及向量数据
     *
     * @param file    文件实体
     * @param details 详情列表
     * @return 结果
     * @since 1.0.0
     */
    private Mono<Boolean> deleteFileAndVectorData(UserKnowledgeFileDO file, List<UserKnowledgeFileDetailDO> details) {
        Mono<Void> deleteVectorStore = Mono.empty();
        if (CollectionUtils.isNotEmpty(details)) {
            List<String> docIds = details.stream()
                    .map(UserKnowledgeFileDetailDO::getDocumentId)
                    .toList();
            deleteVectorStore = this.createVectorStoreByModelId(file.getRelEmbeddingModelId())
                    .flatMap(vectorStore -> Mono.fromRunnable(() -> vectorStore.delete(docIds))
                            .subscribeOn(Schedulers.boundedElastic()))
                    .then();
        }

        return userKnowledgeFileDetailRepository.deleteByRelFileId(file.getId())
                .then(userKnowledgeFileRepository.deleteById(file.getId()))
                .then(deleteVectorStore)
                .thenReturn(true);
    }

    /**
     * 校验知识库 ID
     *
     * @param knowledgeId 知识库 ID
     * @param loginId     登录用户 ID
     * @return Void
     * @since 1.0.0
     */
    private Mono<Void> checkKnowledgeId(Long knowledgeId, Long loginId) {
        return userKnowledgeRepository.existsByIdAndRelUserId(knowledgeId, loginId)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, "知识库不存在")));
    }

    @Override
    public @NonNull String logTag() {
        return "文件服务";
    }
}
