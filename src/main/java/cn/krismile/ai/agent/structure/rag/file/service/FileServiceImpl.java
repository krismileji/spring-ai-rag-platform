package cn.krismile.ai.agent.structure.rag.file.service;

import cn.krismile.ai.agent.configuration.security.context.SecurityUtils;
import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDO;
import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDetailDO;
import cn.krismile.ai.agent.model.enumeration.UserKnowledgeFileStatusEnum;
import cn.krismile.ai.agent.model.request.knowledge.KnowLedgeFileDetailAddRequest;
import cn.krismile.ai.agent.model.request.knowledge.KnowledgeFileAddRequest;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileDetailVO;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileUploadVO;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileVO;
import cn.krismile.ai.agent.repository.knowledge.UserKnowledgeFileDetailRepository;
import cn.krismile.ai.agent.repository.knowledge.UserKnowledgeFileRepository;
import cn.krismile.ai.agent.repository.knowledge.UserKnowledgeRepository;
import cn.krismile.ai.agent.structure.rag.file.FileParser;
import cn.krismile.ai.agent.util.ObjectMapperUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import host.springboot.framework3.core.logging.LoggingComponent;
import jakarta.annotation.Resource;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * FileServiceImpl
 * 响应式重构版 - 基于 Project Reactor
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class FileServiceImpl implements FileService, LoggingComponent {

    @Resource
    private VectorStore vectorStore;
    @Resource
    private TransactionalOperator transactionalOperator;
    @Resource
    private UserKnowledgeFileRepository userKnowledgeFileRepository;
    @Resource
    private UserKnowledgeFileDetailRepository userKnowledgeFileDetailRepository;
    @Resource
    private UserKnowledgeRepository userKnowledgeRepository;

    @Override
    public Flux<KnowledgeFileVO> list(Long knowledgeId) {
        return SecurityUtils.getUserId()
                .flatMapMany(loginId -> userKnowledgeFileRepository.findByRelKnowledgeIdAndRelUserIdAndStatus(
                        knowledgeId, loginId, UserKnowledgeFileStatusEnum.SAVED))
                .map(KnowledgeFileVO::from);
    }

    @Override
    public Flux<KnowledgeFileDetailVO> listFileDetails(Long fileId) {
        return SecurityUtils.getUserId()
                .flatMapMany(loginId -> userKnowledgeFileRepository.findByIdAndRelUserId(fileId, loginId)
                        .flatMapMany(file -> userKnowledgeFileDetailRepository.findByRelFileId(fileId)))
                .map(KnowledgeFileDetailVO::from);
    }

    @Override
    public Flux<KnowledgeFileUploadVO> uploads(Long knowledgeId, Flux<FilePart> files) {
        return SecurityUtils.getUserId()
                .flatMapMany(loginId -> this.checkKnowledgeId(knowledgeId, loginId).thenMany(
                        files.flatMap(file -> this.processUpload(knowledgeId, loginId, file))));
    }

    /**
     * 处理文件上传
     *
     * @param knowledgeId 知识库ID
     * @param loginId     登录用户ID
     * @param file        文件分片
     * @return 上传结果
     * @since 1.0.0
     */
    private Mono<KnowledgeFileUploadVO> processUpload(Long knowledgeId, Long loginId, FilePart file) {
        String filename = file.filename();
        if (StringUtils.isBlank(filename)) {
            return Mono.error(new ApplicationException(ErrorCodeEnum.USER_UPLOAD_FILE_ERROR, "文件名不能为空"));
        }
        String extension = FilenameUtils.getExtension(filename);
        if (StringUtils.isBlank(extension)) {
            return Mono.error(new ApplicationException(ErrorCodeEnum.USER_UPLOAD_FILE_ERROR, "文件扩展名不能为空"));
        }

        return FileParser.create(extension).parse(file)
                .collectList()
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(documents -> {
                    UserKnowledgeFileDO fileDO = new UserKnowledgeFileDO();
                    fileDO.setFileName(filename)
                            .setStatus(UserKnowledgeFileStatusEnum.UNSAVED)
                            .setRelUserId(loginId)
                            .setRelKnowledgeId(knowledgeId);

                    return userKnowledgeFileRepository.save(fileDO)
                            .flatMap(savedFile -> {
                                List<UserKnowledgeFileDetailDO> details = documents.stream().map(document -> {
                                    UserKnowledgeFileDetailDO detail = new UserKnowledgeFileDetailDO();
                                    detail.setDocumentId(document.getId());
                                    detail.setContent(document.getText());
                                    detail.setMetaData(ObjectMapperUtils.convertValue(document.getMetadata(), ObjectNode.class));
                                    detail.setRelFileId(savedFile.getId());
                                    return detail;
                                }).collect(Collectors.toList());

                                return userKnowledgeFileDetailRepository.saveAll(details)
                                        .collectList()
                                        .map(savedDetails -> KnowledgeFileUploadVO.from(savedFile, savedDetails));
                            });
                })
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<Long> add(Long knowledgeId, Flux<KnowledgeFileAddRequest> requests) {
        return SecurityUtils.getUserId()
                .flatMapMany(loginId -> this.checkKnowledgeId(knowledgeId, loginId)
                        .thenMany(requests.flatMap(request ->
                                this.processAdd(loginId, request))));
    }

    /**
     * 处理文件添加
     *
     * @param loginId 登录用户ID
     * @param request 添加请求
     * @return 文件ID
     * @since 1.0.0
     */
    private Mono<Long> processAdd(Long loginId, KnowledgeFileAddRequest request) {
        Long fileId = request.id();
        String description = request.description();
        List<KnowLedgeFileDetailAddRequest> reqDetails = request.details();

        return userKnowledgeFileRepository.findByIdAndRelUserId(fileId, loginId)
                .switchIfEmpty(Mono.error(new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, "文件不存在")))
                .flatMap(dbFile -> {
                    dbFile.setDescription(description);
                    dbFile.setStatus(UserKnowledgeFileStatusEnum.SAVED);

                    return userKnowledgeFileRepository.save(dbFile)
                            .then(this.processDetails(fileId, reqDetails))
                            .then(Mono.<Long>empty());
                })
                .as(transactionalOperator::transactional)
                .onErrorResume(e -> {
                    logInstance().error("文件保存失败", e);
                    return Mono.just(fileId);
                });
    }

    /**
     * 处理文件详情更新
     *
     * @param fileId     文件ID
     * @param reqDetails 详情请求列表
     * @return Void
     * @since 1.0.0
     */
    private Mono<Void> processDetails(Long fileId, List<KnowLedgeFileDetailAddRequest> reqDetails) {
        return userKnowledgeFileDetailRepository.findByRelFileId(fileId).collectList()
                .flatMap(dbDetails -> {
                    Map<Long, UserKnowledgeFileDetailDO> dbDetailMap = dbDetails.stream()
                            .collect(Collectors.toMap(UserKnowledgeFileDetailDO::getId, Function.identity()));

                    List<Long> reqDetailIds = reqDetails.stream()
                            .map(KnowLedgeFileDetailAddRequest::id)
                            .filter(Objects::nonNull)
                            .toList();

                    List<Long> idsToDelete = dbDetails.stream()
                            .map(UserKnowledgeFileDetailDO::getId)
                            .filter(id -> !reqDetailIds.contains(id))
                            .toList();

                    List<UserKnowledgeFileDetailDO> toSave = new ArrayList<>();

                    for (KnowLedgeFileDetailAddRequest reqDetail : reqDetails) {
                        UserKnowledgeFileDetailDO entity;
                        if (reqDetail.id() != null) {
                            UserKnowledgeFileDetailDO existing = dbDetailMap.get(reqDetail.id());
                            if (existing == null) {
                                return Mono.error(new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, "详情不存在"));
                            }
                            entity = existing;
                            ObjectNode newMeta = reqDetail.metaData();
                            ObjectNode oldMeta = entity.getMetaData();
                            if (oldMeta != null && newMeta != null) {
                                // 手动合并 metaData
                                Iterator<String> fieldNames = newMeta.fieldNames();
                                while (fieldNames.hasNext()) {
                                    String key = fieldNames.next();
                                    if (!oldMeta.has(key)) {
                                        oldMeta.set(key, newMeta.get(key));
                                    }
                                }
                                entity.setMetaData(oldMeta);
                            } else if (newMeta != null) {
                                entity.setMetaData(newMeta);
                            }
                            entity.setContent(reqDetail.content());
                        } else {
                            entity = new UserKnowledgeFileDetailDO();
                            entity.setDocumentId(UUID.randomUUID().toString());
                            entity.setContent(reqDetail.content());
                            entity.setMetaData(reqDetail.metaData());
                            entity.setRelFileId(fileId);
                        }
                        toSave.add(entity);
                    }

                    Mono<Void> deleteMono = idsToDelete.isEmpty() ? Mono.empty() :
                            userKnowledgeFileDetailRepository.deleteAllById(idsToDelete);


                    return deleteMono.then(userKnowledgeFileDetailRepository.saveAll(toSave).collectList())
                            .flatMap(savedDetails -> {
                                List<Document> documents = savedDetails.stream()
                                        .map(UserKnowledgeFileDetailDO::toDocument)
                                        .toList();
                                return Flux.fromIterable(documents)
                                        .buffer(10)
                                        // 限制并发数为 1，避免触发限流 (HTTP 429)
                                        .flatMap(batch -> Mono.fromRunnable(() -> vectorStore.add(batch))
                                                .subscribeOn(Schedulers.boundedElastic()), 1)
                                        .then();
                            }).then();
                });
    }

    @Override
    public Mono<Boolean> del(Long fileId) {
        return SecurityUtils.getUserId()
                .flatMap(loginId -> userKnowledgeFileRepository.findByIdAndRelUserId(fileId, loginId)
                        .flatMap(file -> userKnowledgeFileDetailRepository.findByRelFileId(fileId)
                                .collectList()
                                .flatMap(details -> {
                                    List<String> docIds = details.stream()
                                            .map(UserKnowledgeFileDetailDO::getDocumentId)
                                            .toList();

                                    return userKnowledgeFileDetailRepository.deleteByRelFileId(fileId)
                                            .then(userKnowledgeFileRepository.deleteById(file.getId()))
                                            .then(Mono.fromRunnable(() -> vectorStore.delete(docIds))
                                                    .subscribeOn(Schedulers.boundedElastic()));

                                })
                                .thenReturn(true))
                )
                .as(transactionalOperator::transactional);
    }

    /**
     * 校验知识库ID
     *
     * @param knowledgeId 知识库ID
     * @param loginId     登录用户ID
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
