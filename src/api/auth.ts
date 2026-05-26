import { post, get, put } from '@/utils/request'
import { setToken } from '@/utils/auth'
import { setPermissionSession } from '@/utils/permissionStore'
import type { MenuTreeNode } from '@/api/permission'

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
  menus?: MenuTreeNode[]
  roleKeys?: string[]
  perms?: string[]
}

export interface RegisterParams {
  accountType: 'PHONE' | 'EMAIL'
  account: string
  username: string
  password: string
  nickname?: string
  inviteCode: string
}

export interface ForgotPasswordParams {
  accountType: 'PHONE' | 'EMAIL'
  account: string
  inviteCode: string
  newPassword: string
}

function applyAuthSession(data: LoginResult) {
  setToken(data.token)
  setPermissionSession({
    menus: data.menus || [],
    perms: data.perms || [],
    roleKeys: data.roleKeys || [],
  })
}

/** POST /api/auth/login */
export async function login(params: LoginParams) {
  const data = await post<LoginResult>('/api/auth/login', params)
  applyAuthSession(data)
  return data
}

/** POST /api/auth/register */
export async function register(params: RegisterParams) {
  const data = await post<LoginResult>('/api/auth/register', params)
  applyAuthSession(data)
  return data
}

/** POST /api/auth/forgot-password */
export function forgotPassword(params: ForgotPasswordParams) {
  return post<void>('/api/auth/forgot-password', params)
}

/** GET /api/auth/session — 刷新菜单权限 */
export async function refreshAuthSession() {
  const data = await get<Omit<LoginResult, 'token'>>('/api/auth/session')
  setPermissionSession({
    menus: data.menus || [],
    perms: data.perms || [],
    roleKeys: data.roleKeys || [],
  })
  return data
}
