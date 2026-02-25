import apiClient from './api-client'
import type { VO } from './model'
import type { ModelVO } from './chat-api'

// 知识库信息
export interface KnowledgeVO {
  id: number
  name: string
  description: string
}

// 新增/编辑知识库请求参数
export interface KnowledgeAddEditRequest {
  id?: number
  name: string
  description?: string
}

// 知识库文件信息
export interface KnowledgeFileVO {
  id: string
  fileName: string
  description: string
  relEmbeddingModel?: ModelVO
  createTime: string
}

// 知识库文件详情
export interface KnowledgeFileDetailVO {
  id?: string
  content: string
  metaData: Record<string, unknown>
}

// 知识库文件上传响应
export interface KnowledgeFileUploadVO {
  id: string
  fileName: string
  description: string
  embeddingModelId?: string
  details: KnowledgeFileDetailVO[]
}

// 知识库文件详情添加请求
export interface KnowledgeFileDetailAddRequest {
  id?: string // 文档 ID，新增详情时为空
  content: string
  metaData?: Record<string, unknown>
}

// 知识库文件添加请求
export interface KnowledgeFileAddRequest {
  id: string // 文件 ID
  description?: string
  embeddingModelId?: string
  details: KnowledgeFileDetailAddRequest[]
}

/**
 * 查询知识库列表
 */
export const getKnowledgeList = (): Promise<VO<KnowledgeVO[]>> => {
  return apiClient.get('/knowledge/list')
}

/**
 * 校验知识库名称
 */
export const checkKnowledgeName = (name: string): Promise<VO<string | null>> => {
  return apiClient.get('/knowledge/checkName', { params: { name } })
}

/**
 * 新增/编辑知识库
 */
export const addEditKnowledge = (request: KnowledgeAddEditRequest): Promise<VO<boolean>> => {
  return apiClient.post('/knowledge/addEdit', request)
}

/**
 * 删除知识库
 */
export const deleteKnowledge = (id: string): Promise<VO<boolean>> => {
  return apiClient.delete(`/knowledge/delete/${id}`)
}

/**
 * 上传知识库文件
 */
export const uploadKnowledgeFile = (
  knowledgeId: string,
  files: File[],
): Promise<VO<KnowledgeFileUploadVO[]>> => {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('files', file)
  })
  return apiClient.post(`/knowledge/file/uploadFile/${knowledgeId}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

/**
 * 保存知识库文件（支持多文件）
 */
export const addKnowledgeFile = (
  knowledgeId: string,
  requests: KnowledgeFileAddRequest[],
): Promise<VO<string[]>> => {
  return apiClient.put(`/knowledge/file/add/${knowledgeId}`, requests)
}

/**
 * 查询知识库文件列表
 */
export const getKnowledgeFileList = (knowledgeId: string): Promise<VO<KnowledgeFileVO[]>> => {
  return apiClient.get(`/knowledge/file/list/${knowledgeId}`)
}

/**
 * 查询文件详情
 */
export const getFileDetail = (fileId: string): Promise<VO<KnowledgeFileDetailVO[]>> => {
  return apiClient.get(`/knowledge/file/listFileDetails/${fileId}`)
}

/**
 * 删除知识库文件
 */
export const deleteKnowledgeFile = (fileId: string): Promise<VO<boolean>> => {
  return apiClient.delete(`/knowledge/file/del/${fileId}`)
}
