<template>
  <el-dialog
    v-model="visible"
    :title="props.readonly ? t('knowledge.file_dialog.title_view') : t('knowledge.file_dialog.title_edit')"
    width="90%"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- 加载中状态 -->
    <div
      v-if="loading"
      v-loading="true"
      style="min-height: 300px; display: flex; align-items: center; justify-content: center"
    >
      <span style="color: #999">{{ t('knowledge.file_dialog.uploading') }}</span>
    </div>

    <!-- 内容区域 -->
    <div v-else class="file-content-editor">
      <!-- 文件切换标签页 -->
      <el-tabs v-model="activeFileIndex" type="card">
        <el-tab-pane
          v-for="(file, index) in editableFilesData"
          :key="file?.id || index"
          :label="filesData[index]?.fileName || ''"
          :name="String(index)"
        >
          <!-- 失败提示 -->
          <el-alert
            v-if="file && failedFileIds.includes(String(file.id))"
            :title="t('knowledge.file_dialog.save_failed')"
            type="error"
            :closable="false"
            style="margin-bottom: 16px"
          />

          <template v-if="file">
            <!-- 文件描述 -->
            <el-form label-width="100px">
              <el-form-item :label="t('knowledge.file_dialog.file_desc')">
                <el-input
                  v-model="file.description"
                  :placeholder="t('knowledge.file_dialog.file_desc_placeholder')"
                  clearable
                  :disabled="props.readonly"
                />
              </el-form-item>
              <el-form-item :label="t('knowledge.file_dialog.embedding_model')">
                <el-select
                  v-model="file.embeddingModelId"
                  :placeholder="t('knowledge.file_dialog.select_model')"
                  :loading="loadingModels"
                  :disabled="props.readonly"
                  clearable
                  style="width: 100%"
                >
                  <el-option-group
                    v-for="group in groupedModels"
                    :key="group.platformName"
                    :label="group.platformName"
                  >
                    <el-option
                      v-for="item in group.models"
                      :key="item.id"
                      :label="item.modelName"
                      :value="item.id"
                    >
                      <div class="model-option">
                        <span>{{ item.modelName }}</span>
                        <span class="model-tag">{{ item.model }}</span>
                      </div>
                    </el-option>
                  </el-option-group>
                </el-select>
              </el-form-item>
            </el-form>

            <!-- 内容列表 -->
            <div class="content-list">
              <div class="list-header">
                <h3>{{ t('knowledge.file_dialog.parsed_content') }}</h3>
                <el-button v-if="!props.readonly" type="primary" @click="handleAddContent(index)">
                  <el-icon><Plus /></el-icon>
                  {{ t('knowledge.file_dialog.add_content') }}
                </el-button>
              </div>

              <el-table :data="file.details" border stripe>
                <el-table-column type="index" :label="t('common.index')" width="60" />

                <el-table-column :label="t('common.content')" min-width="300">
                  <template #default="{ row }">
                    <el-input
                      v-model="row.content"
                      type="textarea"
                      :rows="3"
                      :placeholder="t('common.input_content_placeholder')"
                      :disabled="props.readonly"
                    />
                  </template>
                </el-table-column>

                <el-table-column :label="t('knowledge.file_dialog.metadata_readonly')" width="250">
                  <template #default="{ row }">
                    <div class="metadata-display">
                      <el-tag
                        v-for="(value, key) in row.metaData"
                        :key="key"
                        size="small"
                        style="margin: 2px"
                      >
                        {{ key }}: {{ value }}
                      </el-tag>
                      <span
                        v-if="!row.metaData || Object.keys(row.metaData).length === 0"
                        class="empty-text"
                      >
                        {{ t('knowledge.file_dialog.no_metadata') }}
                      </span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column v-if="!props.readonly" :label="t('common.action')" width="100" fixed="right">
                  <template #default="{ $index }">
                    <el-button
                      type="danger"
                      size="small"
                      link
                      @click="handleDeleteContent(index, $index)"
                    >
                      {{ t('common.delete') }}
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <el-button @click="handleClose">{{ props.readonly ? t('common.close') : t('common.cancel') }}</el-button>
      <el-button v-if="!props.readonly" type="primary" :loading="saving" @click="handleSave">
        {{ t('common.save') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()
import { Plus } from '@element-plus/icons-vue'
import { addKnowledgeFile, getKnowledgeFileList } from '@/api/knowledge-api'
import { getModelList, type ModelVO } from '@/api/chat-api'
import type {
  KnowledgeFileUploadVO,
  KnowledgeFileAddRequest,
  KnowledgeFileVO,
} from '@/api/model'

const props = defineProps<{
  modelValue: boolean
  filesData: KnowledgeFileUploadVO[]
  knowledgeId: string
  readonly?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const router = useRouter()
const visible = ref(false)
const saving = ref(false)
const loading = ref(false)
const loadingModels = ref(false)
const activeFileIndex = ref('0')
const failedFileIds = ref<string[]>([])
const embeddingModels = ref<ModelVO[]>([])

// 内部可编辑数据
const editableFilesData = ref<KnowledgeFileUploadVO[]>([])

// 知识库文件基础信息（包含描述和关联的嵌入模型）
const fileBaseInfoMap = ref<Record<string, KnowledgeFileVO>>({})

// 分组后的模型列表
const groupedModels = computed(() => {
  const groups: Record<string, ModelVO[]> = {}
  for (const model of embeddingModels.value) {
    const platform = model.platformName || t('common.platform.unknown')
    if (!groups[platform]) {
      groups[platform] = []
    }
    groups[platform].push(model)
  }
  return Object.keys(groups).map((platform) => ({
    platformName: platform,
    models: groups[platform] || [],
  }))
})

// 获取文件基础信息（描述和关联的嵌入模型）
const fetchFileBaseInfo = async () => {
  if (!props.knowledgeId) return
  try {
    const res = await getKnowledgeFileList(props.knowledgeId)
    if (res.errorCode === '00000' && res.data) {
      const map: Record<string, KnowledgeFileVO> = {}
      res.data.forEach((item) => {
        map[String(item.id)] = item
      })
      fileBaseInfoMap.value = map

      // 合并基础信息到当前文件数据
      editableFilesData.value.forEach((file) => {
        if (!file) return
        const baseInfo = map[String(file.id)]
        if (!baseInfo) return

        if (baseInfo.description) {
          file.description = baseInfo.description
        }
        if (baseInfo.relEmbeddingModel && baseInfo.relEmbeddingModel.id) {
          file.embeddingModelId = baseInfo.relEmbeddingModel.id
        }
      })
    }
  } catch (error) {
    console.error('获取文件基础信息失败:', error)
  }
}

// 获取嵌入模型列表
const fetchEmbeddingModels = async () => {
  if (embeddingModels.value.length > 0) {
    setDefaultModel()
    return
  }
  loadingModels.value = true
  try {
    const res = await getModelList('EMBEDDING')
    if (res.errorCode === '00000') {
      embeddingModels.value = res.data.filter((m) => m.enabled)
      if (embeddingModels.value.length > 0) {
        setDefaultModel()
      } else {
        // 无可用模型，提示并跳转
        ElMessageBox.confirm(
          t('knowledge.message.no_embedding_model'),
          t('common.prompt'),
          {
            confirmButtonText: t('chat.area.go_to_config'),
            cancelButtonText: t('common.cancel'),
            type: 'warning',
          },
        ).then(() => {
          handleClose()
          router.push('/quick_answer')
        })
      }
    }
  } catch (error) {
    console.error('Failed to fetch embedding models:', error)
    ElMessage.error(t('knowledge.message.fetch_embedding_failed'))
  } finally {
    loadingModels.value = false
  }
}

// 设置默认模型
const setDefaultModel = () => {
  const models = embeddingModels.value
  const firstModel = models[0]
  if (firstModel) {
    const firstModelId = firstModel.id
    editableFilesData.value.forEach((file) => {
      if (file && !file.embeddingModelId) {
        file.embeddingModelId = firstModelId
      }
    })
  }
}

// 监听弹框显示状态
watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      activeFileIndex.value = '0'
      failedFileIds.value = []
      // 如果文件数据为空，显示加载状态
      loading.value = props.filesData.length === 0
      // 深拷贝文件数据用于编辑
      editableFilesData.value = JSON.parse(JSON.stringify(props.filesData))
      // 获取文件基础信息（描述与嵌入模型）
      fetchFileBaseInfo()
      // 获取模型列表
      fetchEmbeddingModels()
    }
  },
)

