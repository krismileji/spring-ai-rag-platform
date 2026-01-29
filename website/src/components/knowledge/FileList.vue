<template>
  <div class="file-list">
    <el-empty v-if="!files || files.length === 0" description="暂无文件">
      <el-button type="primary" :icon="Upload" @click="$emit('upload')"> 上传文件 </el-button>
    </el-empty>
    <div v-else class="file-grid">
      <div v-for="file in files" :key="file.id" class="file-card">
        <div class="file-icon">
          <el-icon :size="48"><Document /></el-icon>
        </div>
        <div class="file-info">
          <div class="file-name">{{ file.name }}</div>
          <div class="file-meta">
            <span>{{ file.createTime }}</span>
          </div>
        </div>
        <div class="file-actions">
          <el-button text :icon="View" @click="$emit('view', file)" />
          <el-button text :icon="Delete" @click="$emit('delete', file)" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Document, Upload, View, Delete } from '@element-plus/icons-vue'

interface FileItem {
  id: string
  name: string
  createTime: string
}

defineProps<{
  files?: FileItem[]
}>()

defineEmits<{
  (e: 'upload'): void
  (e: 'view', file: FileItem): void
  (e: 'delete', file: FileItem): void
}>()
</script>

<style scoped>
.file-list {
  background: var(--app-card-bg-color);
  border-radius: 12px;
  padding: 24px;
  min-height: 400px;
  transition: background-color 0.3s;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.file-card {
  border: 1px solid var(--app-border-color);
  border-radius: 8px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.2s;
}

.file-card:hover {
  border-color: var(--app-primary-color);
  box-shadow: var(--app-shadow-sm);
}

.file-icon {
  color: var(--app-primary-color);
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text-color-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: var(--app-text-color-secondary);
}

.file-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.file-card:hover .file-actions {
  opacity: 1;
}

@media (max-width: 1024px) {
  .file-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}
</style>
