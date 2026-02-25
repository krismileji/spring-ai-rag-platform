<template>
  <el-dialog
    v-model="showDialog"
    :close-on-click-modal="false"
    width="440px"
    class="login-dialog"
    @close="handleClose"
  >
    <template #header>
      <div class="dialog-header">
        <h3 class="dialog-title">AI Agent</h3>
      </div>
    </template>

    <div class="login-container">
      <el-card class="login-card" :body-style="{ padding: '32px' }">
        <div class="tab-header">
          <div class="tab-item" :class="{ active: isLoginMode }" @click="isLoginMode = true">
            {{ t('login.title') }}
          </div>
          <div class="tab-item" :class="{ active: !isLoginMode }" @click="isLoginMode = false">
            {{ t('login.register') }}
          </div>
        </div>

        <!-- 登录表单 -->
        <el-form
          v-if="isLoginMode"
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="auth-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              :placeholder="t('login.placeholder.username')"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              :placeholder="t('login.placeholder.password')"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="submit-btn"
              @click="handleLogin"
            >
              {{ t('login.submit') }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 注册表单 -->
        <el-form
          v-else
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="auth-form"
          @submit.prevent="handleRegister"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              :placeholder="t('login.placeholder.username')"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              :placeholder="t('login.placeholder.password')"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              :placeholder="t('login.placeholder.confirm_password')"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleRegister"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="submit-btn"
              @click="handleRegister"
            >
              {{ t('login.register') }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import { checkUsername } from '../../api/model'

interface Props {
  modelValue: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { t } = useI18n()
const authStore = useAuthStore()

const showDialog = ref(props.modelValue)
const isLoginMode = ref(true)
const loading = ref(false)

// 监听外部传入的 modelValue 变化
watch(
  () => props.modelValue,
  (newVal) => {
    showDialog.value = newVal
  },
)

// 监听内部 showDialog 变化，同步到外部
watch(showDialog, (newVal) => {
  emit('update:modelValue', newVal)
})

// 关闭弹窗
const handleClose = () => {
  showDialog.value = false
}

// 登录表单
const loginFormRef = ref<FormInstance>()
const loginForm = ref({
  username: '',
  password: '',
})

const loginRules = computed<FormRules>(() => ({
  username: [{ required: true, message: t('login.validation.username_required'), trigger: 'blur' }],
  password: [{ required: true, message: t('login.validation.password_required'), trigger: 'blur' }],
}))

// 注册表单
const registerFormRef = ref<FormInstance>()
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
})

const validateConfirmPassword = (
  _rule: unknown,
  value: string,
  callback: (error?: Error) => void,
) => {
  if (value === '') {
    callback(new Error(t('login.validation.password_confirm_required')))
  } else if (value !== registerForm.value.password) {
    callback(new Error(t('login.validation.password_mismatch')))
  } else {
    callback()
  }
}

// 异步校验用户名
const validateUsername = async (
  _rule: unknown,
  value: string,
  callback: (error?: Error) => void,
) => {
  if (!value) {
    callback(new Error(t('login.validation.username_required')))
    return
  }

  try {
    const response = await checkUsername(value)
    if (response.errorCode !== '00000') {
      // 错误已在拦截器中统一处理
      callback(new Error(response.userTip || t('login.validation.check_failed')))
    } else if (response.data) {
      // data 为 true 表示用户名已存在
      callback(new Error(t('login.validation.username_exists')))
    } else {
      callback()
    }
  } catch {
    callback(new Error(t('login.validation.check_error')))
  }
}

const registerRules = computed<FormRules>(() => ({
  username: [
    { required: true, message: t('login.validation.username_required'), trigger: 'blur' },
    { validator: validateUsername as never, trigger: 'blur' },
  ],
  password: [
    { required: true, message: t('login.validation.password_required'), trigger: 'blur' },
    { min: 6, message: t('login.validation.password_length'), trigger: 'blur' },
  ],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
}))

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.login(loginForm.value.username, loginForm.value.password)
        ElMessage.success(t('login.message.login_success'))
        showDialog.value = false
        // 重置表单
        loginForm.value = { username: '', password: '' }
      } catch {
        // 错误已在axios拦截器中统一处理，这里不需要再次提示
      } finally {
        loading.value = false
      }
    }
  })
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.register(registerForm.value.username, registerForm.value.password)

        // 注册成功，提示是否立即登录
        ElMessageBox.confirm(t('login.message.register_success_confirm'), t('common.prompt'), {
          confirmButtonText: t('login.action.login_now'),
          cancelButtonText: t('login.action.login_later'),
          type: 'success',
        })
          .then(async () => {
            // 点击“立即登录”，使用注册的用户名和密码登录
            try {
              await authStore.login(registerForm.value.username, registerForm.value.password)
              ElMessage.success(t('login.message.login_success'))
              showDialog.value = false
              // 重置表单
              registerForm.value = { username: '', password: '', confirmPassword: '' }
            } catch {
              // 错误已在axios拦截器中统一处理
              // 切换到登录模式
              isLoginMode.value = true
            }
          })
          .catch(() => {
            // 点击"稍后登录"，关闭弹窗
            showDialog.value = false
            // 重置表单
            registerForm.value = { username: '', password: '', confirmPassword: '' }
          })
      } catch {
        // 错误已在axios拦截器中统一处理，这里不需要再次提示
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* 登录弹窗样式 */
:deep(.login-dialog) {
  border-radius: 12px;
  background: var(--app-card-bg-color);
}

:deep(.login-dialog .el-dialog__header) {
  padding: 20px 24px;
  margin: 0;
  border-bottom: 1px solid var(--app-border-color-light);
}

:deep(.login-dialog .el-dialog__title) {
  color: var(--app-text-color-primary);
}

:deep(.login-dialog .el-dialog__body) {
  padding: 0;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--app-primary-color);
  margin: 0;
}

.login-container {
  width: 100%;
}

.login-card {
  width: 100%;
  border: none;
  box-shadow: none;
  background: transparent;
}

:deep(.login-card .el-card__body) {
  background: var(--app-card-bg-color);
}

.tab-header {
  display: flex;
  margin-bottom: 32px;
  border-bottom: 2px solid var(--app-border-color-light);
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px;
  font-size: 18px;
  font-weight: 600;
  color: var(--app-text-color-secondary);
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.tab-item.active {
  color: var(--app-primary-color);
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--app-primary-color);
}

.auth-form {
  margin-top: 24px;
}

.auth-form .el-form-item {
  margin-bottom: 24px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  /* background handled by element plus type="primary" */
}

.submit-btn:hover {
  opacity: 0.9;
}
</style>
