<script setup lang="ts">
import { onMounted } from 'vue'
import SidebarNav from './components/common/SidebarNav.vue'
import { useChatStore } from './stores/chat'
import { useTheme } from './composables/useTheme'

const chatStore = useChatStore()
const { initTheme } = useTheme()

// 初始化聊天store
onMounted(async () => {
  initTheme()
  await chatStore.initialize()
})
</script>

<template>
  <div class="app-container">
    <SidebarNav />
    <router-view />
  </div>
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
