<template>
  <div class="knowledge-base">
    <!-- 左侧导航区 -->
    <KnowledgeSidebar
      ref="sidebarRef"
      :selected-kb-id="selectedKbId"
      :current-view="currentView"
      @view-change="handleViewChange"
      @select-kb="selectKnowledgeBase"
      @create-kb="handleCreateKb"
      @edit-kb="handleEditKb"
      @delete-kb="handleDeleteKb"
      @update-list="handleUpdateList"
    />

    <!-- 右侧主内容区 -->
    <div class="kb-main">
      <!-- 首页视图 -->
      <KnowledgeHome v-if="currentView === 'home'" @create-kb="handleCreateKb" />

      <!-- 知识库详情视图 -->
      <KnowledgeDetail
        v-else-if="selectedKb"
        :knowledge-base="selectedKb"
        v-model:search-query="searchQuery"
        :filtered-files="filteredFiles"
        @view-file="handleViewFile"
        @delete-file="handleDeleteFile"
        @update:files="handleUpdateFiles"
      />
    </div>

    <!-- 创建知识库对话框 -->
    <KnowledgeDialog
      v-model="showCreateDialog"
      :editing-kb="editingKb as any"
      @saved="handleKbSaved"
    />

    <!-- 查看文件详情对话框 -->
    <FileContentDialog
      v-model="showFileDetailDialog"
      :files-data="fileDetailData"
      :knowledge-id="selectedKbId || ''"
      :readonly="true"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import KnowledgeSidebar from '../components/knowledge/KnowledgeSidebar.vue'
import KnowledgeHome from '../components/knowledge/KnowledgeHome.vue'
import KnowledgeDetail from '../components/knowledge/KnowledgeDetail.vue'
import KnowledgeDialog from '../components/knowledge/KnowledgeDialog.vue'
import FileContentDialog from '../components/knowledge/FileContentDialog.vue'
import { getFileDetail } from '@/api/knowledge-api'
import type { KnowledgeFileUploadVO } from '@/api/model'

interface KnowledgeBase {
  id: string
  name: string
  description?: string
  files?: FileItem[]
  createTime: string
}

interface FileItem {
  id: string
  name: string
  createTime: string
}

// 当前视图
const currentView = ref<'home' | 'kb'>('home')

// 知识库列表
const knowledgeBases = ref<KnowledgeBase[]>([])

// 选中的知识库
const selectedKbId = ref<string>()
const selectedKb = computed(() => knowledgeBases.value.find((kb) => kb.id === selectedKbId.value))

// 搜索
const searchQuery = ref('')
const filteredFiles = computed(() => {
  if (!selectedKb.value?.files) return []
  if (!searchQuery.value) return selectedKb.value.files
  return selectedKb.value.files.filter((file) =>
    file.name.toLowerCase().includes(searchQuery.value.toLowerCase()),
  )
})

// 创建/编辑知识库
const showCreateDialog = ref(false)
const editingKb = ref<KnowledgeBase | null>(null)

// 查看文件详情
const showFileDetailDialog = ref(false)
const fileDetailData = ref<KnowledgeFileUploadVO[]>([])

// Sidebar 引用
const sidebarRef = ref<InstanceType<typeof KnowledgeSidebar>>()

// 视图切换
const handleViewChange = (view: 'home' | 'kb') => {
  currentView.value = view
  // 切换到首页时，清空选中的知识库
  if (view === 'home') {
    selectedKbId.value = undefined
  }
}

// 选择知识库
const selectKnowledgeBase = (id: string) => {
  selectedKbId.value = id
  currentView.value = 'kb'
}

// 创建知识库
const handleCreateKb = () => {
  editingKb.value = null
  showCreateDialog.value = true
}

// 编辑知识库
const handleEditKb = (kb: KnowledgeBase) => {
  editingKb.value = kb
  showCreateDialog.value = true
}

// 删除知识库
const handleDeleteKb = (id: string) => {
  if (selectedKbId.value === id) {
    selectedKbId.value = undefined
    currentView.value = 'home'
  }
}

// 更新知识库列表
const handleUpdateList = (list: KnowledgeBase[]) => {
  knowledgeBases.value = list
}

// 知识库保存成功
const handleKbSaved = () => {
  sidebarRef.value?.loadKnowledgeBases()
  editingKb.value = null
}

// 查看文件
const handleViewFile = async (file: FileItem) => {
  try {
    const response = await getFileDetail(file.id)
    if (response.errorCode === '00000' && response.data) {
      fileDetailData.value = [
        {
          id: file.id,
          fileName: file.name,
          description: '',
          details: response.data,
        },
      ]
      showFileDetailDialog.value = true
    }
  } catch (error) {
    console.error('获取文件详情失败:', error)
  }
}

// 删除文件
const handleDeleteFile = (file: FileItem) => {
  if (!selectedKb.value?.files) return
  selectedKb.value.files = selectedKb.value.files.filter((f) => f.id !== file.id)
  ElMessage.success('删除成功')
}

// 更新文件列表
const handleUpdateFiles = (files: FileItem[]) => {
  if (selectedKb.value) {
    selectedKb.value.files = files
  }
}
</script>

<style scoped>
.knowledge-base {
  display: flex;
  height: 100vh;
  flex: 1;
  min-width: 0;
  background: var(--app-bg-color);
  overflow: hidden;
}

.kb-main {
  flex: 1;
  overflow-y: auto;
  padding: 32px 48px;
}

@media (max-width: 1024px) {
  .kb-main {
    padding: 24px 32px;
  }
}

@media (max-width: 768px) {
  .knowledge-base {
    flex-direction: column;
  }

  .kb-main {
    padding: 16px;
  }
}
</style>
