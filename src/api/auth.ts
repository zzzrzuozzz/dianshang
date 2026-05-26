import { post, get } from '@/utils/request'
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

/** POST /api/auth/login */
export async function login(params: LoginParams) {
  const data = await post<LoginResult>('/api/auth/login', params)
  setToken(data.token)
  setPermissionSession({
    menus: data.menus || [],
    perms: data.perms || [],
    roleKeys: data.roleKeys || [],
  })
  return data
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
