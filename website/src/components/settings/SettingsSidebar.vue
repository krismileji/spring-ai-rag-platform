<template>
  <div class="settings-sidebar">
    <div class="sidebar-header">
      <h2>设置</h2>
    </div>
    <div class="sidebar-menu">
      <div
        v-for="item in menuItems"
        :key="item.key"
        class="menu-item"
        :class="{ active: activeTab === item.key }"
        @click="handleTabChange(item.key)"
      >
        <el-icon class="menu-icon">
          <component :is="item.icon" />
        </el-icon>
        <span class="menu-label">{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Setting, Monitor, InfoFilled } from '@element-plus/icons-vue'
import type { Component } from 'vue'

interface MenuItem {
  key: string
  label: string
  icon: Component
}

interface Props {
  activeTab: string
}

interface Emits {
  (e: 'update:activeTab', value: string): void
}

defineProps<Props>()
const emit = defineEmits<Emits>()

const menuItems: MenuItem[] = [
  { key: 'ai', label: 'AI 服务商', icon: Monitor },
  { key: 'general', label: '通用设置', icon: Setting },
  { key: 'about', label: '关于', icon: InfoFilled },
]

const handleTabChange = (key: string) => {
  emit('update:activeTab', key)
}
</script>

<style scoped>
.settings-sidebar {
  width: var(--app-sidebar-width);
  flex-shrink: 0;
  height: 100vh;
  background: var(--app-sidebar-bg-color);
  border-right: 1px solid var(--app-border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: background-color 0.3s, border-color 0.3s;
}

@media (max-width: 768px) {
  .settings-sidebar {
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid var(--app-border-color);
  }
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid var(--app-border-color);
}

.sidebar-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--app-text-color-primary);
}

.sidebar-menu {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--app-text-color-secondary);
}

.menu-item:hover {
  background: var(--app-sidebar-item-hover-bg);
  color: var(--app-text-color-primary);
}

.menu-item.active {
  background: var(--app-primary-light);
  color: var(--app-primary-color);
  font-weight: 500;
}

.menu-icon {
  font-size: 20px;
}

.menu-label {
  font-size: 14px;
}
</style>
