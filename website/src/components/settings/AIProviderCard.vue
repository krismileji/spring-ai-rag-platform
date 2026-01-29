<template>
  <div class="provider-card" :class="{ active: isSelected }" @click="handleSelect">
    <div class="provider-header">
      <div class="provider-info">
        <div class="provider-avatar" :style="{ background: provider.color }">
          {{ provider.name.charAt(0) }}
        </div>
        <div class="provider-name">{{ provider.name }}</div>
      </div>
      <el-switch :model-value="provider.enabled" @click.stop @update:model-value="handleToggle" />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Provider {
  id: string
  name: string
  enabled: boolean
  color: string
}

interface Props {
  provider: Provider
  isSelected: boolean
}

interface Emits {
  (e: 'select', id: string): void
  (e: 'toggle', id: string, enabled: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const handleSelect = () => {
  emit('select', props.provider.id)
}

const handleToggle = (enabled: boolean) => {
  emit('toggle', props.provider.id, enabled)
}
</script>

<style scoped>
.provider-card {
  width: 100%;
  background: var(--app-card-bg-color);
  border: 2px solid var(--app-border-color);
  border-radius: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: all 0.2s;
  box-sizing: border-box;
}

.provider-card:hover {
  border-color: var(--app-primary-color);
  box-shadow: var(--app-shadow-md);
}

.provider-card.active {
  border-color: var(--app-primary-color);
  background: var(--app-primary-light);
}

.provider-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.provider-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.provider-avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 16px;
}

.provider-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text-color-primary);
}
</style>
