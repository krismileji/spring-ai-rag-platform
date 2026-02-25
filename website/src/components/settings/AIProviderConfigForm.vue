<template>
  <div class="provider-config">
    <div class="config-header">
      <h2>{{ t('settings.ai.config_title', { name: providerName }) }}</h2>
    </div>
    <el-form ref="formRef" :model="settings" :rules="formRules" class="config-form">
      <div class="form-row">
        <div class="form-col form-col-full">
          <el-form-item prop="apiKey">
            <template #label>
              <span class="form-label">API Key</span>
            </template>
            <el-input
              :model-value="settings.apiKey"
              type="password"
              :placeholder="t('settings.message.api_key_required')"
              show-password
              size="large"
              @update:model-value="updateSetting('apiKey', $event)"
            />
          </el-form-item>
        </div>
      </div>

      <div class="form-actions">
        <el-button type="primary" size="large" @click="handleSave"> {{ t('settings.ai.save_config') }} </el-button>
        <el-button size="large" @click="handleTest">{{ t('settings.ai.test_connection') }}</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

// 假的加密文本，用于显示已存在的 API Key
const MASKED_API_KEY = '********************************'

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
  providerName: string
  settings: AiSettings
}

interface Emits {
  (e: 'update:settings', value: AiSettings): void
  (e: 'save'): void
  (e: 'validate-field', field: string): void
  (e: 'test'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()

// 动态计算是否为加密文本
const isApiKeyMasked = computed(() => props.settings.apiKey === MASKED_API_KEY)

// 根据是否为加密文本来决定校验规则
const formRules = computed<FormRules>(() => ({
  apiKey: [
    {
      required: true, // 始终显示必填星号
      message: t('settings.message.api_key_required'),
      trigger: ['blur', 'change'],
      validator: (_rule, value, callback) => {
        // 如果是加密文本，跳过校验
        if (isApiKeyMasked.value) {
          callback()
          return
        }
        // 如果不是加密文本，检查是否为空
        if (!value || typeof value !== 'string' || !value.trim()) {
          callback(new Error(t('settings.message.api_key_required')))
          return
        }
        callback()
      },
    },
  ],
}))

const updateSetting = (key: keyof AiSettings, value: string | number | boolean) => {
  const updatedSettings = { ...props.settings, [key]: value }
  emit('update:settings', updatedSettings)
}

const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate((valid) => {
    if (valid) {
      emit('save')
    }
  })
}

const handleTest = async () => {
  if (!formRef.value) return

  await formRef.value.validate((valid) => {
    if (valid) {
      emit('test')
    }
  })
}

// 暴露表单校验方法给父组件
const validateField = (field: string) => {
  if (!formRef.value) return Promise.reject()
  return formRef.value.validateField(field)
}

defineExpose({
  validateField,
})
</script>

<style scoped>
.provider-config {
  background: var(--app-card-bg-color);
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--app-shadow-sm);
}

.config-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--app-border-color-light);
}

.config-header h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text-color-primary);
}

.config-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  flex-direction: column;
}

.form-col {
  display: flex;
  flex-direction: column;
}

.form-col-full {
  width: 100%;
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
