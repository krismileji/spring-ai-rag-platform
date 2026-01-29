<template>
  <div class="kb-detail-view">
    <div class="detail-header">
      <div class="header-left">
        <el-icon :size="24"><Folder /></el-icon>
        <h2>{{ knowledgeBase.name }}</h2>
      </div>
      <div class="header-actions">
        <el-input
          :model-value="searchQuery"
          placeholder="搜索文件..."
          :prefix-icon="Search"
          clearable
          style="width: 300px"
          @update:model-value="$emit('update:searchQuery', $event)"
        />
        <el-button type="primary" :icon="Upload" @click="handleUploadFile"> 上传文件 </el-button>
      </div>
    </div>

    <!-- 文件列表 -->
    <FileList
      :files="filteredFiles"
      @upload="handleUploadFile"
      @view="$emit('view-file', $event)"
      @delete="handleDeleteFile"
    />

    <!-- 文件上传 -->
    <FileUpload
      ref="fileUploadRef"
      :knowledge-id="knowledgeBase.id"
      @upload="handleFilesUpload"
      @success="handleUploadSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, Search, Upload } from '@element-plus/icons-vue'
import FileList from './FileList.vue'
import FileUpload from './FileUpload.vue'
import { getKnowledgeFileList, deleteKnowledgeFile } from '@/api/knowledge-api'
import type { KnowledgeFileVO } from '@/api/model'

interface FileItem {
  id: string
  name: string
  createTime: string
}

interface KnowledgeBase {
  id: string
  name: string
  description?: string
  files?: FileItem[]
  createTime: string
}

const props = defineProps<{
  knowledgeBase: KnowledgeBase
  searchQuery: string
  filteredFiles: FileItem[]
}>()

const emit = defineEmits<{
  (e: 'update:searchQuery', value: string): void
  (e: 'view-file', file: FileItem): void
  (e: 'delete-file', file: FileItem): void
  (e: 'update:files', files: FileItem[]): void
}>()

// 文件上传
const fileUploadRef = ref<InstanceType<typeof FileUpload>>()

// 加载文件列表
const loadFileList = async () => {
  try {
    const response = await getKnowledgeFileList(props.knowledgeBase.id)
    if (response.data && response.data.length > 0) {
      const fileItems: FileItem[] = response.data.map((file: KnowledgeFileVO) => ({
        id: file.id,
        name: file.fileName,
        createTime: file.createTime,
      }))
      emit('update:files', fileItems)
    } else {
      emit('update:files', [])
    }
  } catch (error) {
    console.error('加载文件列表失败:', error)
    if (error && typeof error === 'object' && 'userTip' in error) {
      ElMessage.error((error as { userTip: string }).userTip || '加载文件列表失败')
    } else {
      ElMessage.error('加载文件列表失败')
    }
  }
}

// 监听知识库变化，加载文件列表
watch(
  () => props.knowledgeBase.id,
  () => {
    loadFileList()
  },
  { immediate: true },
)

const handleUploadFile = () => {
  fileUploadRef.value?.triggerUpload()
}

const handleFilesUpload = () => {
  // 不在前端自动添加文件到列表,等待上传成功后通过 loadFileList 刷新
}

const handleUploadSuccess = async () => {
  // 文件保存成功后刷新列表
  await loadFileList()
}

const handleDeleteFile = async (file: FileItem) => {
  try {
    await ElMessageBox.confirm(`是否删除该文件?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const response = await deleteKnowledgeFile(file.id)
    if (response.errorCode === '00000') {
      ElMessage.success(response.userTip || '删除成功')
      await loadFileList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文件失败:', error)
      if (error && typeof error === 'object' && 'userTip' in error) {
        ElMessage.error((error as { userTip: string }).userTip || '删除文件失败')
      }
    }
  }
}
</script>

<style scoped>
.kb-detail-view {
  max-width: 1400px;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: var(--app-bg-color-secondary);
  border-radius: 12px;
  box-shadow: var(--app-shadow-sm);
  transition: background-color 0.3s, box-shadow 0.3s;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--app-text-color-primary);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
  }

  .header-actions .el-input {
    width: 100% !important;
  }
}
</style>
