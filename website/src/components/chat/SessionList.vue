<template>
  <div class="session-list" :class="{ collapsed: isCollapsed }">
    <div class="session-header">
      <el-button v-if="!isCollapsed" class="new-session-btn" @click="handleNewSession">
        <el-icon class="el-icon--left">
          <Plus />
        </el-icon>
        新建对话
      </el-button>
    </div>

    <!-- 折叠按钮：悬停显示 -->
    <div class="toggle-btn">
      <div class="toggle-btn-clickable" @click="toggleCollapse">
        <el-icon>
          <ArrowLeft v-if="!isCollapsed" />
          <ArrowRight v-else />
        </el-icon>
      </div>
    </div>

    <div v-if="!isCollapsed" class="session-search">
      <div class="search-wrapper">
        <el-input v-model="searchText" placeholder="搜索对话..." :prefix-icon="Search" clearable />
      </div>
    </div>

    <div v-if="!isCollapsed" class="session-items">
      <!-- 加载状态 -->
      <div v-if="chatStore.isLoading" class="loading-state">
        <el-icon class="is-loading" :size="32">
          <Loading />
        </el-icon>
        <div class="loading-text">加载中...</div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="filteredSessions.length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <div class="empty-text">暂无会话</div>
        <div class="empty-hint">点击上方按钮创建新对话</div>
      </div>

      <!-- 会话列表 -->
      <div v-for="session in filteredSessions" :key="session.id" class="session-item"
        :class="{ active: session.id === chatStore.currentSessionId }" @click="handleSelectSession(session.id)">
        <div class="session-content">
          <div class="session-title">{{ session.title }}</div>
          <div class="session-preview">{{ session.lastMessage || '暂无消息' }}</div>
        </div>
        <div class="session-actions">
          <el-icon class="action-icon" @click.stop="handleDeleteSession(session.id)">
            <Delete />
          </el-icon>
        </div>
        <div class="session-time">{{ formatTime(session.lastTime) }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Plus, Search, Delete, Loading, ArrowRight, ArrowLeft } from '@element-plus/icons-vue'
import { useChatStore } from '../../stores/chat'
import { ElMessage } from 'element-plus'

const chatStore = useChatStore()
const searchText = ref('')
const isCollapsed = ref(false)

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

const filteredSessions = computed(() => {
  if (!searchText.value) return chatStore.sessions
  return chatStore.sessions.filter((s) =>
    s.title.toLowerCase().includes(searchText.value.toLowerCase()),
  )
})

const handleNewSession = () => {
  chatStore.createSession()
  ElMessage.success('新对话创建成功')
}

const handleSelectSession = (sessionId: string) => {
  chatStore.switchSession(sessionId)
}

const handleDeleteSession = async (sessionId: string) => {
  await chatStore.deleteSession(sessionId)
  ElMessage.success('对话已删除')
}

const formatTime = (timestamp?: number) => {
  if (!timestamp) return ''
  const now = Date.now()
  const diff = now - timestamp
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return new Date(timestamp).toLocaleDateString()
}
</script>

<style scoped>
.session-list {
  width: var(--app-sidebar-width);
  flex-shrink: 0;
  height: 100vh;
  background: var(--app-sidebar-bg-color);
  border-right: 1px solid var(--app-border-color);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease, background-color 0.3s, border-color 0.3s;
  position: relative;
  z-index: 100;
}

.session-list.collapsed {
  width: 0;
  overflow: visible;
}

.session-list.collapsed .toggle-btn {
  right: -60px;
  width: 60px;
}

.session-header {
  padding: 10px 16px;
  display: flex;
  align-items: center;
  height: 60px;
  box-sizing: border-box;
  border-bottom: 1px solid var(--app-border-color);
  transition: border-color 0.3s;
}

.session-list.collapsed .session-header {
  border-bottom: none;
}

.toggle-btn {
  position: absolute;
  top: 0;
  right: -20px;
  width: 20px;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

.toggle-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  width: 20px;
  height: 70px;
  background: var(--app-sidebar-bg-color);
  border: 1px solid var(--app-border-color);
  border-left: none;
  border-radius: 0 8px 8px 0;
  box-shadow: 4px 0 4px var(--app-shadow-color-sm);
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.4s cubic-bezier(0.2, 0.8, 0.2, 1),
    visibility 0.4s cubic-bezier(0.2, 0.8, 0.2, 1),
    border-radius 0.4s cubic-bezier(0.2, 0.8, 0.2, 1),
    background-color 0.3s cubic-bezier(0.25, 0.8, 0.25, 1),
    box-shadow 0.3s cubic-bezier(0.25, 0.8, 0.25, 1),
    transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  pointer-events: none;
  transform: translateY(-50%) scale(0.95);
}

