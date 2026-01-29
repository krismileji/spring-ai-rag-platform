import apiClient from './api-client'
import type { VO } from './model'

// 会话信息
export interface ChatConversationVO {
  id: string
  content: string
  createTime: string
}

// 聊天记录类型
export type ChatMemoryType = 'USER' | 'ASSISTANT' | 'SYSTEM' | 'TOOL'

// 聊天记录
export interface ChatMemoryVO {
  id: number
  mode: string | null // 模型名称
  context: string
  reasoningContent: string | null // 推理内容
  type: ChatMemoryType
  createTime: string
}

/**
 * 查询会话记录
 */
export const getConversations = (): Promise<VO<ChatConversationVO[]>> => {
  return apiClient.get('/chat/history/conversations')
}

/**
 * 查询会话聊天记录
 */
export const getChatMemories = (conversationId: string): Promise<VO<ChatMemoryVO[]>> => {
  return apiClient.get('/chat/history/memories', { params: { conversationId } })
}

/**
 * 删除会话
 */
export const deleteConversation = (conversationId: string): Promise<VO<string>> => {
  return apiClient.delete(`/chat/history/conversation/${conversationId}`)
}
