<template>
  <div class="sidebar-nav">
    <div class="nav-header">
      <!-- 未登录状态 -->
      <div v-if="!authStore.isLoggedIn" class="login-prompt" @click="authStore.showLoginDialog = true">
        <el-tooltip :content="t('nav.login')" placement="right">
          <div class="user-avatar">
            <el-avatar :size="40">
              <el-icon :size="24"><User /></el-icon>
            </el-avatar>
          </div>
        </el-tooltip>
      </div>

      <!-- 已登录状态 -->
      <el-popover
        v-else
        placement="right"
        :width="260"
        trigger="click"
        :offset="16"
        popper-class="user-popover"
      >
        <template #reference>
          <div class="user-avatar">
            <el-avatar :size="40" :src="authStore.user?.avatar">
              <el-icon :size="24"><User /></el-icon>
            </el-avatar>
          </div>
        </template>
        <div class="user-panel">
          <div class="user-info">
            <el-avatar :size="48" :src="authStore.user?.avatar">
              <el-icon :size="24"><User /></el-icon>
            </el-avatar>
            <div class="user-details">
              <div class="user-name">{{ authStore.user?.name }}</div>
              <div class="user-email">{{ authStore.user?.email }}</div>
            </div>
          </div>

          <div class="user-menu">
            <div class="menu-item logout" @click="handleMenuClick('logout')">
              <el-icon><SwitchButton /></el-icon>
              <span>{{ t('nav.logout') }}</span>
            </div>
          </div>
        </div>
      </el-popover>
    </div>

    <div class="nav-menu">
      <el-tooltip :content="t('nav.chat')" placement="right">
        <div class="nav-item" :class="{ active: isActive('/chat') }" @click="navigateTo('/chat')">
          <el-icon :size="24"><ChatDotRound /></el-icon>
        </div>
      </el-tooltip>

      <el-tooltip :content="t('nav.knowledge')" placement="right">
        <div
          class="nav-item"
          :class="{ active: isActive('/knowledge') }"
          @click="navigateTo('/knowledge')"
        >
          <el-icon :size="24"><Document /></el-icon>
        </div>
      </el-tooltip>
    </div>

    <div class="nav-footer">
      <el-tooltip :content="t('nav.settings')" placement="right">
        <div
          class="nav-item"
          :class="{ active: isActive('/settings') }"
          @click="navigateTo('/settings')"
        >
          <el-icon :size="24"><Setting /></el-icon>
        </div>
      </el-tooltip>

      <el-tooltip :content="languageTooltip" placement="right">
        <div class="nav-item" @click="toggleLanguage">
          <span style="font-size: 14px; font-weight: bold;">{{ languageLabel }}</span>
        </div>
      </el-tooltip>

      <el-tooltip :content="theme === 'dark' ? t('theme.light') : t('theme.dark')" placement="right">
        <div class="nav-item" @click="toggleTheme">
          <el-icon :size="24"><component :is="themeIcon" /></el-icon>
        </div>
      </el-tooltip>
    </div>

    <!-- 登录/注册弹窗 -->
    <LoginDialog v-model="authStore.showLoginDialog" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatDotRound,
  Document,
  Setting,
  Sunny,
  Moon,
  User,
  SwitchButton,
} from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import { useTheme } from '../../composables/useTheme'
import LoginDialog from './LoginDialog.vue'

const { t, locale } = useI18n()
const authStore = useAuthStore()
const { theme } = useTheme()
const route = useRoute()
const router = useRouter()

const themeIcon = computed(() => (theme.value === 'dark' ? Moon : Sunny))
const languageLabel = computed(() => t('nav.lang_label'))
const languageTooltip = computed(() => t('nav.switch_language'))

const toggleTheme = () => {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
}

const toggleLanguage = () => {
  locale.value = locale.value === 'zh-CN' ? 'en-US' : 'zh-CN'
  localStorage.setItem('app_locale', locale.value)
  ElMessage.success(t('common.language_switched'))
}

const pendingNavPath = ref<string | null>(null)

// 计算当前激活的导航项
const isActive = (path: string) => {
  return route.path === path
}

// 导航跳转
const navigateTo = async (path: string) => {
  // 如果是知识库页面或设置页面且未登录，需要先登录
  if ((path === '/knowledge' || path === '/settings') && !authStore.isLoggedIn) {
    try {
      const messageKey = path === '/knowledge' ? 'common.login_required' : 'common.login_required_settings'
      await ElMessageBox.confirm(t(messageKey), t('common.prompt'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning',
      })
      // 用户点击确定，保存目标路径并打开登录弹窗
      pendingNavPath.value = path
      authStore.showLoginDialog = true
    } catch {
      // 用户点击取消，直接返回
      return
    }
    return
  }

  router.push(path)
}

const handleMenuClick = (action: string) => {
  if (action === 'logout') {
    authStore.logout()
    ElMessage.success(t('common.logout_success'))
  }
}

// 监听登录状态变化，登录成功后跳转到待访问的页面
watch(
  () => authStore.isLoggedIn,
  (newVal) => {
    if (newVal && pendingNavPath.value) {
      router.push(pendingNavPath.value)
      pendingNavPath.value = null
    }
  },
)
</script>

<style scoped>
.sidebar-nav {
  width: 64px;
  flex-shrink: 0;
  height: 100vh;
  background: var(--app-sidebar-bg-color);
  border-right: var(--app-sidebar-border-right);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
  transition: background-color 0.3s, border-color 0.3s;
}

.nav-header {
  margin-bottom: 32px;
}

.user-avatar {
  cursor: pointer;
  transition: transform 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar:hover {
  transform: scale(1.05);
}

/* 用户面板样式 */
:deep(.user-popover) {
  padding: 0 !important;
  border-radius: 12px !important;
  box-shadow: var(--app-shadow-lg) !important;
  border: 1px solid var(--app-border-color) !important;
  background: var(--app-card-bg-color) !important;
}

.user-panel {
  padding: 16px;
  background: var(--app-card-bg-color);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--app-border-color-light);
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text-color-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-email {
  font-size: 13px;
  color: var(--app-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-menu {
  padding-top: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  margin: 2px 0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: var(--app-text-color-primary);
  transition: all 0.2s;
}

.menu-item:hover {
  background: var(--app-bg-color);
  color: var(--app-primary-color);
}

.menu-item.logout:hover {
  background: #fef2f2;
  color: var(--app-error-color);
}

.menu-item .el-icon {
  font-size: 18px;
  color: var(--app-text-color-secondary);
}

.menu-item:hover .el-icon {
  color: currentColor;
}



.nav-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-footer {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: auto;
}

.nav-item {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  cursor: pointer;
  color: var(--app-sidebar-text-color);
  transition: all 0.3s;
  position: relative;
}

.nav-item:hover {
  background: var(--app-sidebar-item-hover-bg);
  color: var(--app-sidebar-text-active);
  transform: translateX(2px);
}

.nav-item.active {
  background: var(--app-sidebar-item-active-bg);
  color: var(--app-sidebar-text-active);
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: -4px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: var(--app-sidebar-text-active);
  border-radius: 2px;
}

/* 未登录状态 */
.login-prompt {
  cursor: pointer;
}

.login-prompt .user-avatar {
  transition: transform 0.3s;
}

.login-prompt .user-avatar:hover {
  transform: scale(1.05);
}

.login-prompt .el-avatar {
  background: var(--app-primary-light);
  color: var(--app-primary-color);
}
</style>
