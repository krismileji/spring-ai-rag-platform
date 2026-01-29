<template>
  <el-dialog
    :model-value="modelValue"
    :title="editingKb ? '编辑知识库' : '创建知识库'"
    width="500px"
    @update:model-value="emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="localForm" :rules="rules" label-width="80px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="localForm.name" placeholder="请输入知识库名称" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input
          v-model="localForm.description"
          type="textarea"
          :rows="4"
          placeholder="请输入知识库描述（可选）"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSave">
        {{ editingKb ? '保存' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, FormItemRule } from 'element-plus'
import {
  checkKnowledgeName,
  addEditKnowledge,
  type KnowledgeAddEditRequest,
  type KnowledgeVO,
} from '../../api/model'

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
    return '校验失败，请重试'
  } catch (error) {
    console.error('校验名称失败:', error)
    return '校验失败，请重试'
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
      callback(new Error('校验失败，请重试'))
    })
}

const rules: FormRules = {
  name: [
    { required: true, message: '请输入知识库名称', trigger: 'blur' },
    { validator: validateKnowledgeName, trigger: 'blur' },
  ],
}

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
          ElMessage.success(props.editingKb ? '保存成功' : '创建成功')
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
