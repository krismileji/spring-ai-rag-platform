import apiClient from './api-client'
import type { VO } from './model'

// 聊天选项
export interface ChatOptionsRequest {
  timeout?: number // 请求超时时间，单位为秒
  temperature?: number // 控制模型的随机性，值越大，模型的随机性越大
  enableSearch?: boolean // 控制模型在生成文本时是否引用和使用互联网搜索结果
  enableThinking?: boolean // 是否启用模型的思维过程
}

// 聊天平台
export interface ChatPlatformVO {
  platform: 'ollama' | 'deepseek' | 'aliyun' // 平台
  existApiKey: boolean // 是否存在 API 密钥
  defaultOptions: ChatOptionsRequest // 默认选项
  enabled: boolean // 是否启用
}

// 聊天平台编辑请求
export interface ChatPlatformEditRequest {
  platform: 'ollama' | 'deepseek' | 'aliyun' // 聊天平台
  apiKey?: string // ApiKey
  enabled?: boolean // 是否启用
  defaultOptions?: ChatOptionsRequest // 默认配置项
}

// 获取聊天平台列表
export const getChatPlatformList = async (): Promise<VO<ChatPlatformVO[]>> => {
  return apiClient.get('/platform/chat/list')
}

// 编辑聊天平台
export const editChatPlatform = async (
  request: ChatPlatformEditRequest,
): Promise<VO<boolean>> => {
  return apiClient.put('/platform/chat/edit', request)
}
