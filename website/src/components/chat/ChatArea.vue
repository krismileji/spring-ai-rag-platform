<template>
  <div class="chat-area">
    <div class="chat-header">
      <div class="header-left">
        <el-select v-model="selectedModel" :placeholder="t('chat.area.select_model')" style="width: 260px" :loading="modelLoading">
          <el-option-group v-for="platform in platformGroups" :key="platform.platform" :label="platform.platformName">
            <el-option v-for="item in platform.models" :key="item.model" :label="item.modelName" :value="item.model">
              <el-tooltip :content="item.description || t('chat.area.no_description')" placement="right" :disabled="!item.description"
                effect="light" :show-after="300" popper-class="model-description-tooltip">
                <span style="display: block; width: 100%">{{ item.modelName }}</span>
              </el-tooltip>
            </el-option>
          </el-option-group>
        </el-select>
      </div>
      <div class="header-right">
        <div class="knowledge-type-selector">
          <el-radio-group v-model="knowledgeType" size="small">
            <el-radio value="">{{ t('chat.knowledge.none') }}</el-radio>
            <el-radio value="LOCAL">{{ t('chat.knowledge.local') }}</el-radio>
            <!-- <el-radio value="ALI_BAI_LIAN">云端知识库</el-radio> -->
          </el-radio-group>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="chatStore.isLoading" class="empty-container">
      <div class="loading-state">
        <el-icon class="is-loading" :size="48">
          <Loading />
        </el-icon>
        <div class="loading-text">{{ t('common.loading') }}</div>
      </div>
    </div>

    <!-- 没有会话时的空状态 -->
    <div v-else-if="!chatStore.currentSession" class="empty-container">
      <div class="empty-state">
        <div class="empty-icon">👋</div>
        <div class="empty-text">{{ t('chat.welcome.title') }}</div>
        <div class="empty-hint">{{ t('chat.welcome.hint') }}</div>
      </div>
    </div>

    <!-- 有会话时的正常布局 -->
    <template v-else>
      <div ref="messagesContainer" class="chat-messages">
        <div v-if="chatStore.currentMessages.length === 0" class="empty-state">
          <div class="empty-icon">💬</div>
          <div class="empty-text">{{ t('chat.empty.title') }}</div>
          <div class="empty-hint">{{ t('chat.empty.hint') }}</div>
        </div>

        <MessageBubble v-for="message in chatStore.currentMessages" :key="message.id" :message="message" />
      </div>

      <MessageInput />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted } from 'vue'
import { useChatStore } from '../../stores/chat'
import MessageBubble from './MessageBubble.vue'
import MessageInput from './MessageInput.vue'
import { getModelList, type ModelVO } from '../../api/model'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const chatStore = useChatStore()
const router = useRouter()
const messagesContainer = ref<HTMLElement>()
const selectedModel = ref('')
const modelList = ref<ModelVO[]>([])
const modelLoading = ref(false)
const knowledgeType = ref('')

interface PlatformGroup {
  platform: string
  platformName: string
  models: ModelVO[]
}

const platformGroups = ref<PlatformGroup[]>([])

// 监听模型选择变化，同步到 store
watch(selectedModel, (newModel) => {
  if (newModel) {
    const modelInfo = modelList.value.find((m) => m.model === newModel)
    if (modelInfo) {
      chatStore.setSelectedModel(modelInfo.platform, modelInfo.model)
    }
  }
})

// 监听知识库类型变化，同步到 store
watch(knowledgeType, (newType) => {
  chatStore.setKnowledgeType(newType)
})

