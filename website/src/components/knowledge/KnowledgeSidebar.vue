<template>
  <div class="kb-sidebar">
    <div class="kb-header">
      <div class="kb-title">
        <h2>{{ t('knowledge.sidebar.title') }}</h2>
        <p class="kb-subtitle">{{ t('knowledge.sidebar.subtitle') }}</p>
      </div>
    </div>

    <div class="kb-nav">
      <div class="nav-section">
        <div
          class="nav-item"
          :class="{ active: currentView === 'home' }"
          @click="$emit('view-change', 'home')"
        >
          <el-icon><HomeFilled /></el-icon>
          <span>{{ t('knowledge.sidebar.home') }}</span>
        </div>
      </div>

      <div class="nav-section">
        <div class="section-header">
          <span>{{ t('knowledge.sidebar.title') }}</span>
          <el-button
            text
            :icon="Plus"
            size="small"
            @click="$emit('create-kb')"
          />
        </div>
        <div class="kb-list">
          <div
            v-for="kb in knowledgeBases"
            :key="kb.id"
            class="kb-item"
            :class="{ active: selectedKbId === kb.id }"
            @click="$emit('select-kb', kb.id)"
          >
            <div class="kb-item-content">
              <el-icon class="kb-icon"><Folder /></el-icon>
              <span class="kb-name">{{ kb.name }}</span>
            </div>
            <el-dropdown trigger="click" @command="handleKbAction">
              <el-icon class="more-icon" @click.stop><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{ action: 'edit', id: kb.id }">
                    {{ t('common.edit') }}
                  </el-dropdown-item>
                  <el-dropdown-item
                    :command="{ action: 'delete', id: kb.id }"
                    divided
                  >
                    {{ t('common.delete') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { HomeFilled, Plus, Folder, MoreFilled } from '@element-plus/icons-vue'
import { getKnowledgeList, deleteKnowledge, type KnowledgeVO } from '../../api/model'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

interface FileItem {
  id: string
  name: string
  size: string
  createTime: string
}

interface KnowledgeBase {
  id: string
  name: string
  description?: string
  files?: FileItem[]
  createTime: string
}

defineProps<{
  selectedKbId?: string
  currentView: 'home' | 'kb'
}>()

const emit = defineEmits<{
  (e: 'view-change', view: 'home' | 'kb'): void
  (e: 'select-kb', id: string): void
  (e: 'create-kb'): void
  (e: 'edit-kb', kb: KnowledgeBase): void
  (e: 'delete-kb', id: string): void
  (e: 'update-list', list: KnowledgeBase[]): void
}>()

// 知识库列表
const knowledgeBases = ref<KnowledgeBase[]>([])

// 加载知识库列表
const loadKnowledgeBases = async () => {
  // 检查是否已登录，未登录不调用接口
  const token = localStorage.getItem('token')
  if (!token) {
    return
  }

  try {
    const res = await getKnowledgeList()
    if (res.errorCode === '00000' && res.data) {
      knowledgeBases.value = res.data.map((kb: KnowledgeVO) => ({
        id: kb.id.toString(),
        name: kb.name,
        description: kb.description || '',
        files: [],
        createTime: new Date().toLocaleString(),
      }))
      emit('update-list', knowledgeBases.value)
    }
  } catch (error) {
    console.error('加载知识库列表失败:', error)
  }
}

// 处理知识库操作
const handleKbAction = async (command: { action: string; id: string }) => {
  const kb = knowledgeBases.value.find((k) => k.id === command.id)
  if (!kb) return

  if (command.action === 'edit') {
    emit('edit-kb', kb)
  } else if (command.action === 'delete') {
    try {
      await ElMessageBox.confirm(t('knowledge.message.delete_confirm'), t('common.prompt'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
      })
      const res = await deleteKnowledge(command.id)
      if (res.errorCode === '00000' && res.data) {
        emit('delete-kb', command.id)
        await loadKnowledgeBases()
        ElMessage.success(t('knowledge.message.delete_success'))
      } else {
        ElMessage.error(res.errorMessage || t('knowledge.message.delete_error'))
      }
    } catch (e) {
      if (e !== 'cancel') {
        console.error(e)
      }
    }
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadKnowledgeBases()
})

// 暴露刷新方法供父组件调用
defineExpose({
  loadKnowledgeBases,
})
</script>

<style scoped>
.kb-sidebar {
  width: var(--app-sidebar-width);
  flex-shrink: 0;
  background: var(--app-sidebar-bg-color);
  border-right: 1px solid var(--app-border-color);
  display: flex;
  flex-direction: column;
  height: 100vh;
  transition: background-color 0.3s, border-color 0.3s;
}

.kb-header {
  padding: 24px 20px;
  border-bottom: 1px solid var(--app-border-color);
  transition: border-color 0.3s;
}

.kb-title h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--app-text-color-primary);
  margin-bottom: 4px;
}

.kb-subtitle {
  font-size: 13px;
  color: var(--app-text-color-secondary);
  margin: 0;
}

.kb-nav {
  flex: 1;
  overflow-y: auto;
  padding: 16px 12px;
}

.nav-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 600;
  color: var(--app-text-color-secondary);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: var(--app-text-color-primary);
  transition: all 0.2s;
}

.nav-item:hover {
  background: var(--app-sidebar-item-hover-bg);
}

.nav-item.active {
  background: var(--app-primary-light);
  color: var(--app-primary-color);
  font-weight: 500;
}

.kb-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.kb-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--app-text-color-primary);
}

.kb-item:hover {
  background: var(--app-sidebar-item-hover-bg);
}

.kb-item.active {
  background: var(--app-primary-light);
  color: var(--app-primary-color);
}

.kb-item-content {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.kb-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.kb-name {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.more-icon {
  font-size: 16px;
  color: var(--app-text-color-secondary);
  opacity: 0;
  transition: opacity 0.2s;
}

.kb-item:hover .more-icon {
  opacity: 1;
}


@media (max-width: 1024px) {
  .kb-sidebar {
    width: 240px;
  }
}

@media (max-width: 768px) {
  .kb-sidebar {
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid var(--app-border-color);
  }
}
</style>
