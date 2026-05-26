import { del, get, post } from '@/utils/request'

export const messageCategories = [
  { label: '秒杀活动', value: 'seckill' },
  { label: '客服消息', value: 'service' },
  { label: '运营消息', value: 'ops' },
  { label: '系统通知', value: 'system' },
]

export const memberLevelOptions = [
  { label: '全部会员', value: 'all' },
  { label: '普通会员', value: 'normal' },
  { label: '黄金会员', value: 'gold' },
  { label: '铂金会员', value: 'platinum' },
  { label: '钻石会员', value: 'diamond' },
]

export const tagGroups = [
  {
    key: 'newUser',
    label: '新老用户标签',
    options: [
      { label: '全部标签', value: 'all' },
      { label: '7天内注册新用户', value: '7d' },
      { label: '15天内注册新用户', value: '15d' },
      { label: '不限', value: 'none' },
    ],
  },
  {
    key: 'firstBuy',
    label: '首购标签',
    options: [
      { label: '全部标签', value: 'all' },
      { label: '7天内首购', value: '7d' },
      { label: '15天内首购', value: '15d' },
      { label: '不限', value: 'none' },
    ],
  },
  {
    key: 'repurchase',
    label: '复购标签',
    options: [
      { label: '全部标签', value: 'all' },
      { label: '7天内购买', value: '7d' },
      { label: '15天内购买', value: '15d' },
      { label: '不限', value: 'none' },
    ],
  },
  {
    key: 'active',
    label: '活跃标签',
    options: [
      { label: '全部标签', value: 'all' },
      { label: '7天内登录', value: '7d' },
      { label: '15天内登录', value: '15d' },
      { label: '不限', value: 'none' },
    ],
  },
]

export const innerLinkOptions = [
  { label: '自定义', value: 'custom' },
  { label: '活动', value: 'activity' },
  { label: '专题', value: 'topic' },
  { label: '商品', value: 'product' },
  { label: '优惠券', value: 'coupon' },
]

export const advTypeOptions = [
  { label: 'APP首页轮播', value: 'carousel' },
  { label: 'APP首页活动', value: 'activity' },
  { label: '猜你喜欢', value: 'guess' },
]

export const systemPushPresets = [
  { label: '富文本', query: { jumpType: 'TEXT' } },
  { label: '内链', query: { jumpType: 'INNER', innerLinkType: 'custom' } },
  { label: '活动', query: { jumpType: 'INNER', innerLinkType: 'activity' } },
  { label: '商品', query: { jumpType: 'INNER', innerLinkType: 'product' } },
  { label: '优惠券', query: { jumpType: 'INNER', innerLinkType: 'coupon' } },
]

export interface PushForm {
  notifyCode?: string
  category: string
  title: string
  intro: string
  jumpType: string
  jumpUrl: string
  innerLinkType: string
  detail: string
  coverImages: string[]
  memberLevels: string[]
  regions: string[][]
  tags: Record<string, string[]>
  sendType: number
  publishTime: string
  generateTypes: string[]
  appPush: boolean
  estimatedUsers: number
  pushMethod: string[]
  stationContent: string
  smsContent: string
}

export function createEmptyPushForm(): PushForm {
  return {
    category: '',
    title: '',
    intro: '',
    jumpType: 'TEXT',
    jumpUrl: '',
    innerLinkType: 'custom',
    detail: '',
    coverImages: [],
    memberLevels: ['all'],
    regions: [],
    tags: { newUser: ['all'], firstBuy: ['all'], repurchase: ['all'], active: ['all'] },
    sendType: 1,
    publishTime: '',
    generateTypes: [],
    appPush: false,
    estimatedUsers: 0,
    pushMethod: ['tag'],
    stationContent: '',
    smsContent: '',
  }
}

export interface NotificationRow {
  id: string
  title: string
  publishStatus: number
  publishStatusText: string
  appPush: string
  publishTime: string
  msgCategory?: string
  msgCategoryText?: string
  pushCount: number
  pushVolume: number
  clickCount?: number
  receiveVolume: number
  pushUser: string
}

export interface NotificationPage {
  list: NotificationRow[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

export interface AdvertisementRow {
  id: string
  name: string
  publishTime: string
  endTime: string
  advType: string
  exposureCount: number
  clickCount: number
  sort: number
  status: number
  statusText: string
}

export interface AdvertisementPage {
  list: AdvertisementRow[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

export function fetchNotificationList(params: {
  msgType: string
  tab?: string
  title?: string
  startDate?: string
  endDate?: string
  page?: number
  pageSize?: number
}) {
  return get<NotificationPage>('/api/ops/notification/list', { params })
}

export function fetchNotificationDetail(notifyCode: string) {
  return get<PushForm & { msgType: string; notifyCode: string }>(`/api/ops/notification/${notifyCode}`)
}

export function saveNotification(data: PushForm & { msgType: string; notifyCode?: string }) {
  return post<string>('/api/ops/notification/save', data)
}

export function estimateAudience(data: {
  memberLevels?: string[]
  regions?: string[][]
  tags?: Record<string, string[]>
}) {
  return post<{ estimatedUsers: number }>('/api/ops/notification/estimate', data)
}

export function resendNotification(notifyCode: string) {
  return post<void>(`/api/ops/notification/${notifyCode}/resend`)
}

export function deleteNotification(notifyCode: string) {
  return del<void>(`/api/ops/notification/${notifyCode}`)
}

export function batchDeleteNotifications(ids: string[]) {
  return post<{ count: number }>('/api/ops/notification/batch/delete', { ids })
}

export function batchResendNotifications(ids: string[]) {
  return post<{ count: number }>('/api/ops/notification/batch/resend', { ids })
}

export function fetchAdvertisementList(params: {
  tab?: string
  title?: string
  startDate?: string
  endDate?: string
  page?: number
  pageSize?: number
}) {
  return get<AdvertisementPage>('/api/ops/advertisement/list', { params })
}

export function fetchAdvertisementDetail(advCode: string) {
  return get<{
    advCode: string
    category: string
    title: string
    intro: string
    jumpType: string
    jumpUrl: string
    detail: string
    coverImages: string[]
    memberLevels: string[]
    regions: string[][]
    tags: Record<string, string[]>
    startTime: string
    endTime: string
    online: boolean
    appPush: boolean
    estimatedUsers: number
  }>(`/api/ops/advertisement/${advCode}`)
}

export function saveAdvertisement(data: Record<string, unknown>) {
  return post<string>('/api/ops/advertisement/save', data)
}

export function toggleAdvertisementOnline(advCode: string, online: boolean) {
  return post<void>(`/api/ops/advertisement/${advCode}/online`, { online })
}

export function pinAdvertisement(advCode: string) {
  return post<void>(`/api/ops/advertisement/${advCode}/pin`)
}

export function deleteAdvertisement(advCode: string) {
  return del<void>(`/api/ops/advertisement/${advCode}`)
}
