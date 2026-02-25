import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import i18n from '../i18n'

// 配置 axios 基础 URL
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token 并添加到请求头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 添加语言头
    config.headers['Accept-Language'] = i18n.global.locale.value
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => {
    const data = response.data
    // 检查业务错误码，如果不是成功码则提示错误
    if (data?.errorCode && data.errorCode !== '00000') {
      // A0301 表示访问未授权，弹出登录框并清空登录信息
      if (data.errorCode === 'A0301') {
        const authStore = useAuthStore()
        authStore.logout()
        authStore.showLoginDialog = true
      }
      if (data.userTip) {
        ElMessage.error(data.userTip)
      }
      return Promise.reject(data)
    }
    return data
  },
  (error) => {
    console.error('API Error:', error)
    // 统一处理错误响应，使用 userTip 进行提示
    if (error.response?.data?.userTip) {
      ElMessage.error(error.response.data.userTip)
    }
    // A0301 表示访问未授权，弹出登录框并清空登录信息
    if (error.response?.data?.errorCode === 'A0301') {
      const authStore = useAuthStore()
      authStore.logout()
      authStore.showLoginDialog = true
    }
    return Promise.reject(error.response?.data || error)
  },
)

export default apiClient
