<template>
  <div class="content-section">
    <div class="section-header">
      <h1>{{ t('settings.general.title') }}</h1>
      <p class="section-desc">{{ t('settings.general.description') }}</p>
    </div>

    <div class="config-form">
      <div class="form-row">
        <div class="form-col">
          <div class="form-item">
            <label class="form-label">{{ t('settings.general.language') }}</label>
            <el-select
              :model-value="settings.language"
              size="large"
              @update:model-value="updateSetting('language', $event)"
            >
              <el-option label="简体中文" value="zh-CN" />
              <el-option label="English" value="en-US" />
            </el-select>
          </div>
        </div>
        <div class="form-col">
          <div class="form-item">
            <label class="form-label">{{ t('settings.general.theme') }}</label>
            <el-select
              :model-value="settings.theme"
              size="large"
              @update:model-value="updateSetting('theme', $event)"
            >
              <el-option :label="t('settings.general.theme_light')" value="light" />
              <el-option :label="t('settings.general.theme_dark')" value="dark" />
              <el-option :label="t('settings.general.theme_auto')" value="auto" />
            </el-select>
          </div>
        </div>
      </div>

      <div class="form-actions">
        <el-button type="primary" size="large" @click="handleSave"> {{ t('common.save') }} </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
interface GeneralSettings {
  language: string
  theme: string
}

interface Props {
  settings: GeneralSettings
}

interface Emits {
  (e: 'update:settings', value: GeneralSettings): void
  (e: 'save'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const updateSetting = (key: keyof GeneralSettings, value: string) => {
  const updatedSettings = { ...props.settings, [key]: value }
  emit('update:settings', updatedSettings)
}

const handleSave = () => {
  emit('save')
}
</script>

<style scoped>
.content-section {
  max-width: 1000px;
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

.config-form {
  background: var(--app-card-bg-color);
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--app-shadow-sm);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.form-col {
  display: flex;
  flex-direction: column;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text-color-primary);
}

.form-actions {
  display: flex;
  gap: 12px;
  padding-top: 8px;
}
</style>