// 获取模型列表
const fetchModelList = async () => {
  modelLoading.value = true
  try {
    const response = await getModelList('CHAT')
    if (response.errorCode !== '00000') {
      ElMessage.error(response.errorMessage || t('chat.area.fetch_model_failed'))
      return
    }
    modelList.value = response.data || []

    // 按平台分组
    const platformMap = new Map<string, { platformName: string; models: ModelVO[] }>()
    // 只展示 enabled 为 true 的模型
    modelList.value.filter((model) => model.enabled === true).forEach((model) => {
      if (!platformMap.has(model.platform)) {
        platformMap.set(model.platform, {
          platformName: model.platformName,
          models: [],
        })
      }
      platformMap.get(model.platform)?.models.push(model)
    })

    // 转换为分组数组
    platformGroups.value = Array.from(platformMap.entries()).map(([platform, data]) => ({
      platform,
      platformName: data.platformName,
      models: data.models,
    }))

    // 默认选中第一个启用的模型
    const enabledModels = modelList.value.filter((model) => model.enabled === true)
    if (enabledModels.length > 0 && !selectedModel.value) {
      selectedModel.value = enabledModels[0]?.model || ''
      // 同步到 store
      if (enabledModels[0]) {
        chatStore.setSelectedModel(enabledModels[0].platform, enabledModels[0].model)
      }
    } else if (enabledModels.length === 0) {
      // 没有启用的模型,提示用户配置
      ElMessageBox.confirm(
        t('chat.area.no_model_available'),
        t('common.prompt'),
        {
          confirmButtonText: t('chat.area.go_to_config'),
          cancelButtonText: t('common.cancel'),
          type: 'warning',
        },
      )
        .then(() => {
          router.push({ path: '/settings', query: { tab: 'ai' } })
        })
        .catch(() => {
          // 用户取消,不做处理
        })
    }
  } catch (error) {
    ElMessage.error(t('chat.area.fetch_model_failed'))
    console.error('获取模型列表失败:', error)
  } finally {
    modelLoading.value = false
  }
}

// 自动滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 监听消息变化
watch(
  () => chatStore.currentMessages,
  () => {
    scrollToBottom()
  },
  { deep: true },
)

// 监听会话切换
watch(
  () => chatStore.currentSessionId,
  () => {
    scrollToBottom()
  },
)

// 组件挂载时获取模型列表
onMounted(() => {
  fetchModelList()
})
</script>

<style scoped>
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--app-bg-color);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  box-sizing: border-box;
  padding: 0 24px;
  border-bottom: 1px solid var(--app-border-color);
  background: var(--app-bg-color-secondary);
  box-shadow: var(--app-shadow-sm);
  z-index: 10;
}

.header-left {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
}

.session-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--app-text-color-primary);
  margin: 0;
}

.knowledge-type-selector {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  background: var(--app-bg-color);
  border-radius: 6px;
  border: 1px solid var(--app-border-color);
}

.knowledge-type-selector :deep(.el-radio) {
  margin-right: 20px;
}

.knowledge-type-selector :deep(.el-radio:last-child) {
  margin-right: 0;
}

.knowledge-type-selector :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: var(--app-primary-color);
  border-color: var(--app-primary-color);
  box-shadow: 0 0 0 2px var(--app-primary-light);
}

.knowledge-type-selector :deep(.el-radio__inner) {
  width: 16px;
  height: 16px;
  border: 2px solid var(--app-text-color-disabled);
  transition: all 0.2s;
  background: transparent;
}

.knowledge-type-selector :deep(.el-radio__inner:hover) {
  border-color: var(--app-primary-color);
}

.knowledge-type-selector :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: var(--app-text-color-primary);
  font-weight: 500;
}

.knowledge-type-selector :deep(.el-radio__label) {
  font-size: 13px;
  color: var(--app-text-color-secondary);
  padding-left: 8px;
  transition: color 0.2s;
}

.knowledge-type-selector :deep(.el-radio:hover .el-radio__label) {
  color: var(--app-primary-color);
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: var(--app-bg-color);
  transition: background-color 0.3s;
}

.empty-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-bg-color);
  transition: background-color 0.3s;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--app-text-color-disabled);
}

.chat-messages .empty-state {
  height: 100%;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--app-text-color-primary);
}

.empty-hint {
  font-size: 14px;
  color: var(--app-text-color-secondary);
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--app-primary-color);
}

.loading-text {
  margin-top: 16px;
  font-size: 16px;
  color: var(--app-text-color-secondary);
}

::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-thumb {
  background: var(--app-border-color);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--app-text-color-disabled);
}
</style>

<style>
/* 模型描述 Tooltip 样式（全局样式，不受 scoped 限制）*/
.model-description-tooltip {
  max-width: 320px !important;
  padding: 10px 14px !important;
  font-size: 13px !important;
  line-height: 1.6 !important;
  color: var(--app-text-color-primary) !important;
  background: var(--app-card-bg-color) !important;
  border: 1px solid var(--app-border-color) !important;
  border-radius: 8px !important;
  box-shadow: var(--app-shadow-lg) !important;
}

.model-description-tooltip.is-light {
  background: var(--app-card-bg-color) !important;
  border-color: var(--app-border-color) !important;
}
</style>
