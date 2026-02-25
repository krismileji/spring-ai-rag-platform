<template>
  <div class="model-list-container">
    <el-tabs v-model="activeTab" class="model-tabs">
      <!-- 聊天模型标签页 -->
      <el-tab-pane :label="t('settings.ai.chat_model')" name="chat">
        <div class="tab-content">
          <div class="model-list-header">
            <h3>{{ t('settings.ai.chat_model_list') }}</h3>
            <div class="model-actions">
              <el-tooltip
                :content="t('common.refresh')"
                placement="top"
              >
                <el-button
                  :icon="Refresh"
                  circle
                  :loading="chatLoading"
                  @click="handleRefresh"
                />
              </el-tooltip>
              <el-input
                v-model="chatSearchQuery"
                :placeholder="t('settings.ai.search_model_placeholder')"
                clearable
                :prefix-icon="Search"
                class="model-search"
              />
            </div>
          </div>

          <div v-if="chatLoading" class="model-list-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>{{ t('common.loading') }}</span>
          </div>

          <div v-else-if="filteredChatModels.length === 0" class="model-list-empty">
            <el-icon><Box /></el-icon>
            <span>{{ chatSearchQuery ? t('settings.ai.no_model_found') : t('settings.ai.no_model_available') }}</span>
          </div>

          <div v-else class="model-list-content">
            <div v-for="model in filteredChatModels" :key="model.model" class="model-item">
              <div class="model-info">
                <span class="model-name">{{ model.modelName }}</span>
                <span class="model-id">{{ model.model }}</span>
              </div>
              <el-switch
                :model-value="isChatModelEnabled(model.model)"
                @update:model-value="toggleChatModel(model.model, $event)"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 嵌入模型标签页 -->
      <el-tab-pane :label="t('settings.ai.embedding_model')" name="embedding">
        <div class="tab-content">
          <div class="model-list-header">
            <h3>{{ t('settings.ai.embedding_model_list') }}</h3>
            <div class="model-actions">
              <el-tooltip
                :content="t('common.refresh')"
                placement="top"
              >
                <el-button
                  :icon="Refresh"
                  circle
                  :loading="embeddingLoading"
                  @click="handleRefresh"
                />
              </el-tooltip>
              <el-input
                v-model="embeddingSearchQuery"
                :placeholder="t('settings.ai.search_model_placeholder')"
                clearable
                :prefix-icon="Search"
                class="model-search"
              />
            </div>
          </div>

          <div v-if="embeddingLoading" class="model-list-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>{{ t('common.loading') }}</span>
          </div>

          <div v-else-if="filteredEmbeddingModels.length === 0" class="model-list-empty">
            <el-icon><Box /></el-icon>
            <span>{{ embeddingSearchQuery ? t('settings.ai.no_model_found') : t('settings.ai.no_model_available') }}</span>
          </div>

          <div v-else class="model-list-content">
            <div v-for="model in filteredEmbeddingModels" :key="model.model" class="model-item">
              <div class="model-info">
                <span class="model-name">{{ model.modelName }}</span>
                <span class="model-id">{{ model.model }}</span>
              </div>
              <el-switch
                :model-value="isEmbeddingModelEnabled(model.model)"
                @update:model-value="toggleEmbeddingModel(model.model, $event)"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Search, Loading, Box, Refresh } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import {
  getModelList,
  editModel,
  refreshModels,
  type ModelVO,
} from '../../api/chat-api'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

interface Props {
  provider: string
  providerEnabled: boolean
}

const props = defineProps<Props>()

const activeTab = ref('chat')

// 聊天模型相关
const chatSearchQuery = ref('')
const chatLoading = ref(false)
const chatModels = ref<ModelVO[]>([])
const enabledChatModels = ref<Set<string>>(new Set())

// 嵌入模型相关
const embeddingSearchQuery = ref('')
const embeddingLoading = ref(false)
const embeddingModels = ref<ModelVO[]>([])
const enabledEmbeddingModels = ref<Set<string>>(new Set())

const filteredChatModels = computed(() => {
  if (!chatSearchQuery.value) {
    return chatModels.value
  }
  const query = chatSearchQuery.value.toLowerCase()
  return chatModels.value.filter(
    (model) =>
      model.modelName.toLowerCase().includes(query) || model.model.toLowerCase().includes(query),
  )
})

const filteredEmbeddingModels = computed(() => {
  if (!embeddingSearchQuery.value) {
    return embeddingModels.value
  }
  const query = embeddingSearchQuery.value.toLowerCase()
  return embeddingModels.value.filter(
    (model) =>
      model.modelName.toLowerCase().includes(query) || model.model.toLowerCase().includes(query),
  )
})

const isChatModelEnabled = (modelId: string) => {
  return enabledChatModels.value.has(modelId)
}

const isEmbeddingModelEnabled = (modelId: string) => {
  return enabledEmbeddingModels.value.has(modelId)
}

