import { post } from '@/utils/request'

export interface InviteCodeItem {
  id: number
  code: string
  roleId: number
  roleName: string
  used: number
  usedLabel: string
  usedByAdminId?: number
  usedByUsername?: string
  usedAccount?: string
  usedAccountType?: string
  usedTime?: string
  createTime: string
  remark?: string
}

export interface InviteCodePageResult {
  list: InviteCodeItem[]
  total: number
  page: number
  pageSize: number
}

/** POST /api/system/invite-code/generate */
export function generateInviteCodes(data: { roleId: number; count: number; remark?: string }) {
  return post<string[]>('/api/system/invite-code/generate', data)
}

/** POST /api/system/invite-code/page */
export function fetchInviteCodePage(params: { used?: number | null; page?: number; pageSize?: number }) {
  return post<InviteCodePageResult>('/api/system/invite-code/page', params)
}
