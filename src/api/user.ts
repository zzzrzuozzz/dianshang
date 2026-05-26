import axios from 'axios'
import { del, get, post } from '@/utils/request'
import { getToken } from '@/utils/auth'

export interface MemberListItem {
  id: string
  nickname: string
  account: string
  level: string
  levelKey: string
  consumeAmount: number
  orderCount: number
  points: number
  growth: number
  status: string
  statusText: string
  remark: string
  registerTime: string
  avatar: string
}

export interface MemberPageResult {
  list: MemberListItem[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

export interface LevelOption {
  label: string
  value: string
}

export interface MemberDetail {
  userId: string
  nickname: string
  avatar: string
  phoneMasked: string
  level: string
  levelKey: string
  statusText: string
  ip: string
  source: string
  tags: string
  birthday: string
  registerTime: string
  city: string
  gender: string
  consumeAmount: number
  orderCount: number
  points: number
  growth: number
  couponCount: number
  reviewCount: number
  returnCount: number
  loginCount: number
  favoriteProducts: number
  favoriteTopics: number
  orderFriends: number
  lotteryCount: number
  remark: string
  account: string
  permissions: string[]
  addresses: { name: string; phone: string; region: string; detail: string }[]
  orders: { id: string; time: string; amount: number; payMethod: string; source: string; status: string }[]
}

export interface MemberUpdatePayload {
  userId: string
  phone?: string
  level?: string
  avatar?: string
  gender?: string
  city?: string[]
  password?: string
  permissions?: string[]
  remark?: string
}

export interface MemberTagItem {
  id: string
  name: string
  memberCount: number
  condition: string
}

export interface MemberTagSavePayload {
  id?: string
  name: string
  gender?: string[]
  memberLevels?: string[]
  cityMode?: string
  regionCodes?: string[][]
  registerEnabled?: boolean
  registerType?: string
  registerRange?: string[]
  registerDays?: number
  orderCountEnabled?: boolean
  orderCountType?: string
  orderCount?: number
  amountEnabled?: boolean
  amountType?: string
  amount?: number
}

export interface MemberLevelItem {
  id: string
  name: string
  isDefault: boolean
  growthPoint: number
  freeShipping: string
  reviewReward: string
  sort: number
  freeShipAmount?: number
  freeShipTimes?: number
  reviewGrowth?: number
  reviewTimes?: number
  privileges?: Record<string, boolean>
}

export interface MemberLevelSavePayload {
  id?: string
  name: string
  growthPoint?: number
  isDefault?: boolean
  freeShipAmount?: number
  freeShipTimes?: number
  reviewGrowth?: number
  reviewTimes?: number
  privileges?: Record<string, boolean>
}

export interface GrowthAdjustPayload {
  userId: string
  type: 'growth' | 'points' | 'settings'
  value?: number
  remark?: string
  level?: string
  points?: number
  growth?: number
}

export interface GrowthBatchPayload {
  userIds: string[]
  adjustType: 'growth' | 'points'
  mode: 'add' | 'set'
  value: number
  remark?: string
}

export interface GrowthTaskItem {
  taskCode: string
  taskName: string
  growthReward: number
  pointsReward: number
  enabled: boolean
  description?: string
  sortNum?: number
}

export interface GrowthRules {
  orderGrowthPerYuan: number
  orderPointsPerYuan: number
  signInGrowth: number
  signInPoints: number
  reviewGrowth: number
  reviewPoints: number
  growthExpireDays: string
  pointsExpireDays: string
}

export interface GrowthRewards {
  registerEnabled: boolean
  registerGrowth: number
  registerPoints: number
  birthdayEnabled: boolean
  birthdayGrowth: number
  birthdayPoints: number
  inviteEnabled: boolean
  inviteGrowth: number
  invitePoints: number
}

export interface LedgerItem {
  ledgerNo: string
  userNo: string
  nickname: string
  account: string
  ledgerType: string
  changeType: string
  changeTypeText: string
  beforeQty: number
  changeQty: number
  afterQty: number
  remark: string
  operatorName: string
  createdAt: string
}

export interface LedgerPageResult {
  list: LedgerItem[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

export function fetchUserList(params: Record<string, unknown>) {
  return get<MemberPageResult>('/api/user/list', { params })
}

export function fetchUserDetail(userNo: string) {
  return get<MemberDetail>(`/api/user/detail/${userNo}`)
}

export function updateUser(data: MemberUpdatePayload) {
  return post<void>('/api/user/update', data)
}

export function fetchLevelOptions() {
  return get<LevelOption[]>('/api/user/level/options')
}

export function fetchTagList(params?: { keyword?: string }) {
  return get<MemberTagItem[]>('/api/user/tag/list', { params })
}

export function fetchTagDetail(tagCode: string) {
  return get<MemberTagSavePayload>(`/api/user/tag/detail/${tagCode}`)
}

export function saveTag(data: MemberTagSavePayload) {
  return post<string>('/api/user/tag/save', data)
}

export function deleteTag(tagCode: string) {
  return del<void>(`/api/user/tag/${tagCode}`)
}

export function fetchTagUsers(params: { tagId: string; page?: number; pageSize?: number }) {
  return get<MemberPageResult>('/api/user/tag/users', { params })
}

export function removeTagUser(tagId: string, userNo: string) {
  return post<void>('/api/user/tag/users/remove', null, { params: { tagId, userNo } })
}

export function fetchLevelList(params?: { name?: string }) {
  return get<MemberLevelItem[]>('/api/user/level/list', { params })
}

export function fetchLevelDetail(levelCode: string) {
  return get<MemberLevelItem>(`/api/user/level/detail/${levelCode}`)
}

export function saveLevel(data: MemberLevelSavePayload) {
  return post<string>('/api/user/level/save', data)
}

export function deleteLevel(levelCode: string) {
  return del<void>(`/api/user/level/${levelCode}`)
}

export function fetchGrowthList(params: Record<string, unknown>) {
  return get<MemberPageResult>('/api/user/growth/list', { params })
}

export function adjustGrowth(data: GrowthAdjustPayload) {
  return post<void>('/api/user/growth/adjust', data)
}

export function batchAdjustGrowth(data: GrowthBatchPayload) {
  return post<void>('/api/user/growth/batch', data)
}

export function fetchGrowthTasks() {
  return get<GrowthTaskItem[]>('/api/user/growth/tasks')
}

export function saveGrowthTasks(tasks: GrowthTaskItem[]) {
  return post<void>('/api/user/growth/tasks/save', tasks)
}

export function fetchGrowthRules() {
  return get<GrowthRules>('/api/user/growth/rules')
}

export function saveGrowthRules(data: GrowthRules) {
  return post<void>('/api/user/growth/rules/save', data)
}

export function fetchGrowthRewards() {
  return get<GrowthRewards>('/api/user/growth/rewards')
}

export function saveGrowthRewards(data: GrowthRewards) {
  return post<void>('/api/user/growth/rewards/save', data)
}

export function fetchLedger(params: Record<string, unknown>) {
  return get<LedgerPageResult>('/api/user/growth/ledger', { params })
}

async function downloadExport(url: string, params: Record<string, string>) {
  const token = getToken()
  const qs = new URLSearchParams(params).toString()
  const res = await axios.get(`${url}?${qs}`, {
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `export_${Date.now()}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
}

export function exportUserList(params: Record<string, string>) {
  return downloadExport('/api/user/export', params)
}

export function exportGrowthList(params: Record<string, string>) {
  return downloadExport('/api/user/growth/export', params)
}
