import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  generateConversationId,
  sendChatMessage,
  getConversations,
  getChatMemories,
  deleteConversation,
  type ChatRequest,
  type ChatConversationVO,
  type ChatMemoryVO,
} from '../api/model'
import { ElMessage } from 'element-plus'

export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
  model?: string // 模型名称，用户消息使用上一条AI回复的模型，AI回复从接口获取
  isLoading?: boolean // 是否正在加载中
  reasoningContent?: string // AI思考过程（仅在启用thinking模式时有值）
  isThinking?: boolean // 是否正在思考中
  thinkingStartTime?: number // 思考开始时间
  thinkingDuration?: number // 思考耗时（秒）
}

export interface Session {
  id: string
  conversationId?: string // 后端会话ID
  title: string
  messages: Message[]
  lastMessage?: string
  lastTime?: number
}

export const useChatStore = defineStore('chat', () => {
  const { t } = useI18n()
  const sessions = ref<Session[]>([])

  const currentSessionId = ref<string>('')

  // 初始化标记
  const initialized = ref(false)

  // 加载状态
  const isLoading = ref(false)

  // 当前选中的模型信息
  const selectedModel = ref<{ platform: string; model: string }>({
    platform: '',
    model: '',
  })

  // 当前知识库类型
  const knowledgeType = ref<string>('')

  const currentSession = computed(() => {
    return sessions.value.find((s) => s.id === currentSessionId.value)
  })

  const currentMessages = computed(() => {
    return currentSession.value?.messages || []
  })

  // 初始化store，创建首个会话
  const initialize = async () => {
    if (initialized.value) return
    initialized.value = true
    isLoading.value = true

    try {
      // 加载会话列表
      await loadSessions()

      // 如果没有会话，创建第一个
      if (sessions.value.length === 0) {
        await createSession()
      }
    } finally {
      isLoading.value = false
    }
  }

  // 加载会话列表
  const loadSessions = async () => {
    // 检查是否有 token
    const token = localStorage.getItem('token')
    if (!token) {
      console.log('未登录，跳过加载会话列表')
      return
    }

    try {
      const response = await getConversations()
      if (response.errorCode !== '00000') {
        return
      }

      // 将接口返回的会话转换为本地会话格式
      sessions.value = response.data.map((conv: ChatConversationVO) => ({
        id: conv.id,
        conversationId: conv.id,
        title: conv.content || `${t('chat.session.title_prefix')} ${conv.id}`,
        messages: [],
        lastMessage: conv.content,
        lastTime: new Date(conv.createTime).getTime(),
      }))

      // 设置当前会话为第一个
      if (sessions.value.length > 0 && sessions.value[0]) {
        currentSessionId.value = sessions.value[0].id
        // 加载第一个会话的聊天记录
        await loadChatMemories(sessions.value[0].id)
      }
    } catch (error) {
      console.error('加载会话列表失败:', error)
    }
  }

  // 加载会话聊天记录
  const loadChatMemories = async (sessionId: string) => {
    const session = sessions.value.find((s) => s.id === sessionId)
    if (!session || !session.conversationId) {
      return
    }

    try {
      const response = await getChatMemories(session.conversationId)
      if (response.errorCode !== '00000') {
        return
      }

      // 将接口返回的聊天记录转换为消息格式
      const memories = response.data || []
      session.messages = memories.map((memory: ChatMemoryVO, index: number) => {
        let model: string | undefined = undefined

        if (memory.type === 'USER') {
          // 用户消息使用自己的 mode 字段
          model = memory.mode && memory.mode !== '' ? memory.mode : undefined
        } else if (memory.type === 'ASSISTANT') {
          // AI 消息查找最近的上一条用户消息的 mode
          for (let i = index - 1; i >= 0; i--) {
            const prevMemory = memories[i]
            if (prevMemory && prevMemory.type === 'USER' && prevMemory.mode && prevMemory.mode !== '') {
              model = prevMemory.mode
              break
            }
          }
        }

        return {
          id: memory.id.toString(),
          role: memory.type === 'USER' ? 'user' : 'assistant',
          content: memory.context || '',
          timestamp: new Date(memory.createTime).getTime(),
          model,
          reasoningContent: memory.reasoningContent && memory.reasoningContent !== '' ? memory.reasoningContent : undefined,
        }
      })
    } catch (error) {
      console.error('加载聊天记录失败:', error)
    }
  }

  // 创建新会话
  const createSession = async () => {
    try {
      // 调用接口生成会话ID
      const response = await generateConversationId()
      if (response.errorCode !== '00000') {
        // 错误已在axios拦截器中统一处理
        return
      }

      const conversationId = response.data
      const newSession: Session = {
        id: Date.now().toString(),
        conversationId, // 保存后端返回的会话ID
        title: `${t('chat.session.new_prefix')} ${sessions.value.length + 1}`,
        messages: [],
        lastMessage: t('chat.session.start_chat'),
        lastTime: Date.now(),
      }
      sessions.value.unshift(newSession)
      currentSessionId.value = newSession.id
    } catch (error) {
      console.error('创建会话失败:', error)
      // 错误已在axios拦截器中统一处理
    }
  }

  // 切换会话
  const switchSession = async (sessionId: string) => {
    currentSessionId.value = sessionId
    // 加载该会话的聊天记录
    await loadChatMemories(sessionId)
  }

  // 删除会话
  const deleteSession = async (sessionId: string) => {
    const session = sessions.value.find((s) => s.id === sessionId)
    if (!session || !session.conversationId) {
      return
    }

    try {
      // 调用删除会话接口
      const response = await deleteConversation(session.conversationId)
      if (response.errorCode !== '00000') {
        return
      }

      // 删除本地会话
      const index = sessions.value.findIndex((s) => s.id === sessionId)
      if (index !== -1) {
        sessions.value.splice(index, 1)
        // 如果删除的是当前会话，清空当前会话ID或切换到其他会话
        if (currentSessionId.value === sessionId) {
          if (sessions.value.length > 0) {
            currentSessionId.value = sessions.value[0]?.id || ''
            // 加载新选中会话的聊天记录
            await loadChatMemories(currentSessionId.value)
          } else {
            // 没有会话了，清空当前会话ID
            currentSessionId.value = ''
          }
        }
      }
    } catch (error) {
      console.error('删除会话失败:', error)
    }
  }

  // 设置当前选中的模型
  const setSelectedModel = (platform: string, model: string) => {
    selectedModel.value = { platform, model }
  }

  // 设置知识库类型
  const setKnowledgeType = (type: string) => {
    knowledgeType.value = type
  }

  // 发送消息
  const sendMessage = async (content: string, enableThinking: boolean = false) => {
    if (!currentSession.value) {
      ElMessage.warning(t('chat.message.select_session_warning'))
      return
    }

    if (!currentSession.value.conversationId) {
      ElMessage.error(t('chat.message.session_id_missing'))
      return
    }

    if (!selectedModel.value.platform || !selectedModel.value.model) {
      ElMessage.warning(t('chat.message.select_model_warning'))
      return
    }

    // 添加用户消息，使用当前选中的模型
    const userMessage: Message = {
      id: Date.now().toString(),
      role: 'user',
      content,
      timestamp: Date.now(),
      model: selectedModel.value.model,
    }

    currentSession.value.messages.push(userMessage)

    // 如果是第一条消息，更新 title 和 lastMessage
    if (currentSession.value.messages.length === 1) {
      currentSession.value.title = content.length > 20 ? content.substring(0, 20) + '...' : content
      currentSession.value.lastMessage = content
    }

    currentSession.value.lastTime = Date.now()
    console.log('[Chat Store] 用户消息已添加:', userMessage)
    console.log('[Chat Store] 当前消息列表:', currentSession.value.messages)

    // 创建 AI 消息占位符，使用用户消息的模型
    const aiMessageId = (Date.now() + 1).toString()
    const requestStartTime = Date.now() // 记录请求开始时间
    const aiMessage: Message = {
      id: aiMessageId,
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      model: selectedModel.value.model,
      isLoading: true, // 初始状态为加载中
      isThinking: enableThinking, // 如果启用思考模式，标记为思考中
      thinkingStartTime: enableThinking ? requestStartTime : undefined,
    }
    currentSession.value.messages.push(aiMessage)
    console.log('[Chat Store] AI消息占位符已添加:', aiMessage)
    console.log('[Chat Store] 当前消息列表:', currentSession.value.messages)

    // 调用聊天接口
    const chatRequest: ChatRequest = {
      platform: selectedModel.value.platform,
      model: selectedModel.value.model,
      conversationId: currentSession.value.conversationId,
      message: content,
      knowledgeType: knowledgeType.value && knowledgeType.value !== '' ? knowledgeType.value : undefined, // 当知识库类型为空字符串时不传递
      options: {
        enableThinking,
      },
    }

    try {
      await sendChatMessage(chatRequest, {
        onMessage: (chunk: string, reasoningChunk?: string) => {
          if (currentSession.value) {
            const message = currentSession.value.messages.find((m) => m.id === aiMessageId)
            if (message) {
              // 处理 reasoningContent(思考过程)
              if (reasoningChunk !== undefined && reasoningChunk !== null && reasoningChunk !== '') {
                if (!message.reasoningContent) {
                  message.reasoningContent = ''
                }
                message.reasoningContent += reasoningChunk
                // 思考过程中,保持思考状态
                message.isThinking = true
                message.isLoading = false // 收到思考内容后取消加载状态
              }

              // 处理 content(正式回复)
              if (chunk) {
                // 收到第一条 content 时,说明思考已结束
                if (message.isThinking && !message.content) {
                  message.isThinking = false
                  if (message.thinkingStartTime) {
                    message.thinkingDuration = Math.round((Date.now() - message.thinkingStartTime) / 1000)
                  }
                }
                message.isLoading = false // 收到第一条响应后取消加载状态
                message.content += chunk
              }

              // lastMessage 保持为接口返回的初始消息,聊天时不更新
              // currentSession.value.lastMessage = message.content
              currentSession.value.lastTime = Date.now()
            }
          }
        },
        onError: (error: Error, userTip?: string) => {
          console.error('聊天错误:', error)
          console.log('[Chat Store] 接收到 onError 回调, userTip:', userTip)
          // 将错误信息显示为AI回复消息
          if (currentSession.value) {
            const message = currentSession.value.messages.find((m) => m.id === aiMessageId)
            if (message && userTip) {
              // 将 userTip 填充到 AI 消息内容中
              message.content = userTip
              console.log('[Chat Store] AI消息内容已更新:', message)
              console.log('[Chat Store] 当前消息列表:', currentSession.value.messages)
              // lastMessage 保持为接口返回的初始消息，聊天时不更新
              currentSession.value.lastTime = Date.now()
            } else if (message) {
              // 如果没有 userTip，删除 AI 消息
              const index = currentSession.value.messages.findIndex((m) => m.id === aiMessageId)
              if (index !== -1) {
                currentSession.value.messages.splice(index, 1)
              }
              // lastMessage 保持为接口返回的初始消息，聊天时不更新
            }
          }
        },
        onComplete: () => {
          console.log('聊天完成')
          // 确保加载状态被清除
          if (currentSession.value) {
            const message = currentSession.value.messages.find((m) => m.id === aiMessageId)
            if (message) {
              message.isLoading = false
            }
          }
        },
      })
    } catch (error) {
      console.error('发送消息失败:', error)
    }
  }

  return {
    sessions,
    currentSessionId,
    currentSession,
    currentMessages,
    selectedModel,
    knowledgeType,
    isLoading,
    initialize,
    loadSessions,
    loadChatMemories,
    createSession,
    switchSession,
    deleteSession,
    setSelectedModel,
    setKnowledgeType,
    sendMessage,
  }
})
