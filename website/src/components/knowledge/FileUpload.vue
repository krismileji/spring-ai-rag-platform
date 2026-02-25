<template>
  <input ref="fileInputRef" type="file" style="display: none" multiple @change="handleFileSelect" />
  <FileContentDialog
    v-model="dialogVisible"
    :files-data="uploadedFiles"
    :knowledge-id="knowledgeId"
    @success="handleSaveSuccess"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { uploadKnowledgeFile } from '@/api/knowledge-api'
import { ElMessage } from 'element-plus'
import FileContentDialog from './FileContentDialog.vue'
import type { KnowledgeFileUploadVO } from '@/api/model'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const fileInputRef = ref<HTMLInputElement>()
const dialogVisible = ref(false)
const uploadedFiles = ref<KnowledgeFileUploadVO[]>([])
const uploading = ref(false)

const props = defineProps<{
  knowledgeId: string
}>()

const emit = defineEmits<{
  (e: 'upload', files: File[]): void
  (e: 'success'): void
  (e: 'uploadSuccess', data: KnowledgeFileUploadVO[]): void
  (e: 'uploadStart'): void
  (e: 'uploadEnd'): void
}>()

const triggerUpload = () => {
  fileInputRef.value?.click()
}

const handleFileSelect = async (event: Event) => {
  const files = (event.target as HTMLInputElement).files
  if (!files || files.length === 0) return

  const fileArray = Array.from(files)
  emit('upload', fileArray)

  uploading.value = true
  emit('uploadStart')

  // 立即显示弹框并开始 loading
  uploadedFiles.value = []
  dialogVisible.value = true

  try {
    const response = await uploadKnowledgeFile(props.knowledgeId, fileArray)
    if (response.errorCode === '00000') {
      ElMessage.success(response.userTip || t('knowledge.message.upload_success_edit'))
      emit('uploadSuccess', response.data)

      // 更新弹框数据
      if (response.data && response.data.length > 0) {
        uploadedFiles.value = response.data
      } else {
        // 如果没有数据，关闭弹框
        dialogVisible.value = false
      }
    } else {
      // 接口失败时关闭弹框
      dialogVisible.value = false
    }
  } catch {
    // 异常时关闭弹框
    dialogVisible.value = false
  } finally {
    uploading.value = false
    emit('uploadEnd')
    // 清空input
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }
}

// 保存成功后
const handleSaveSuccess = () => {
  emit('success')
}

defineExpose({
  triggerUpload,
  uploading,
})
</script>
