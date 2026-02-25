import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { registerByUsername, loginByUsername } from '../api/model'
import type { RegisterByUsernameRequest, LoginByUsernameRequest } from '../api/model'
import { useChatStore } from './chat'

export interface User {
  id: string
  name: string
  email: string
  avatar?: string
}

export const useAuthStore = defineStore('auth', () => {
  const { t } = useI18n()
  const isLoggedIn = ref(false)
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const showLoginDialog = ref(false)

  // 初始化时从 localStorage 读取 token
  const initToken = localStorage.getItem('token')
  if (initToken) {
    token.value = initToken
    isLoggedIn.value = true
    // TODO: 可以根据 token 获取用户信息
  }

  // 登录
  const login = async (username: string, password: string) => {
    const request: LoginByUsernameRequest = {
      username,
      password,
    }

    const response = await loginByUsername(request)

    if (response.errorCode !== '00000') {
      // 错误已在拦截器中统一处理，这里只需要抛出异常即可
      throw new Error(response.userTip || t('auth.login_failed'))
    }

    // 保存 token
    token.value = response.data
    // 将 token 保存到 localStorage
    localStorage.setItem('token', response.data)
    isLoggedIn.value = true

    // 设置用户信息（暂时使用用户名作为基本信息）
    user.value = {
      id: username,
      name: username,
      email: '',
      avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    }

    // 登录成功后加载会话列表
    const chatStore = useChatStore()
    await chatStore.loadSessions()

    return true
  }

  // 注册
  const register = async (username: string, password: string) => {
    const request: RegisterByUsernameRequest = {
      username,
      password,
    }

    const response = await registerByUsername(request)

    if (response.errorCode !== '00000') {
      // 错误已在拦截器中统一处理，这里只需要抛出异常即可
      throw new Error(response.userTip || t('auth.register_failed'))
    }

    // 注册成功,返回 true
    return true
  }

  // 退出登录
  const logout = () => {
    user.value = null
    isLoggedIn.value = false
    token.value = null
    // 从 localStorage 中移除 token
    localStorage.removeItem('token')
  }

  return {
    isLoggedIn,
    user,
    token,
    showLoginDialog,
    login,
    register,
    logout,
  }
})
