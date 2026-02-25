<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElConfigProvider } from 'element-plus'
// @ts-ignore
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
// @ts-ignore
import en from 'element-plus/dist/locale/en.mjs'
import SidebarNav from './components/common/SidebarNav.vue'
import { useChatStore } from './stores/chat'
import { useTheme } from './composables/useTheme'

const chatStore = useChatStore()
const { initTheme } = useTheme()

const { locale: i18nLocale } = useI18n()
const locale = computed(() => (i18nLocale.value === 'zh-CN' ? zhCn : en))

// 初始化聊天store
onMounted(async () => {
  initTheme()
  await chatStore.initialize()
})
</script>

<template>
  <el-config-provider :locale="locale">
    <div class="app-container">
      <SidebarNav />
      <router-view />
    </div>
  </el-config-provider>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100vh;
  overflow: hidden;
}
</style>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}
</style>
