package cn.krismile.ai.agent.structure.rag.file.context;

import cn.krismile.ai.agent.model.domain.AiModelDO;
import cn.krismile.ai.agent.model.domain.AiPlatformDO;
import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDO;
import cn.krismile.ai.agent.model.domain.UserKnowledgeFileDetailDO;
import cn.krismile.ai.agent.model.enumeration.UserKnowledgeFileStatusEnum;
import cn.krismile.ai.agent.model.request.knowledge.KnowLedgeFileDetailAddRequest;
import cn.krismile.ai.agent.model.request.knowledge.KnowledgeFileAddRequest;
import com.fasterxml.jackson.databind.node.ObjectNode;
import host.springboot.framework3.core.enumeration.error.ErrorCodeEnum;
import host.springboot.framework3.core.exception.ApplicationException;
import host.springboot.framework3.core.logging.LoggingComponent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 批量新增文件上下文
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public class BatchOperationContext implements LoggingComponent {

    // region 入参

    /**
     * 文件映射
     */
    private final Map<Long, UserKnowledgeFileDO> fileMap;

    /**
     * 模型映射
     */
    private final Map<Long, AiModelDO> modelMap;

    /**
     * 详情映射
     */
    private final Map<Long, List<UserKnowledgeFileDetailDO>> detailsMap;

    /**
     * 平台映射
     */
    private final Map<Long, AiPlatformDO> platformMap;

    // endregion

    // region 过程

    /**
     * 待更新文件
     */
    private final List<UserKnowledgeFileDO> filesToUpdate = new ArrayList<>();

    /**
     * 待删除详情
     */
    private final List<UserKnowledgeFileDetailDO> detailsToDelete = new ArrayList<>();

    /**
     * 待保存详情
     */
    private final List<UserKnowledgeFileDetailDO> detailsToSave = new ArrayList<>();

    /**
     * 结果 ID 列表
     */
    private final List<Long> resultIds = new ArrayList<>();

    /**
     * 待新增文档映射
     */
    private final Map<Long, List<Document>> documentsToAddByModel = new HashMap<>();

    /**
     * 待删除文档 ID 映射
     */
    private final Map<Long, List<String>> documentIdsToDeleteByModel = new HashMap<>();

    // endregion

    /**
     * 应用单条请求
     *
     * @param request 请求
     * @since 1.0.0
     */
    public void applyRequest(KnowledgeFileAddRequest request) {
        Long fileId = request.id();
        UserKnowledgeFileDO existingFile = this.fileMap.get(fileId);
        if (existingFile == null) {
            logInstance().error("文件不存在: " + fileId);
            this.resultIds.add(fileId);
            return;
        }

        AiModelDO model = this.modelMap.get(request.embeddingModelId());
        if (model == null || BooleanUtils.isNotTrue(model.getEnabled())) {
            logInstance().error("模型不存在或未启用: " + request.embeddingModelId());
            this.resultIds.add(fileId);
            return;
        }

        existingFile.setDescription(request.description());
        existingFile.setRelEmbeddingModelId(request.embeddingModelId());
        existingFile.setStatus(UserKnowledgeFileStatusEnum.SAVED);
        this.filesToUpdate.add(existingFile);
        this.resultIds.add(existingFile.getId());

        List<UserKnowledgeFileDetailDO> dbDetails = this.detailsMap.getOrDefault(fileId, Collections.emptyList());
        Map<Long, UserKnowledgeFileDetailDO> dbDetailMap = dbDetails.stream()
                .collect(Collectors.toMap(UserKnowledgeFileDetailDO::getId, Function.identity()));

        List<KnowLedgeFileDetailAddRequest> reqDetails = request.details();
        Set<Long> reqDetailIds = this.extractRequestDetailIds(reqDetails);
        List<UserKnowledgeFileDetailDO> toDelete = dbDetails.stream()
                .filter(detail -> !reqDetailIds.contains(detail.getId()))
                .toList();
        this.detailsToDelete.addAll(toDelete);

        if (!toDelete.isEmpty()) {
            this.documentIdsToDeleteByModel.computeIfAbsent(model.getId(), key -> new ArrayList<>())
                    .addAll(toDelete.stream().map(UserKnowledgeFileDetailDO::getDocumentId).toList());
        }

        List<UserKnowledgeFileDetailDO> toSave = this.prepareDetailsToSave(fileId, reqDetails, dbDetailMap);
        this.detailsToSave.addAll(toSave);
        this.documentsToAddByModel.computeIfAbsent(model.getId(), key -> new ArrayList<>())
                .addAll(toSave.stream().map(UserKnowledgeFileDetailDO::toDocument).toList());
    }

    /**
     * 提取请求详情 ID 集合
     *
     * @param reqDetails 请求详情
     * @return 详情 ID 集合
     * @since 1.0.0
     */
    private Set<Long> extractRequestDetailIds(List<KnowLedgeFileDetailAddRequest> reqDetails) {
        return reqDetails.stream()
                .map(KnowLedgeFileDetailAddRequest::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 准备需要保存的详情对象
     *
     * @param fileId      文件 ID
     * @param reqDetails  请求详情
     * @param dbDetailMap 数据库现有详情映射
     * @return 待保存列表
     * @since 1.0.0
     */
    private List<UserKnowledgeFileDetailDO> prepareDetailsToSave(
            Long fileId,
            List<KnowLedgeFileDetailAddRequest> reqDetails,
            Map<Long, UserKnowledgeFileDetailDO> dbDetailMap) {
        List<UserKnowledgeFileDetailDO> toSave = new ArrayList<>(reqDetails.size());
        for (KnowLedgeFileDetailAddRequest reqDetail : reqDetails) {
            UserKnowledgeFileDetailDO entity;
            if (reqDetail.id() != null) {
                UserKnowledgeFileDetailDO existing = dbDetailMap.get(reqDetail.id());
                if (existing == null) {
                    throw new ApplicationException(ErrorCodeEnum.INVALID_USER_INPUT, "详情不存在: " + reqDetail.id());
                }
                entity = existing;
                this.mergeMetaData(entity, reqDetail.metaData());
            } else {
                entity = new UserKnowledgeFileDetailDO();
                entity.setDocumentId(UUID.randomUUID().toString());
                entity.setMetaData(reqDetail.metaData());
                entity.setRelFileId(fileId);
            }
            entity.setContent(reqDetail.content());
            toSave.add(entity);
        }
        return toSave;
    }

    /**
     * 合并 MetaData
     *
     * @param entity  实体
     * @param newMeta 新的 MetaData
     * @since 1.0.0
     */
    private void mergeMetaData(UserKnowledgeFileDetailDO entity, ObjectNode newMeta) {
        ObjectNode oldMeta = entity.getMetaData();
        if (oldMeta != null && newMeta != null) {
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
    }

    /**
     * 日志标签
     *
     * @return 日志标签
     * @since 1.0.0
     */
    @Override
    public @NonNull String logTag() {
        return "批量文件上下文";
    }
}