const toggleChatModel = async (modelId: string, enabled: boolean) => {
  const model = chatModels.value.find((m) => m.model === modelId)
  if (!model) {
    ElMessage.error('模型不存在')
    return
  }

  try {
    await editModel('CHAT', {
      id: model.id,
      model: modelId,
      enabled,
    })

    if (enabled) {
      enabledChatModels.value.add(modelId)
    } else {
      enabledChatModels.value.delete(modelId)
    }
    enabledChatModels.value = new Set(enabledChatModels.value)
    ElMessage.success(enabled ? '模型已启用' : '模型已禁用')
  } catch (error) {
    console.error('[AIModelList] 切换聊天模型状态失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

const toggleEmbeddingModel = async (modelId: string, enabled: boolean) => {
  const model = embeddingModels.value.find((m) => m.model === modelId)
  if (!model) {
    ElMessage.error('模型不存在')
    return
  }

  try {
    await editModel('EMBEDDING', {
      id: model.id,
      model: modelId,
      enabled,
    })

    if (enabled) {
      enabledEmbeddingModels.value.add(modelId)
    } else {
      enabledEmbeddingModels.value.delete(modelId)
    }
    enabledEmbeddingModels.value = new Set(enabledEmbeddingModels.value)
    ElMessage.success(enabled ? '模型已启用' : '模型已禁用')
  } catch (error) {
    console.error('[AIModelList] 切换嵌入模型状态失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

const handleRefresh = async () => {
  const type = activeTab.value === 'chat' ? 'CHAT' : 'EMBEDDING'
  const loadingRef = activeTab.value === 'chat' ? chatLoading : embeddingLoading

  loadingRef.value = true
  try {
    const response = await refreshModels(type, { platform: props.provider })
    ElMessage.success(t('common.refresh_success'))

    if (response.data) {
      if (type === 'CHAT') {
        chatModels.value = response.data
        enabledChatModels.value = new Set(
          chatModels.value.filter((m) => m.enabled !== false).map((m) => m.model),
        )
      } else {
        embeddingModels.value = response.data
        enabledEmbeddingModels.value = new Set(
          embeddingModels.value.filter((m) => m.enabled !== false).map((m) => m.model),
        )
      }
    }
  } catch (error) {
    console.error('[AIModelList] 刷新模型列表失败:', error)
    ElMessage.error(t('common.refresh_failed'))
  } finally {
    loadingRef.value = false
  }
}

const loadChatModels = async () => {
  console.log(
    '[AIModelList] loadChatModels called, provider:',
    props.provider,
    'enabled:',
    props.providerEnabled,
  )

  chatLoading.value = true
  try {
    console.log('[AIModelList] Fetching chat models for platform:', props.provider)
    const response = await getModelList('CHAT', { platform: props.provider })
    console.log('[AIModelList] Chat models API response:', response)

    if (response.data) {
      chatModels.value = response.data
      console.log('[AIModelList] Chat models loaded:', chatModels.value.length, 'items')
      enabledChatModels.value = new Set(
        chatModels.value.filter((m) => m.enabled !== false).map((m) => m.model),
      )
    } else {
      console.warn('[AIModelList] No data in chat models response')
      chatModels.value = []
    }
  } catch (error) {
    console.error('[AIModelList] 加载聊天模型列表失败:', error)
    chatModels.value = []
  } finally {
    chatLoading.value = false
  }
}

const loadEmbeddingModels = async () => {
  console.log(
    '[AIModelList] loadEmbeddingModels called, provider:',
    props.provider,
    'enabled:',
    props.providerEnabled,
  )

  embeddingLoading.value = true
  try {
    console.log('[AIModelList] Fetching embedding models for platform:', props.provider)
    const response = await getModelList('EMBEDDING', { platform: props.provider })
    console.log('[AIModelList] Embedding models API response:', response)

    if (response.data) {
      embeddingModels.value = response.data
      console.log('[AIModelList] Embedding models loaded:', embeddingModels.value.length, 'items')
      enabledEmbeddingModels.value = new Set(
        embeddingModels.value.filter((m) => m.enabled !== false).map((m) => m.model),
      )
    } else {
      console.warn('[AIModelList] No data in embedding models response')
      embeddingModels.value = []
    }
  } catch (error) {
    console.error('[AIModelList] 加载嵌入模型列表失败:', error)
    embeddingModels.value = []
  } finally {
    embeddingLoading.value = false
  }
}

const loadCurrentTabModels = async () => {
  if (activeTab.value === 'chat') {
    await loadChatModels()
  } else if (activeTab.value === 'embedding') {
    await loadEmbeddingModels()
  }
}

watch(
  () => activeTab.value,
  () => {
    loadCurrentTabModels()
  },
)

watch(
  () => [props.provider, props.providerEnabled],
  () => {
    chatSearchQuery.value = ''
    embeddingSearchQuery.value = ''
    // 切换服务商时清空所有数据
    chatModels.value = []
    embeddingModels.value = []
    loadCurrentTabModels()
  },
  { immediate: true },
)
</script>

<style scoped>
.model-list-container {
  background: var(--app-card-bg-color);
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--app-shadow-sm);
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 300px;
}

.model-tabs {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.model-tabs :deep(.el-tabs__content) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.model-tabs :deep(.el-tab-pane) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tab-content {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.model-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 16px;
}

.model-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.model-list-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text-color-primary);
  white-space: nowrap;
}

.model-search {
  width: 240px;
}

.model-list-loading,
.model-list-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--app-text-color-secondary);
  font-size: 14px;
}

.model-list-loading .el-icon,
.model-list-empty .el-icon {
  font-size: 32px;
}

.model-list-content {
  flex: 1;
  overflow-y: auto;
}

.model-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.model-item:hover {
  background: var(--app-sidebar-item-hover-bg);
}

.model-item:not(:last-child) {
  border-bottom: 1px solid var(--app-border-color-light);
}

.model-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
  flex: 1;
  margin-right: 16px;
}

.model-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-id {
  font-size: 12px;
  color: var(--app-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .model-list-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .model-search {
    width: 100%;
  }
}
</style>
