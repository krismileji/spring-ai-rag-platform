<template>
  <div class="settings-view">
    <SettingsSidebar :active-tab="activeTab" @update:active-tab="activeTab = $event" />

    <SettingsContent
      :active-tab="activeTab"
      :providers="providers"
      :selected-provider="selectedProvider"
      :ai-settings="aiSettings"
      :general-settings="generalSettings"
      @update:providers="providers = $event"
      @update:selected-provider="selectedProvider = $event"
      @update:ai-settings="aiSettings = $event"
      @update:general-settings="generalSettings = $event"
      @save-ai-settings="saveAiSettings"
      @save-general-settings="saveGeneralSettings"
      @test-connection="handleTestConnection"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import SettingsSidebar from '../components/settings/SettingsSidebar.vue'
import SettingsContent from '../components/settings/SettingsContent.vue'
import { useSettings, type AiSettings } from '../composables/useSettings'
import { useTheme } from '../composables/useTheme'
import type { ChatPlatformVO } from '../api/model'

// ==================== 类型定义 ====================
interface GeneralSettings {
  language: string
  theme: string
}

// ==================== 常量配置 ====================
const route = useRoute()
const DEFAULT_AI_SETTINGS: AiSettings = {
  apiKey: '',
  apiUrl: '',
  defaultModel: '',
  maxTokens: 4000,
  enableSearch: true,
  enableThinking: true,
}

const DEFAULT_GENERAL_SETTINGS: GeneralSettings = {
  language: 'zh-CN',
  theme: 'light',
}

// ==================== Composables ====================
const {
  providers,
  loadPlatformList,
  savePlatformSettings,
  testPlatformConnection,
  MASKED_API_KEY,
} = useSettings()

const { theme } = useTheme()

// ==================== 状态管理 ====================
const activeTab = ref((route.query.tab as string) || 'ai')
const selectedProvider = ref('ollama')
const aiSettings = ref<AiSettings>(JSON.parse(JSON.stringify(DEFAULT_AI_SETTINGS)))
const generalSettings = ref<GeneralSettings>({ ...DEFAULT_GENERAL_SETTINGS })
const platformsData = ref<ChatPlatformVO[]>([])

// ==================== 辅助函数 ====================
const updateAiSettingsForm = (providerId: string) => {
  const currentPlatformData = platformsData.value.find((p) => p.platform === providerId)

  if (currentPlatformData) {
    aiSettings.value = {
      apiKey: currentPlatformData.existApiKey ? MASKED_API_KEY : '',
      apiUrl: '',
      defaultModel: '',
      timeout: currentPlatformData.defaultOptions?.timeout,
      temperature: currentPlatformData.defaultOptions?.temperature,
      maxTokens: 4000,
      enableSearch: currentPlatformData.defaultOptions?.enableSearch ?? true,
      enableThinking: currentPlatformData.defaultOptions?.enableThinking ?? true,
    }
  } else {
    // 如果找不到平台配置，重置为默认值
    aiSettings.value = JSON.parse(JSON.stringify(DEFAULT_AI_SETTINGS))
  }
}

// ==================== 数据加载 ====================
const loadSettings = async () => {
  const result = await loadPlatformList()
  platformsData.value = result.platformsData

  if (result.firstProviderId) {
    // 如果当前选中的平台不在列表中，或者没有选中平台，则默认选中第一个
    const currentExists = platformsData.value.some((p) => p.platform === selectedProvider.value)
    if (!selectedProvider.value || !currentExists) {
      selectedProvider.value = result.firstProviderId
    }

    updateAiSettingsForm(selectedProvider.value)
  }
}

// ==================== 设置保存 ====================
const saveAiSettings = async () => {
  const currentProvider = providers.value.find((p) => p.id === selectedProvider.value)
  if (!currentProvider) {
    ElMessage.error('未找到对应的平台')
    return
  }

  const success = await savePlatformSettings(
    selectedProvider.value,
    aiSettings.value,
    currentProvider.enabled,
  )

  if (success) {
    await loadSettings()
  }
}

const saveGeneralSettings = async () => {
  try {
    // TODO: 提交到后端接口保存
    // await api.saveGeneralSettings(generalSettings.value)

    ElMessage.success('通用设置已保存')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请重试')
  }
}

const handleTestConnection = async () => {
  await testPlatformConnection(selectedProvider.value, aiSettings.value)
}

// ==================== 监听器 ====================
// 监听服务商状态变化，同步到后端
watch(
  providers,
  async () => {
    try {
      // TODO: 提交服务商状态到后端
      // await api.updateProvidersState(providers.value)
      console.log('服务商状态已更新')
    } catch (error) {
      console.error('更新服务商状态失败:', error)
    }
  },
  { deep: true },
)

// 监听平台切换，加载对应平台的配置
watch(selectedProvider, (newVal) => {
  updateAiSettingsForm(newVal)
})

// 监听主题设置变化，实时应用主题
watch(
  () => generalSettings.value.theme,
  (newTheme) => {
    theme.value = newTheme as 'light' | 'dark' | 'auto'
  },
)

// ==================== 生命周期 ====================
onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.settings-view {
  display: flex;
  flex: 1;
  min-width: 0;
  height: 100vh;
  background: var(--app-bg-color);
}

@media (max-width: 768px) {
  .settings-view {
    flex-direction: column;
  }
}
</style>
