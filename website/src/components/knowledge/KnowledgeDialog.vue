<template>
  <el-dialog
    :model-value="modelValue"
    :title="editingKb ? t('knowledge.dialog.title_edit') : t('knowledge.dialog.title_create')"
    width="500px"
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="localForm" :rules="rules" label-width="80px">
      <el-form-item :label="t('knowledge.dialog.name_label')" prop="name">
        <el-input v-model="localForm.name" :placeholder="t('knowledge.dialog.name_placeholder')" />
      </el-form-item>
      <el-form-item :label="t('knowledge.dialog.desc_label')">
        <el-input
          v-model="localForm.description"
          type="textarea"
          :rows="4"
          :placeholder="t('knowledge.dialog.desc_placeholder')"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="handleSave">
        {{ editingKb ? t('common.save') : t('knowledge.dialog.create') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, FormItemRule } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  checkKnowledgeName,
  addEditKnowledge,
  type KnowledgeAddEditRequest,
  type KnowledgeVO,
} from '../../api/model'

const { t } = useI18n()

interface FileItem {
  id: string
  name: string
  createTime: string
}

interface KnowledgeBase extends KnowledgeVO {
  files?: FileItem[]
  createTime: string
}

interface KbForm {
  name: string
  description: string
}

const props = defineProps<{
  modelValue: boolean
  editingKb: KnowledgeBase | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'saved'): void
}>()

const formRef = ref<FormInstance>()

// 创建本地表单数据
const localForm = reactive<KbForm>({
  name: '',
  description: '',
})

// 同步 props.editingKb 到 localForm
watch(
  () => props.editingKb,
  (kb) => {
    if (kb) {
      localForm.name = kb.name
      localForm.description = kb.description || ''
    } else {
      localForm.name = ''
      localForm.description = ''
    }
  },
  { immediate: true },
)

// 校验知识库名称
const validateName = async (name: string): Promise<string | null> => {
  try {
    const checkRes = await checkKnowledgeName(name)
    if (checkRes.errorCode === '00000') {
      return checkRes.data // 返回错误信息或null
    }
    return t('knowledge.validation.check_failed')
  } catch (error) {
    console.error('校验名称失败:', error)
    return t('knowledge.validation.check_failed')
  }
}

// 表单验证规则
const validateKnowledgeName: FormItemRule['validator'] = (_rule, value, callback) => {
  if (!value || !value.trim()) {
    callback()
    return
  }
  // 编辑时跳过名称未变的情况
  if (props.editingKb && value === props.editingKb.name) {
    callback()
    return
  }
  // 调用校验函数
  validateName(value)
    .then((errorMsg) => {
      if (errorMsg === null) {
        callback()
      } else {
        callback(new Error(errorMsg))
      }
    })
    .catch(() => {
      callback(new Error(t('knowledge.validation.check_failed')))
    })
}

const rules = computed<FormRules>(() => ({
  name: [
    { required: true, message: t('knowledge.validation.name_required'), trigger: 'blur' },
    { validator: validateKnowledgeName, trigger: 'blur' },
  ],
}))

watch(
  () => props.modelValue,
  (newVal) => {
    if (!newVal) {
      handleClose()
    }
  },
)

const handleClose = () => {
  formRef.value?.resetFields()
  emit('update:modelValue', false)
}

const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 构建请求参数
      const request: KnowledgeAddEditRequest = {
        name: localForm.name,
        description: localForm.description,
      }

      if (props.editingKb) {
        request.id = props.editingKb.id
      }

      try {
        const res = await addEditKnowledge(request)
        if (res.errorCode === '00000') {
          ElMessage.success(props.editingKb ? t('knowledge.message.save_success') : t('knowledge.message.create_success'))
          emit('saved')
          handleClose()
        }
      } catch (error) {
        console.error('保存知识库失败:', error)
      }
    }
  })
}
</script>
