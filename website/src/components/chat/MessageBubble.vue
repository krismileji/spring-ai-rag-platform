<template>
  <div class="message-bubble" :class="[message.role]">
    <div v-if="message.role === 'assistant'" class="avatar">
      <el-avatar :size="36" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
        🤖
      </el-avatar>
    </div>

    <div class="message-content">
      <div v-if="message.role === 'assistant' && message.model" class="model-name">
        {{ message.model }}
      </div>
      <div class="message-text">
        <div v-if="message.role === 'assistant' && message.isLoading" class="loading-indicator">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </div>
        <template v-else-if="message.role === 'assistant'">
          <!-- 显示思考过程 -->
          <div v-if="message.reasoningContent || message.isThinking" class="reasoning-section">
            <div class="reasoning-header" @click="!message.isThinking && toggleReasoning(message.id)" :class="{ clickable: !message.isThinking }">
              <span class="reasoning-icon">🧠</span>
              <span class="reasoning-title">
                {{ message.isThinking ? t('chat.message.thinking') : t('chat.message.thinking_completed') }}
              </span>
              <span v-if="message.isThinking || message.thinkingDuration" class="reasoning-duration">{{ t('chat.message.thinking_duration', { duration: thinkingElapsedTime }) }}</span>
              <span v-if="!message.isThinking" class="toggle-btn">{{ isReasoningExpanded(message.id) ? t('chat.message.collapse') : t('chat.message.expand') }}</span>
            </div>
            <transition name="reasoning-collapse">
              <div v-show="(isReasoningExpanded(message.id) || message.isThinking) && message.reasoningContent" class="reasoning-content-wrapper">
                <AgentMarkdown
                  :content="message.reasoningContent"
                  :md-options="mdOptions"
                  class="reasoning-content"
                />
              </div>
            </transition>
          </div>
          <!-- 显示回复内容 -->
          <AgentMarkdown
            :content="message.content"
            :md-options="mdOptions"
          />
        </template>
        <div v-else class="user-message">{{ message.content }}</div>
      </div>
      <div class="message-time">{{ formatTime(message.timestamp) }}</div>
    </div>

    <div v-if="message.role === 'user'" class="avatar">
      <el-avatar :size="36" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
        👤
      </el-avatar>
    </div>
  </div>
</template>

<script setup lang="ts">
import { AgentMarkdown } from 'agent-markdown-vue'
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import 'animate.css'
import type { Message } from '../../stores/chat'

const { t, locale } = useI18n()

const props = defineProps<{
  message: Message
}>()

const mdOptions = {
  html: true,
  breaks: true,
  linkify: true
}

const formatTime = (timestamp: number) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString(locale.value, { hour: '2-digit', minute: '2-digit' })
}

// 管理折叠状态
const expandedReasoningIds = ref<Set<string>>(new Set())

const toggleReasoning = (messageId: string) => {
  if (expandedReasoningIds.value.has(messageId)) {
    expandedReasoningIds.value.delete(messageId)
  } else {
    expandedReasoningIds.value.add(messageId)
  }
}

const isReasoningExpanded = (messageId: string) => {
  return expandedReasoningIds.value.has(messageId)
}

// 实时计算思考用时
const currentTime = ref(Date.now())
let timer: number | null = null

const thinkingElapsedTime = computed(() => {
  if (!props.message.isThinking || !props.message.thinkingStartTime) {
    return props.message.thinkingDuration || 0
  }
  return Math.round((currentTime.value - props.message.thinkingStartTime) / 1000)
})

const startTimer = () => {
  if (timer === null) {
    timer = window.setInterval(() => {
      currentTime.value = Date.now()
    }, 1000)
  }
}

const stopTimer = () => {
  if (timer !== null) {
    clearInterval(timer)
    timer = null
  }
}

onMounted(() => {
  if (props.message.isThinking) {
    startTimer()
  }
})

