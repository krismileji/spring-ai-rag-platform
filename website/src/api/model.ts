// 统一响应结构 VO
export interface VO<T> {
  errorCode: string
  errorMessage: string
  userTip: string
  data: T
  page?: PageVO
}

// 分页信息
export interface PageVO {
  pageNo: number
  pageSize: number
  totalCount: number
}

// 导出所有 API 接口
export * from './api-client'
export * from './auth-api'
export * from './chat-api'
export * from './history-api'
export * from './knowledge-api'
export * from './platform-api'
