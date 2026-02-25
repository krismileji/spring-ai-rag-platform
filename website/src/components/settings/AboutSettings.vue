<template>
  <div class="content-section">
    <div class="section-header">
      <h1>{{ t('settings.about.title') }}</h1>
    </div>
    <div class="about-content">
      <div class="about-item">
        <span class="about-label">{{ t('settings.about.version') }}</span>
        <span class="about-value">1.0.0</span>
      </div>
      <div class="about-item">
        <span class="about-label">{{ t('settings.about.update_time') }}</span>
        <span class="about-value">2026-01-13</span>
      </div>
      <div class="about-item">
        <span class="about-label">{{ t('settings.about.description') }}</span>
        <span class="about-value">{{ t('settings.about.app_description') }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<style scoped>
.content-section {
  max-width: 1000px;
}

.section-header {
  margin-bottom: 24px;
  padding: 20px 24px;
  background: var(--app-bg-color-secondary);
  border-radius: 12px;
  box-shadow: var(--app-shadow-sm);
}

.section-header h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: var(--app-text-color-primary);
}

.about-content {
  background: var(--app-card-bg-color);
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--app-shadow-sm);
}

.about-item {
  display: flex;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid var(--app-border-color-light);
}

.about-item:last-child {
  border-bottom: none;
}

.about-label {
  font-size: 14px;
  color: var(--app-text-color-secondary);
}

.about-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text-color-primary);
}
</style>