onUnmounted(() => {
  stopTimer()
})

// 监听思考状态变化
watch(() => props.message.isThinking, (isThinking) => {
  if (isThinking) {
    startTimer()
  } else {
    stopTimer()
  }
})
</script>

<style scoped>
.message-bubble {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-bubble.user {
  flex-direction: row-reverse;
}

.message-bubble.assistant {
  flex-direction: row;
}

.avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.model-name {
  font-size: 12px;
  color: var(--app-text-color-secondary);
  padding: 0 8px;
  font-weight: 500;
}

.message-text {
  border-radius: 12px;
  overflow: hidden;
}

.user-message {
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.message-bubble.user .message-text {
  background: var(--app-primary-color);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-bubble.user .user-message {
  color: #fff;
}

.message-bubble.assistant .message-text {
  background: var(--app-card-bg-color);
  color: var(--app-text-color-primary);
  border-bottom-left-radius: 4px;
  padding: 12px 16px;
  box-shadow: var(--app-shadow-sm);
}

/* 覆盖 agent-markdown-vue 默认样式 */
.message-bubble.assistant :deep(.markdown-body) {
  padding: 0;
  background: transparent !important;
  color: var(--app-text-color-primary);
  font-size: 14px;
  line-height: 1.6;
}

/* 覆盖代码块背景 */
.message-bubble.assistant :deep(pre) {
  background: var(--app-bg-color-tertiary) !important;
  border-radius: 6px;
  padding: 12px;
  margin: 8px 0;
  border: 1px solid var(--app-border-color);
}

.message-bubble.assistant :deep(code) {
  background: var(--app-bg-color-tertiary) !important;
  padding: 2px 6px;
  border-radius: 4px;
  color: var(--app-text-color-primary);
}

/* 思考过程样式 */
.reasoning-section {
  background: var(--app-bg-color-tertiary);
  border: 1px solid var(--app-border-color);
  border-radius: 6px;
  padding: 8px 12px;
  margin-bottom: 12px;
}

.reasoning-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 400;
  color: var(--app-text-color-secondary);
  user-select: none;
  font-size: 13px;
}

.reasoning-header.clickable {
  cursor: pointer;
}

.reasoning-header.clickable:hover .toggle-btn {
  color: var(--app-primary-color);
}

.reasoning-icon {
  font-size: 14px;
}

.reasoning-title {
  flex: 1;
}

.reasoning-duration {
  color: var(--app-text-color-disabled);
  font-size: 12px;
}

.toggle-btn {
  color: var(--app-text-color-secondary);
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.toggle-btn:hover {
  background: var(--app-sidebar-item-hover-bg);
}

.reasoning-content-wrapper {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--app-border-color);
  overflow: hidden;
}

/* 折叠动画 */
.reasoning-collapse-enter-active,
.reasoning-collapse-leave-active {
  transition: all 0.3s ease;
  max-height: 1000px;
}

.reasoning-collapse-enter-from,
.reasoning-collapse-leave-to {
  max-height: 0;
  margin-top: 0;
  padding-top: 0;
  border-top: none;
  opacity: 0;
}

.reasoning-content :deep(.markdown-body) {
  font-size: 13px;
  line-height: 1.5;
  color: var(--app-text-color-secondary);
}

.reasoning-content :deep(pre) {
  background: var(--app-bg-color-tertiary) !important;
}

.reasoning-content :deep(code) {
  background: var(--app-bg-color-tertiary) !important;
}

.message-time {
  font-size: 11px;
  color: var(--app-text-color-disabled);
  padding: 0 8px;
}

.message-bubble.user .message-time {
  text-align: right;
}

.message-bubble.assistant .message-time {
  text-align: left;
}

/* 加载动画 */
.loading-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
}

.loading-indicator .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--app-text-color-disabled);
  animation: bounce 1.4s infinite ease-in-out both;
}

.loading-indicator .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-indicator .dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
