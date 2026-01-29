<template>
  <div class="settings-content">
    <AIProviderSettings
      v-show="activeTab === 'ai'"
      :providers="providers"
      :selected-provider="selectedProvider"
      :settings="aiSettings"
      @update:providers="updateProviders"
      @update:selected-provider="updateSelectedProvider"
      @update:settings="updateAiSettings"
      @save="handleSaveAiSettings"
      @test="handleTestConnection"
    />

    <GeneralSettings
      v-show="activeTab === 'general'"
      :settings="generalSettings"
      @update:settings="updateGeneralSettings"
      @save="handleSaveGeneralSettings"
    />

    <AboutSettings v-show="activeTab === 'about'" />
  </div>
</template>

<script setup lang="ts">
import AIProviderSettings from './AIProviderSettings.vue'
import GeneralSettings from './GeneralSettings.vue'
import AboutSettings from './AboutSettings.vue'

interface Provider {
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

interface GeneralSettings {
  language: string
  theme: string
}

interface Props {
  activeTab: string
  providers: Provider[]
  selectedProvider: string
  aiSettings: AiSettings
  generalSettings: GeneralSettings
}

interface Emits {
  (e: 'update:providers', value: Provider[]): void
  (e: 'update:selectedProvider', value: string): void
  (e: 'update:aiSettings', value: AiSettings): void
  (e: 'update:generalSettings', value: GeneralSettings): void
  (e: 'saveAiSettings'): void
  (e: 'saveGeneralSettings'): void
  (e: 'testConnection'): void
}

defineProps<Props>()
const emit = defineEmits<Emits>()

const updateProviders = (value: Provider[]) => {
  emit('update:providers', value)
}

const updateSelectedProvider = (value: string) => {
  emit('update:selectedProvider', value)
}

const updateAiSettings = (value: AiSettings) => {
  emit('update:aiSettings', value)
}

const updateGeneralSettings = (value: GeneralSettings) => {
  emit('update:generalSettings', value)
}

const handleSaveAiSettings = () => {
  emit('saveAiSettings')
}

const handleSaveGeneralSettings = () => {
  emit('saveGeneralSettings')
}

const handleTestConnection = () => {
  emit('testConnection')
}

</script>

<style scoped>
.settings-content {
  flex: 1;
  height: 100vh;
  overflow-y: auto;
  padding: 40px;
}

@media (max-width: 768px) {
  .settings-content {
    padding: 20px;
    height: auto;
  }
}
</style>
