import { get, post, put } from '@/utils/request'

export interface NoticeItem {
  id: number
  title: string
  content: string
  noticeType: string
  noticeTypeLabel: string
  level: string
  levelLabel: string
  status: number
  sender: string
  createTime: string
}

export interface NoticeUnreadSummary {
  total: number
  systemCount: number
  auditCount: number
  alarmCount: number
  urgentCount: number
}

export interface NoticePageParams {
  noticeType?: string
  page?: number
  pageSize?: number
}

export interface NoticePageResult {
  list: NoticeItem[]
  total: number
  page: number
  pageSize: number
}

/** GET /api/system/notice/unread-list */
export function fetchUnreadNoticeList(limit = 5, urgent = false) {
  return get<NoticeItem[]>('/api/system/notice/unread-list', { params: { limit, urgent } })
}

/** GET /api/system/notice/unread-summary */
export function fetchUnreadNoticeSummary() {
  return get<NoticeUnreadSummary>('/api/system/notice/unread-summary')
}

/** POST /api/system/notice/page */
export function fetchNoticePage(params: NoticePageParams) {
  return post<NoticePageResult>('/api/system/notice/page', params)
}

/** PUT /api/system/notice/read */
export function markNoticesRead(ids: number[]) {
  return put<void>('/api/system/notice/read', { ids })
}

/** PUT /api/system/notice/read-all */
export function markAllNoticesRead(noticeType?: string) {
  return put<void>('/api/system/notice/read-all', noticeType ? { noticeType } : {})
}

/** POST /api/system/notice/delete */
export function deleteNotices(ids: number[]) {
  return post<void>('/api/system/notice/delete', { ids })
}

/** GET /api/system/notice/{id} */
export function fetchNoticeDetail(id: number) {
  return get<NoticeItem>(`/api/system/notice/${id}`)
}
