<script setup lang="ts">
import SessionList from '../components/chat/SessionList.vue'
import ChatArea from '../components/chat/ChatArea.vue'
</script>

<template>
  <div class="chat-view">
    <SessionList />
    <ChatArea />
  </div>
</template>

<style scoped>
.chat-view {
  display: flex;
  flex: 1;
  overflow: hidden;
}
</style>
