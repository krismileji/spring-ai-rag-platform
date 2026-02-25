<template>
  <div class="home-view">
    <div class="home-header">
      <h1>{{ t('knowledge.home.title') }}</h1>
    </div>
    <div class="action-cards">
      <div class="action-card" @click="$emit('create-kb')">
        <div class="card-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
          <el-icon :size="32"><FolderAdd /></el-icon>
        </div>
        <h3>{{ t('knowledge.home.create_kb') }}</h3>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FolderAdd } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineEmits<{
  (e: 'create-kb'): void
}>()
</script>

<style scoped>
.home-view {
  max-width: 1200px;
}

.home-header h1 {
  font-size: 32px;
  font-weight: 600;
  color: var(--app-text-color-primary);
  margin-bottom: 32px;
}

.action-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.action-card {
  background: var(--app-card-bg-color);
  border: 1px solid var(--app-border-color);
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.action-card:hover {
  border-color: var(--app-primary-color);
  transform: translateY(-4px);
  box-shadow: var(--app-shadow-lg);
}

.card-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin: 0 auto 16px;
}

.action-card h3 {
  font-size: 16px;
  font-weight: 500;
  color: var(--app-text-color-primary);
  margin: 0;
}

@media (max-width: 1024px) {
  .action-cards {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  }
}
</style>
