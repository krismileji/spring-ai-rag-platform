<template>
  <div class="message-input">
    <div class="input-container">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="3"
        resize="none"
        :placeholder="t('chat.input.placeholder')"
        class="input-textarea"
        @keydown="handleKeyDown"
      />

      <div class="input-toolbar">
        <div class="toolbar-left">
          <button
            class="thinking-toggle"
            :class="{ active: enableThinking }"
            @click="enableThinking = !enableThinking"
          >
            <span class="toggle-icon">{{ enableThinking ? '🧠' : '⚡' }}</span>
            <span class="toggle-text">{{ enableThinking ? t('chat.input.thinking_on') : t('chat.input.thinking_off') }}</span>
          </button>
        </div>

        <div class="toolbar-right">
          <el-button
            type="primary"
            :icon="Promotion"
            round
            :disabled="!inputText.trim()"
            :loading="isSending"
            @click="handleSend"
            class="send-btn"
          >
            {{ t('chat.input.send') }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Promotion } from '@element-plus/icons-vue'
import { useChatStore } from '../../stores/chat'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const chatStore = useChatStore()
const inputText = ref('')
const isSending = ref(false)
const enableThinking = ref(false)

const handleKeyDown = (event: KeyboardEvent) => {
  // Shift+Enter: 换行（默认行为）
  if (event.shiftKey && event.key === 'Enter') {
    // 允许默认的换行行为
    return
  }

  // 单独按 Enter: 发送消息
  if (event.key === 'Enter' && !event.shiftKey && !event.ctrlKey && !event.metaKey) {
    event.preventDefault()
    handleSend()
  }
}

const handleSend = async () => {
  if (!inputText.value.trim()) {
    ElMessage.warning(t('chat.input.warning.empty'))
    return
  }

  isSending.value = true
  const content = inputText.value.trim()
  inputText.value = ''

  try {
    await chatStore.sendMessage(content, enableThinking.value)
  } finally {
    isSending.value = false
  }
}
</script>

<style scoped>
.message-input {
  padding: 16px 24px;
  background: var(--app-card-bg-color);
  border-top: 1px solid var(--app-border-color);
  transition: background-color 0.3s, border-color 0.3s;
}

.input-container {
  max-width: 900px;
  margin: 0 auto;
}

.input-textarea {
  margin-bottom: 12px;
}

.input-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 1px solid var(--app-border-color);
  padding: 16px;
  font-size: 14px;
  line-height: 1.6;
  transition: all 0.3s;
  background: var(--app-bg-color-secondary);
  color: var(--app-text-color-primary);
  box-shadow: var(--app-shadow-sm);
}

.input-textarea :deep(.el-textarea__inner):focus {
  border-color: var(--app-primary-color);
  box-shadow: 0 0 0 2px var(--app-primary-light), var(--app-shadow-md);
}

.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px;
}

.toolbar-left {
  display: flex;
  gap: 8px;
  align-items: center;
}

.toolbar-left .el-button {
  color: var(--app-text-color-secondary);
}

.toolbar-left .el-button:hover {
  color: var(--app-primary-color);
  background: var(--app-primary-light);
}

.thinking-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: transparent;
  border: 1px solid var(--app-border-color);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
  color: var(--app-text-color-secondary);
  font-weight: 500;
}

.thinking-toggle:hover {
  border-color: var(--app-primary-color);
  background: var(--app-primary-light);
  color: var(--app-primary-color);
}

.thinking-toggle.active {
  background: var(--app-primary-light);
  border-color: var(--app-primary-color);
  color: var(--app-primary-color);
  box-shadow: none;
}

.send-btn {
  padding: 8px 24px;
  height: auto;
  font-weight: 500;
  border-radius: 8px;
  box-shadow: var(--app-shadow-sm);
  transition: all 0.2s ease;
}

.send-btn:hover {
  transform: translateY(-1px);
  box-shadow: var(--app-shadow-md);
}

.send-btn:active {
  transform: translateY(0);
}
</style>
