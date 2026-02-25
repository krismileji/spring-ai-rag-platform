<template>
  <div class="content-section">
    <div class="section-header">
      <h1>{{ t('settings.ai.title') }}</h1>
      <p class="section-desc">{{ t('settings.ai.description') }}</p>
    </div>

    <div class="provider-layout">
      <!-- 左侧：服务商列表 -->
      <div class="provider-sidebar">
        <AIProviderCard
          v-for="provider in providers"
          :key="provider.id"
          :provider="provider"
          :is-selected="selectedProvider === provider.id"
          @select="handleSelectProvider"
          @toggle="handleToggleProvider"
        />
      </div>

      <!-- 右侧：配置区 -->
      <div class="provider-main">
        <!-- 上部：API Key配置 -->
        <AIProviderConfigForm
          v-if="selectedProvider && currentProvider"
          ref="configFormRef"
          :provider-name="currentProvider.name"
          :settings="settings"
          @update:settings="handleUpdateSettings"
          @save="handleSave"
          @test="handleTest"
        />

        <!-- 下部：模型列表 -->
        <AIModelList
          v-if="selectedProvider && currentProvider"
          :provider="selectedProvider"
          :provider-enabled="currentProvider.enabled"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AIProviderCard from './AIProviderCard.vue'
import AIProviderConfigForm from './AIProviderConfigForm.vue'
import AIModelList from './AIModelList.vue'
import { editChatPlatform } from '../../api/platform-api'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

export interface Provider {
  id: string
  name: string
  enabled: boolean
  color: string
  existApiKey: boolean
}

interface AiSettings {
  apiKey: string
  apiUrl: string
  defaultModel: string
  timeout?: number
  temperature?: number
  maxTokens: number
  enableSearch: boolean
  enableThinking: boolean
}

interface Props {
  providers: Provider[]
  selectedProvider: string
  settings: AiSettings
}

interface Emits {
  (e: 'update:providers', value: Provider[]): void
  (e: 'update:selectedProvider', value: string): void
  (e: 'update:settings', value: AiSettings): void
  (e: 'save'): void
  (e: 'test'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const configFormRef = ref<InstanceType<typeof AIProviderConfigForm>>()

const currentProvider = computed(() => props.providers.find((p) => p.id === props.selectedProvider))

const handleSelectProvider = (id: string) => {
  emit('update:selectedProvider', id)
}

const handleToggleProvider = async (id: string, enabled: boolean) => {
  const platformMap: Record<string, 'ollama' | 'deepseek' | 'aliyun'> = {
    ollama: 'ollama',
    deepseek: 'deepseek',
    aliyun: 'aliyun',
  }

  const platform = platformMap[id]
  if (!platform) {
    ElMessage.error(t('settings.message.platform_unsupported'))
    return
  }

  // 如果是启用操作，检查是否存在apiKey
  if (enabled) {
    const provider = props.providers.find((p) => p.id === id)
    if (provider && !provider.existApiKey) {
      ElMessage.warning(t('settings.message.configure_api_key_first'))
      emit('update:selectedProvider', id)
      // 触发表单校验
      setTimeout(() => {
        configFormRef.value?.validateField('apiKey')
      }, 100)
      return
    }
  }

  try {
    const response = await editChatPlatform({
      platform,
      enabled,
    })

    if (response.errorCode === '00000') {
      const updatedProviders = props.providers.map((p) => (p.id === id ? { ...p, enabled } : p))
      emit('update:providers', updatedProviders)
      ElMessage.success(enabled ? t('settings.message.enabled') : t('settings.message.disabled'))
    } else {
      ElMessage.error(response.userTip || t('common.error_operation_failed'))
    }
  } catch (error) {
    console.error('切换状态失败:', error)
    ElMessage.error(t('common.error_operation_failed_retry'))
  }
}

const handleUpdateSettings = (value: AiSettings) => {
  emit('update:settings', value)
}

const handleSave = () => {
  emit('save')
}

const handleTest = () => {
  emit('test')
}


</script>

<style scoped>
.content-section {
  max-width: 1200px;
}

.section-header {
  margin-bottom: 24px;
  padding: 20px 24px;
  background: var(--app-bg-color-secondary);
  border-radius: 12px;
  box-shadow: var(--app-shadow-sm);
}

.section-header h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: var(--app-text-color-primary);
}

.section-desc {
  margin: 0;
  color: var(--app-text-color-secondary);
  font-size: 14px;
}

.provider-layout {
  display: flex;
  gap: 24px;
  min-height: calc(100vh - 220px);
}

.provider-sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.provider-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-width: 0;
}

@media (max-width: 900px) {
  .provider-layout {
    flex-direction: column;
    min-height: auto;
  }

  .provider-sidebar {
    width: 100%;
    flex-direction: row;
    overflow-x: auto;
    gap: 12px;
    padding-bottom: 8px;
  }

  .provider-sidebar > :deep(.provider-card) {
    min-width: 200px;
    flex-shrink: 0;
  }

  .provider-main {
    width: 100%;
  }
}
</style>
