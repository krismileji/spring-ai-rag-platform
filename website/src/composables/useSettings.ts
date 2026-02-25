import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
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
// 使用 computed 在组件内部获取，这里仅保留 key 映射
const PLATFORM_NAME_KEYS: Record<string, string> = {
  ollama: 'settings.platform.ollama',
  deepseek: 'settings.platform.deepseek',
  aliyun: 'settings.platform.aliyun',
}

// ==================== 平台ID映射 ====================
const PLATFORM_MAP: Record<string, 'ollama' | 'deepseek' | 'aliyun'> = {
  ollama: 'ollama',
  deepseek: 'deepseek',
  aliyun: 'aliyun',
}

export const useSettings = () => {
  const { t } = useI18n()
  const providers = ref<Provider[]>([])

  // 加载平台列表，返回平台配置数据
  const loadPlatformList = async () => {
    // 检查是否已登录，未登录不调用接口
    const token = localStorage.getItem('token')
    if (!token) {
      return { firstProviderId: null, platformsData: [] }
    }

    try {
      const response = await getChatPlatformList()
      if (response.errorCode === '00000' && response.data && response.data.length > 0) {
        providers.value = response.data.map((platform: ChatPlatformVO) => ({
          id: platform.platform,
          name: PLATFORM_NAME_KEYS[platform.platform] ? t(PLATFORM_NAME_KEYS[platform.platform]!) : platform.platform,
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
      ElMessage.error(t('settings.message.load_config_failed'))
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
      ElMessage.warning(t('settings.message.api_key_required'))
      return false
    }

    const platform = PLATFORM_MAP[providerId]
    if (!platform) {
      ElMessage.error(t('settings.message.platform_invalid'))
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

      // 构造请求参数
      const payload: ChatPlatformEditRequest = {
        platform,
        apiKey: isApiKeyMasked ? undefined : aiSettings.apiKey, // 如果是加密文本，不传 apiKey
        defaultOptions: {
          ...defaultOptions,
          // models 字段在 ChatPlatformEditRequest 中未定义，暂时移除
          // 后端接口似乎不支持通过此接口更新模型列表，如果需要更新模型列表，需要确认后端接口定义
        },
        enabled: providerEnabled,
      }

      const response = await editChatPlatform(payload)

      if (response.errorCode === '00000') {
        ElMessage.success(t('settings.message.save_success'))
        await loadPlatformList()
        return true
      }

      ElMessage.error(response.userTip || t('settings.message.save_failed'))
      return false
    } catch (error) {
      console.error('保存失败:', error)
      ElMessage.error(t('settings.message.save_failed'))
      return false
    }
  }

  // 测试平台连接
  const testPlatformConnection = async (providerId: string, aiSettings: AiSettings) => {
    // TODO: 实现真实的连接测试逻辑
    console.log('Test connection:', providerId, aiSettings)
    return new Promise((resolve) => {
      setTimeout(() => {
        ElMessage.success(t('settings.message.test_connection_success'))
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
