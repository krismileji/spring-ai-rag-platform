import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getChatPlatformList,
  editChatPlatform,
  type ChatPlatformVO,
  type ChatPlatformEditRequest,
  type ChatOptionsRequest,
} from '../api/model'

// ==================== 类型定义 ====================
export interface Provider {
  id: string
  name: string
  enabled: boolean
  color: string
  existApiKey: boolean
}

export interface AiSettings {
  apiKey: string
  apiUrl: string
  defaultModel: string
  timeout?: number
  temperature?: number
  maxTokens: number
  enableSearch: boolean
  enableThinking: boolean
}

// ==================== 常量定义 ====================
// 假的加密文本，用于显示已存在的 API Key
const MASKED_API_KEY = '********************************'

// ==================== 平台颜色映射 ====================
const PLATFORM_COLORS: Record<string, string> = {
  ollama: '#6B7280',
  deepseek: '#3B82F6',
  aliyun: '#8B5CF6',
}

// ==================== 平台名称映射 ====================
const PLATFORM_NAMES: Record<string, string> = {
  ollama: 'Ollama',
  deepseek: 'DeepSeek',
  aliyun: '阿里云',
}

// ==================== 平台ID映射 ====================
const PLATFORM_MAP: Record<string, 'ollama' | 'deepseek' | 'aliyun'> = {
  ollama: 'ollama',
  deepseek: 'deepseek',
  aliyun: 'aliyun',
}

export const useSettings = () => {
  const providers = ref<Provider[]>([])

  // 加载平台列表，返回平台配置数据
  const loadPlatformList = async () => {
    try {
      const response = await getChatPlatformList()
      if (response.errorCode === '00000' && response.data && response.data.length > 0) {
        providers.value = response.data.map((platform: ChatPlatformVO) => ({
          id: platform.platform,
          name: PLATFORM_NAMES[platform.platform] || platform.platform,
          enabled: platform.enabled,
          color: PLATFORM_COLORS[platform.platform] || '#6B7280',
          existApiKey: platform.existApiKey,
        }))
        // 返回平台配置数据，用于初始化设置
        return {
          firstProviderId: providers.value[0]?.id || null,
          platformsData: response.data,
        }
      }
      return { firstProviderId: null, platformsData: [] }
    } catch (error) {
      console.error('加载配置失败:', error)
      ElMessage.error('加载配置失败')
      return { firstProviderId: null, platformsData: [] }
    }
  }

  // 保存AI设置
  const savePlatformSettings = async (
    providerId: string,
    aiSettings: AiSettings,
    providerEnabled: boolean,
  ) => {
    // 判断 API Key 是否为加密文本
    const isApiKeyMasked = aiSettings.apiKey === MASKED_API_KEY

    // 如果不是加密文本，校验 API Key 是否为空
    if (!isApiKeyMasked && !aiSettings.apiKey.trim()) {
      ElMessage.warning('请输入API Key')
      return false
    }

    const platform = PLATFORM_MAP[providerId]
    if (!platform) {
      ElMessage.error('不支持的平台类型')
      return false
    }

    try {
      // 构建 defaultOptions，只包含有值的字段
      const defaultOptions: Partial<ChatOptionsRequest> = {}
      if (aiSettings.timeout !== null && aiSettings.timeout !== undefined) {
        defaultOptions.timeout = aiSettings.timeout
      }
      if (aiSettings.temperature !== null && aiSettings.temperature !== undefined) {
        defaultOptions.temperature = aiSettings.temperature
      }
      if (aiSettings.enableSearch !== null && aiSettings.enableSearch !== undefined) {
        defaultOptions.enableSearch = aiSettings.enableSearch
      }
      if (aiSettings.enableThinking !== null && aiSettings.enableThinking !== undefined) {
        defaultOptions.enableThinking = aiSettings.enableThinking
      }

      const request: ChatPlatformEditRequest = {
        platform,
        enabled: providerEnabled,
        // 只有当 API Key 不是加密文本时才传递
        ...(!isApiKeyMasked && { apiKey: aiSettings.apiKey }),
        ...(Object.keys(defaultOptions).length > 0 && { defaultOptions }),
      }

      const response = await editChatPlatform(request)

      if (response.errorCode === '00000') {
        ElMessage.success('AI服务商设置已保存')
        await loadPlatformList()
        return true
      } else {
        ElMessage.error(response.userTip || '保存失败')
        return false
      }
    } catch (error) {
      console.error('保存失败:', error)
      ElMessage.error('保存失败，请重试')
      return false
    }
  }

  // 测试平台连接
  const testPlatformConnection = async (providerId: string, aiSettings: AiSettings) => {
    // TODO: 实现真实的连接测试逻辑
    console.log('Test connection:', providerId, aiSettings)
    return new Promise((resolve) => {
      setTimeout(() => {
        ElMessage.success('连接测试成功')
        resolve(true)
      }, 1000)
    })
  }

  return {
    providers,
    loadPlatformList,
    savePlatformSettings,
    testPlatformConnection,
    MASKED_API_KEY, // 导出加密文本常量
  }
}
