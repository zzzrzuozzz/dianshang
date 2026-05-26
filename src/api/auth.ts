import { post } from '@/utils/request'
import { setToken } from '@/utils/auth'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  tokenType: string
  userId: number
  username: string
  nickname: string
  avatar: string
}

/** POST /api/auth/login */
export async function login(params: LoginParams) {
  const data = await post<LoginResult>('/api/auth/login', params)
  setToken(data.token)
  return data
}
