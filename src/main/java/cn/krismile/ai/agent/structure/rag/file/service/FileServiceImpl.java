package cn.krismile.ai.agent.structure.rag.file.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.krismile.ai.agent.context.RequestContext;
import cn.krismile.ai.agent.model.domain.UserKnowledgeDO;
import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDO;
import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDetailDO;
import cn.krismile.ai.agent.model.enumeration.UserKnowledgeFileStatusEnum;
import cn.krismile.ai.agent.model.request.knowledge.KnowLedgeFileDetailAddRequest;
import cn.krismile.ai.agent.model.request.knowledge.KnowledgeFileAddRequest;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileDetailVO;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileUploadVO;
import cn.krismile.ai.agent.model.response.knowledge.KnowledgeFileVO;
import cn.krismile.ai.agent.repository.knowledge.UserKnowledgeFileDetailRepository;
import cn.krismile.ai.agent.structure.SchedulerDelegate;
import cn.krismile.ai.agent.structure.rag.file.FileParser;
import cn.krismile.ai.agent.util.ObjectMapperUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import host.springboot.framework3.core.logging.LoggingComponent;
import host.springboot.framework3.core.util.UUIDUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;

import static cn.krismile.ai.agent.model.domain.table.UserKnowledgeDOTableDef.USER_KNOWLEDGE_DO;
import static cn.krismile.ai.agent.model.domain.table.UserKnowledgeFileDOTableDef.USER_KNOWLEDGE_FILE_DO;
import static cn.krismile.ai.agent.model.domain.table.UserKnowledgeFileDetailDOTableDef.USER_KNOWLEDGE_FILE_DETAIL_DO;

