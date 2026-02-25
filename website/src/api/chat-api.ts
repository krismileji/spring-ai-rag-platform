import { ElMessage } from 'element-plus'
import type { VO } from './model'
import apiClient from './api-client'
import i18n from '../i18n'

export type ModelType = 'CHAT' | 'EMBEDDING'

export interface ModelVO {
  id: string
  platform: string
  platformName: string
  model: string
  modelName: string
  description: string
  sort: number
  enabled?: boolean
}

export interface ModelQuery {
  platform?: string
}

export interface ModelEditRequest {
  id: string
  model: string
  enabled: boolean
}

export interface ChatRequest {
  platform: string
  model: string
  conversationId: string
  message: string
  knowledgeType?: string // 知识库类型，可选：LOCAL(本地知识库)、ALI_BAI_LIAN(云端知识库)
  options?: {
    enableSearch?: boolean
    enableThinking?: boolean
  }
}

export interface ChatStreamCallbacks {
  onMessage: (content: string, reasoningContent?: string) => void
  onError?: (error: Error, userTip?: string) => void
  onComplete?: () => void
}

/**
 * 获取模型列表
 */
export const getModelList = (type: ModelType, query?: ModelQuery): Promise<VO<ModelVO[]>> => {
  return apiClient.get(`/platform/model/${type}/list`, { params: query || {} })
}

/**
 * 编辑模型
 */
export const editModel = (type: ModelType, request: ModelEditRequest): Promise<VO<boolean>> => {
  return apiClient.put(`/platform/model/${type}/edit`, request)
}

/**
 * 刷新模型缓存
 */
export const refreshModels = (type: ModelType, query?: ModelQuery): Promise<VO<ModelVO[]>> => {
  return apiClient.post(`/platform/model/${type}/refresh`, null, { params: query || {} })
}

/**
 * 生成会话ID
 */
export const generateConversationId = (): Promise<VO<string>> => {
  return apiClient.post('/conversation/generate')
}

/**
 * 聊天接口 - SSE 流式响应
 */
export const sendChatMessage = async (
  request: ChatRequest,
  callbacks: ChatStreamCallbacks,
): Promise<void> => {
  const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
  const url = `${baseURL}/chat/message`

  // 获取 token
  const token = localStorage.getItem('token')
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  // 添加语言头
  headers['Accept-Language'] = i18n.global.locale.value

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(request),
    })

    // 检查 HTTP 状态码
    if (!response.ok) {
      // 尝试解析错误响应中的 userTip
      try {
        const errorData = await response.json()
        if (errorData.userTip) {
          ElMessage.error(errorData.userTip)
        } else {
          ElMessage.error(`${i18n.global.t('common.error_request_failed')}: ${response.status}`)
        }
        throw new Error(errorData.userTip || `HTTP error! status: ${response.status}`)
      } catch (parseError) {
        // 如果无法解析响应体，使用默认错误信息
        ElMessage.error(`${i18n.global.t('common.error_network')}: ${response.status}`)
        throw new Error(`HTTP error! status: ${response.status}`)
      }
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()

    if (!reader) {
      throw new Error(i18n.global.t('common.error_no_response_stream'))
    }

    // 处理业务错误的统一函数
    const handleBusinessError = (jsonStr: string): boolean => {
      try {
        const errorData = JSON.parse(jsonStr)
        if (errorData.errorCode && errorData.errorCode !== '00000') {
          // A0301 表示访问未授权，清空登录信息
          if (errorData.errorCode === 'A0301') {
            localStorage.removeItem('token')
            localStorage.removeItem('username')
          }
          const error = new Error(errorData.userTip || errorData.errorMessage || i18n.global.t('common.error_request_failed'))
          callbacks.onError?.(error, errorData.userTip)
          callbacks.onComplete?.()
          return true
        }
      } catch {
        // JSON 解析失败，忽略
      }
      return false
    }

    let buffer = ''
    let hasReceivedData = false

    while (true) {
      const { done, value } = await reader.read()

      if (done) {
        // 流结束时检查 buffer 中是否有未处理的错误响应
        if (!hasReceivedData && buffer.trim()) {
          handleBusinessError(buffer.trim())
        }
        break
      }

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      // 处理每一行数据
      for (const line of lines) {
        // 只去除行尾的换行符和回车符，保留内容中的空格
        const processedLine = line.replace(/[\r\n]+$/, '')
        if (!processedLine) continue

        // 检查是否是 JSON 错误响应（JSON 通常不以空格开头）
        const trimmedForCheck = processedLine.trim()
        if (trimmedForCheck.startsWith('{')) {
          if (handleBusinessError(trimmedForCheck)) return
          continue
        }

        // 处理正常的流式数据（JSON格式）
        let dataContent: string
        if (processedLine.startsWith('data: ')) {
          // 去除 'data: ' 前缀（包括后面的一个空格）
          dataContent = processedLine.slice(6)
        } else if (processedLine.startsWith('data:')) {
          // 去除 'data:' 前缀（没有空格）
          dataContent = processedLine.slice(5)
        } else {
          dataContent = processedLine
        }

        if (dataContent && dataContent !== '[DONE]') {
          try {
            // 解析 JSON 对象
            const jsonData = JSON.parse(dataContent)
            if (jsonData.content !== undefined) {
              hasReceivedData = true
              // JSON.parse会自动处理转义字符，包括\n转换为真正的换行符
              // 所以这里直接使用即可，不需要额外处理
              callbacks.onMessage(jsonData.content, jsonData.reasoningContent)
            }
          } catch (parseError) {
            // JSON 解析失败，记录错误但不中断流程
            console.warn('SSE数据解析失败:', dataContent, parseError)
          }
        }
      }

      // 检查 buffer 中是否有完整的 JSON（无换行符的情况）
      const trimmedBuffer = buffer.trim()
      if (trimmedBuffer.startsWith('{') && trimmedBuffer.endsWith('}')) {
        if (handleBusinessError(trimmedBuffer)) return
      }
    }
  } catch (error) {
    // 网络错误等异常情况
    callbacks.onError?.(error as Error)
    throw error
  }
}
