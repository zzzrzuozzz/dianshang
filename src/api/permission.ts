import { get, post, put, del } from '@/utils/request'

export interface MenuTreeNode {
  id: number
  parentId?: number
  menuName: string
  menuType: 'M' | 'C' | 'F'
  path?: string
  perms?: string
  icon?: string
  sortNum?: number
  children?: MenuTreeNode[]
}

export interface MenuSavePayload {
  id?: number
  parentId?: number
  menuName: string
  menuType: string
  path?: string
  perms?: string
  icon?: string
  sortNum?: number
}

export interface RoleItem {
  id: number
  roleName: string
  roleKey: string
  sortNum?: number
  status: number
  remark?: string
  createTime?: string
}

export interface RoleSavePayload {
  id?: number
  roleName: string
  roleKey: string
  sortNum?: number
  status?: number
  remark?: string
}

export interface AdminItem {
  id: number
  username: string
  nickname?: string
  status: number
  createTime?: string
  roles: { id: number; roleName: string; roleKey: string }[]
}

export interface AdminSavePayload {
  id?: number
  username: string
  password?: string
  nickname?: string
  status?: number
  roleIds?: number[]
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

/** GET /api/system/menu/tree */
export function fetchMenuTree() {
  return get<MenuTreeNode[]>('/api/system/menu/tree')
}

export function saveMenu(data: MenuSavePayload) {
  return post<void>('/api/system/menu/save', data)
}

export function deleteMenu(id: number) {
  return del<void>(`/api/system/menu/${id}`)
}

/** POST /api/system/role/page */
export function fetchRolePage(query: { keyword?: string; status?: number; page: number; pageSize: number }) {
  return post<PageResult<RoleItem>>('/api/system/role/page', query)
}

export function fetchRoleOptions() {
  return get<{ id: number; roleName: string; roleKey: string }[]>('/api/system/role/options')
}

export function fetchRoleMenuIds(roleId: number) {
  return get<number[]>(`/api/system/role/${roleId}/menu-ids`)
}

export function saveRole(data: RoleSavePayload) {
  return post<void>('/api/system/role/save', data)
}

export function updateRoleStatus(id: number, status: number) {
  return put<void>(`/api/system/role/${id}/status`, { status })
}

/** 事务内先清空再批量插入，防主键冲突 */
export function saveRolePermissions(roleId: number, menuIds: number[]) {
  return post<void>('/api/system/role/save-permissions', { roleId, menuIds })
}

export function deleteRole(id: number) {
  return del<void>(`/api/system/role/${id}`)
}

/** POST /api/system/admin/page */
export function fetchAdminPage(query: { username?: string; nickname?: string; page: number; pageSize: number }) {
  return post<PageResult<AdminItem>>('/api/system/admin/page', query)
}

export function saveAdmin(data: AdminSavePayload) {
  return post<void>('/api/system/admin/save', data)
}

export function updateAdminStatus(id: number, status: number) {
  return put<void>(`/api/system/admin/${id}/status`, { status })
}