/**
 * FileServiceImpl
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Service
public class FileServiceImpl implements FileService, LoggingComponent {

    @Resource
    private VectorStore vectorStore;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private UserKnowledgeFileDetailRepository userKnowledgeFileDetailRepository;

    @Override
    public List<KnowledgeFileVO> list(Long knowledgeId) {
        Long loginId = RequestContext.syncApply(StpUtil::getLoginIdAsLong);
        return UserKnowledgeFileDO.create()
                .where(USER_KNOWLEDGE_FILE_DO.REL_KNOWLEDGE_ID.eq(knowledgeId)
                        .and(USER_KNOWLEDGE_FILE_DO.REL_USER_ID.eq(loginId))
                        .and(USER_KNOWLEDGE_FILE_DO.STATUS.eq(UserKnowledgeFileStatusEnum.SAVED)))
                .list().stream()
                .map(KnowledgeFileVO::from)
                .toList();
    }

    @Override
    public List<KnowledgeFileDetailVO> listFileDetails(Long fileId) {
        Long loginId = RequestContext.syncApply(StpUtil::getLoginIdAsLong);
        return UserKnowledgeFileDetailDO.create()
                .where(USER_KNOWLEDGE_FILE_DETAIL_DO.REL_FILE_ID.eq(fileId))
                .innerJoin(UserKnowledgeFileDO.class).on(USER_KNOWLEDGE_FILE_DO.ID.eq(USER_KNOWLEDGE_FILE_DETAIL_DO.REL_FILE_ID)
                        .and(USER_KNOWLEDGE_FILE_DO.REL_USER_ID.eq(loginId)))
                .list().stream()
                .map(KnowledgeFileDetailVO::from)
                .toList();
    }

    @Override
    public Flux<KnowledgeFileUploadVO> uploads(Long knowledgeId, Flux<FilePart> files) {
        Long loginId = RequestContext.syncApply(StpUtil::getLoginIdAsLong);
        this.checkKnowledgeId(knowledgeId);
        return files.flatMap(file -> {
            String filename = file.filename();
            if (StringUtils.isBlank(filename)) {
                return Flux.error(new ApplicationException(ErrorCodeEnum.USER_UPLOAD_FILE_ERROR, "文件名不能为空"));
            }
            String extension = FilenameUtils.getExtension(filename);
            if (StringUtils.isBlank(extension)) {
                return Flux.error(new ApplicationException(ErrorCodeEnum.USER_UPLOAD_FILE_ERROR, "文件扩展名不能为空"));
            }
            return FileParser.create(extension).parse(file).collectList()
                    .publishOn(SchedulerDelegate.create(Schedulers.boundedElastic()))
                    .flatMap(documents -> Mono.fromCallable(() ->
                            this.transactionTemplate.execute(status -> {
                                UserKnowledgeFileDO dbFile = UserKnowledgeFileDO.create()
                                        .setFileName(filename)
                                        .setStatus(UserKnowledgeFileStatusEnum.UNSAVED)
                                        .setRelUserId(loginId)
                                        .setRelKnowledgeId(knowledgeId)
                                        .saveOpt().orElseThrow(() -> new ApplicationException(
                                                ErrorCodeEnum.USER_UPLOAD_FILE_ERROR, "文件保存失败"));
                                List<UserKnowledgeFileDetailDO> details = documents.stream().map(document ->
                                        UserKnowledgeFileDetailDO.create()
                                                .setDocumentId(document.getId())
                                                .setContent(document.getText())
                                                .setMetaData(ObjectMapperUtils.convertValue(document.getMetadata(), ObjectNode.class))
                                                .setRelFileId(dbFile.getId())
                                                .saveOpt().orElseThrow(() -> new ApplicationException(
                                                        ErrorCodeEnum.USER_UPLOAD_FILE_ERROR, "文档详情保存失败"))).toList();
                                return KnowledgeFileUploadVO.from(dbFile, details);
                            })));
        });
    }

    @Override
    public Flux<Long> add(Long knowledgeId, Flux<KnowledgeFileAddRequest> requests) {
        this.checkKnowledgeId(knowledgeId);
        Long loginId = RequestContext.syncApply(StpUtil::getLoginIdAsLong);
        return requests.publishOn(SchedulerDelegate.create(Schedulers.boundedElastic()))
                .flatMap(request -> Mono.fromCallable(() -> {
                    Long fileId = request.id();
                    String description = request.description();
                    List<KnowLedgeFileDetailAddRequest> details = request.details();
                    try {
                        List<Long> detailIds = details.stream()
                                .map(KnowLedgeFileDetailAddRequest::id)
                                .filter(Objects::nonNull).toList();
                        List<Long> delDetailIds = new ArrayList<>();
                        Map<Long, UserKnowledgeFileDetailDO> dbId2Detail = new HashMap<>();
                        if (CollectionUtils.isNotEmpty(detailIds)) {
                            // 查出文件关联的所有详情
                            List<UserKnowledgeFileDetailDO> dbDetails = UserKnowledgeFileDetailDO.create()
                                    .where(USER_KNOWLEDGE_FILE_DETAIL_DO.REL_FILE_ID.eq(fileId))
                                    .list();
                            if (CollectionUtils.isNotEmpty(dbDetails)) {
                                for (UserKnowledgeFileDetailDO dbDetail : dbDetails) {
                                    Long dbId = dbDetail.getId();
                                    if (detailIds.contains(dbId)) {
                                        dbId2Detail.put(dbId, dbDetail);
                                    } else {
                                        delDetailIds.add(dbId);
                                    }
                                }
                            }
                        }
                        List<UserKnowledgeFileDetailDO> editDetails = new ArrayList<>();
                        for (KnowLedgeFileDetailAddRequest detail : details) {
                            Long detailId = detail.id();
                            String content = detail.content();
                            ObjectNode metaData = detail.metaData();

                            UserKnowledgeFileDetailDO entity = UserKnowledgeFileDetailDO.create()
                                    .setId(detailId)
                                    .setDocumentId(UUIDUtils.randomOrigin())
                                    .setContent(content)
                                    .setMetaData(metaData)
                                    .setRelFileId(fileId);
                            if (detailId != null) {
                                UserKnowledgeFileDetailDO dbDetail = Optional.ofNullable(dbId2Detail.get(detailId))
                                        .orElseThrow(() -> new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, "详情不存在"));
                                ObjectNode dbMetaData = dbDetail.getMetaData();
                                if (dbMetaData != null && metaData != null) {
                                    metaData.fieldNames().forEachRemaining(key -> {
                                        if (!dbMetaData.has(key)) {
                                            dbMetaData.set(key, metaData.get(key));
                                        }
                                    });
                                }
                            } else {
                                entity.setDocumentId(UUID.randomUUID().toString());
                            }
                            editDetails.add(entity);
                        }

                        this.transactionTemplate.execute(status -> {
                            UserKnowledgeFileDO.create()
                                    .where(USER_KNOWLEDGE_FILE_DO.ID.eq(fileId)
                                            .and(USER_KNOWLEDGE_FILE_DO.REL_USER_ID.eq(loginId)))
                                    .setDescription(description)
                                    .setStatus(UserKnowledgeFileStatusEnum.SAVED)
                                    .updateOpt().orElseThrow(() -> new ApplicationException(
                                            ErrorCodeEnum.DATABASE_SERVICE_ERROR, "文件保存失败"));
                            this.userKnowledgeFileDetailRepository.removeByIds(delDetailIds);
                            this.userKnowledgeFileDetailRepository.saveOrUpdateBatch(editDetails);
                            List<Document> documents = editDetails.stream()
                                    .map(UserKnowledgeFileDetailDO::toDocument)
                                    .toList();
                            this.vectorStore.add(documents);
                            return status;
                        });
                        return null;
                    } catch (Exception e) {
                        logInstance().error("文件保存失败", e);
                        return fileId;
                    }
                }))
                .filter(Objects::nonNull);
    }

    @Override
    public Boolean del(Long fileId) {
        return this.transactionTemplate.execute(status -> {
            Long loginId = RequestContext.syncApply(StpUtil::getLoginIdAsLong);
            UserKnowledgeFileDO.create()
                    .where(USER_KNOWLEDGE_FILE_DO.ID.eq(fileId)
                            .and(USER_KNOWLEDGE_FILE_DO.REL_USER_ID.eq(loginId)))
                    .removeOpt().orElseThrow(() -> new ApplicationException(
                            ErrorCodeEnum.DATABASE_SERVICE_ERROR, "文件删除失败"));
            List<UserKnowledgeFileDetailDO> details = UserKnowledgeFileDetailDO.create()
                    .select(USER_KNOWLEDGE_FILE_DETAIL_DO.ID, USER_KNOWLEDGE_FILE_DETAIL_DO.DOCUMENT_ID)
                    .where(USER_KNOWLEDGE_FILE_DETAIL_DO.REL_FILE_ID.eq(fileId))
                    .list();
            if (CollectionUtils.isNotEmpty(details)) {
                List<Long> detailIds = new ArrayList<>();
                List<String> documentIds = new ArrayList<>();
                for (UserKnowledgeFileDetailDO detail : details) {
                    detailIds.add(detail.getId());
                    documentIds.add(detail.getDocumentId());
                }
                UserKnowledgeFileDetailDO.create()
                        .where(USER_KNOWLEDGE_FILE_DETAIL_DO.ID.in(detailIds))
                        .removeOpt().orElseThrow(() -> new ApplicationException(
                                ErrorCodeEnum.DATABASE_SERVICE_ERROR, "文件删除失败"));
                this.userKnowledgeFileDetailRepository.removeByIds(detailIds);
                this.vectorStore.delete(documentIds);
            }
            return true;
        });
    }

    private void checkKnowledgeId(Long knowledgeId) {
        Long loginId = RequestContext.syncApply(StpUtil::getLoginIdAsLong);
        if (!UserKnowledgeDO.create()
                .where(USER_KNOWLEDGE_DO.ID.eq(knowledgeId)
                        .and(USER_KNOWLEDGE_DO.REL_USER_ID.eq(loginId)))
                .exists()) {
            throw new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, "知识库不存在");
        }
    }

    @Override
    public @NonNull String logTag() {
        return "文件服务";
    }
}