.toggle-btn-clickable {
  position: absolute;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  width: 20px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 2;
}

.toggle-btn .el-icon {
  position: relative;
  z-index: 1;
  font-size: 15px;
  color: var(--app-text-color-secondary);
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.4s cubic-bezier(0.2, 0.8, 0.2, 1),
    visibility 0.4s cubic-bezier(0.2, 0.8, 0.2, 1),
    color 0.3s ease,
    transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  pointer-events: none;
  transform: scale(0.8);
}

.session-list:hover .toggle-btn::before,
.toggle-btn:hover::before {
  opacity: 1;
  visibility: visible;
  transform: translateY(-50%) scale(1);
}

.session-list:hover .toggle-btn .el-icon,
.toggle-btn:hover .el-icon {
  opacity: 1;
  visibility: visible;
  transform: scale(1);
}

.toggle-btn:hover::before {
  background: var(--app-sidebar-item-hover-bg);
  box-shadow: 6px 0 16px var(--app-shadow-color-sm);
  transform: translateY(-50%) scale(1.02);
}

.toggle-btn:hover .el-icon {
  color: var(--app-primary-color);
  transform: scale(1.2);
  transition-delay: 0.05s;
}

.toggle-btn::before {
  border-radius: 0 15px 15px 0px;
}

.new-session-btn {
  width: 100%;
  height: 40px;
  background: var(--app-primary-color);
  border: none;
  color: #fff;
  font-weight: 600;
  border-radius: 10px;
  transition: all 0.2s ease-in-out;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.new-session-btn:hover {
  background: var(--app-primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.new-session-btn:active {
  background: var(--app-primary-active);
  transform: translateY(0);
}

.session-search {
  padding: 12px 16px;
}

.search-wrapper :deep(.el-input__wrapper) {
  box-shadow: none !important;
  border: 1px solid var(--app-border-color);
  border-radius: 10px;
  background: var(--app-bg-color);
  transition: all 0.2s;
  padding: 4px 12px;
}

.search-wrapper :deep(.el-input__wrapper.is-focus) {
  border-color: var(--app-primary-color);
  background: var(--app-bg-color-secondary);
  box-shadow: 0 0 0 3px var(--app-primary-light) !important;
}

.session-items {
  flex: 1;
  overflow-y: auto;
  padding: 4px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.session-item {
  position: relative;
  padding: 14px;
  background: transparent;
  border: 1px solid var(--app-border-color-light);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 0;
}

.session-item:hover {
  background: var(--app-sidebar-item-hover-bg);
  border-color: var(--app-border-color);
  transform: translateY(-1px);
  box-shadow: var(--app-shadow-sm);
  z-index: 1;
}

.session-item.active {
  background: var(--app-sidebar-item-active-bg);
  border-color: var(--app-primary-color);
  box-shadow: var(--app-shadow-sm);
}

.session-item.active .session-title {
  color: var(--app-primary-color);
  font-weight: 600;
}

.session-content {
  padding-right: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.session-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text-color-primary);
  margin-bottom: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-right: 50px;
}

.session-preview {
  font-size: 12px;
  color: var(--app-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  opacity: 0.8;
  line-height: 1.4;
}

.session-time {
  position: absolute;
  top: 15px;
  right: 14px;
  font-size: 11px;
  color: var(--app-text-color-disabled);
  font-variant-numeric: tabular-nums;
}

.session-actions {
  position: absolute;
  top: 50%;
  right: 10px;
  transform: translateY(-50%);
  opacity: 0;
  transition: all 0.2s;
  background: inherit;
  padding: 4px;
  border-radius: 6px;
}

.session-item:hover .session-actions {
  opacity: 1;
  background: var(--app-sidebar-item-hover-bg);
  box-shadow: -8px 0 12px var(--app-sidebar-item-hover-bg);
}
.session-item.active .session-actions {
    background: var(--app-sidebar-item-active-bg);
    box-shadow: -8px 0 12px var(--app-sidebar-item-active-bg);
}

.session-item:hover .session-time {
  opacity: 0;
}

::-webkit-scrollbar {
  width: 4px;
}

::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: 4px;
}

.session-items:hover::-webkit-scrollbar-thumb {
  background: var(--app-border-color);
}

::-webkit-scrollbar-thumb:hover {
  background: var(--app-text-color-disabled);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: var(--app-text-color-disabled);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
  filter: grayscale(1);
}

.empty-text {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--app-text-color-secondary);
}

.empty-hint {
  font-size: 13px;
  color: var(--app-text-color-disabled);
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: var(--app-primary-color);
}

.loading-text {
  margin-top: 12px;
  font-size: 13px;
  color: var(--app-text-color-secondary);
}
</style>