// 监听 filesData 变化，当数据加载完成后关闭 loading
watch(
  () => props.filesData,
  (val) => {
    if (val && val.length > 0) {
      loading.value = false
      editableFilesData.value = JSON.parse(JSON.stringify(val))
      if (embeddingModels.value.length > 0) {
        setDefaultModel()
      }
    }
  },
)

// 监听 visible 变化，同步到父组件
watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 新增内容
const handleAddContent = (fileIndex: number) => {
  editableFilesData.value[fileIndex]?.details.push({
    content: '',
    metaData: {},
  })
}

// 删除内容
const handleDeleteContent = (fileIndex: number, detailIndex: number) => {
  ElMessageBox.confirm('确定要删除这条内容吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      editableFilesData.value[fileIndex]?.details.splice(detailIndex, 1)
      ElMessage.success('删除成功')
    })
    .catch(() => {})
}

// 保存
const handleSave = async () => {
  // 验证
  if (!editableFilesData.value || editableFilesData.value.length === 0) {
    ElMessage.error('文件数据为空')
    return
  }

  // 检查每个文件
  for (let i = 0; i < editableFilesData.value.length; i++) {
    const file = editableFilesData.value[i]
    if (!file) continue

    if (file.details.length === 0) {
      ElMessage.error(`文件「${props.filesData[i]?.fileName || ''}」至少需要一条内容`)
      return
    }

    if (!file.embeddingModelId) {
      ElMessage.error(`请为文件「${props.filesData[i]?.fileName || ''}」选择嵌入模型`)
      return
    }

    const hasEmptyContent = file.details.some((item) => !item.content.trim())
    if (hasEmptyContent) {
      ElMessage.error(`文件「${props.filesData[i]?.fileName || ''}」存在空内容`)
      return
    }
  }

  saving.value = true

  try {
    const requests: KnowledgeFileAddRequest[] = editableFilesData.value.map((file) => ({
      id: file.id,
      description: file.description || '',
      embeddingModelId: file.embeddingModelId,
      details: file.details.map((item) => ({
        id: item.id,
        content: item.content,
        metaData: item.metaData,
      })),
    }))

    const response = await addKnowledgeFile(props.knowledgeId, requests)
    if (response.errorCode === '00000') {
      // 检查是否有失败的文件
      if (response.data && response.data.length > 0) {
        failedFileIds.value = response.data
        ElMessage.warning(response.userTip || `有 ${response.data.length} 个文件保存失败，请检查`)
      } else {
        ElMessage.success(response.userTip || '保存成功')
        emit('success')
        handleClose()
      }
    }
  } catch {
    // 空 catch
  } finally {
    saving.value = false
  }
}

// 关闭弹框
const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.file-content-editor {
  .content-list {
    margin-top: 20px;

    .list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 500;
      }
    }

    .metadata-display {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;

      .empty-text {
        color: #999;
        font-size: 12px;
      }
    }
  }

  .model-option {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .model-tag {
      font-size: 12px;
      color: #999;
      margin-left: 8px;
    }
  }
}
</style>
