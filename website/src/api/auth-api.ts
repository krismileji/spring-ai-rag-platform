import apiClient from './api-client'
import type { VO } from './model'

export interface RegisterByUsernameRequest {
  username: string
  password: string
}

export interface LoginByUsernameRequest {
  username: string
  password: string
}

/**
 * 校验用户名
 */
export const checkUsername = (username: string): Promise<VO<boolean>> => {
  return apiClient.get('/register/checkUsername', { params: { username } })
}

/**
 * 根据用户名注册
 */
export const registerByUsername = (request: RegisterByUsernameRequest): Promise<VO<string>> => {
  return apiClient.post('/register/username', request)
}

/**
 * 根据用户名登录
 */
export const loginByUsername = (request: LoginByUsernameRequest): Promise<VO<string>> => {
  return apiClient.post('/login/username', request)
}
