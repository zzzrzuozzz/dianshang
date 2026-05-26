import { del, get, post } from '@/utils/request'

export const seckillStatusMap: Record<string, { label: string; type: string }> = {
  pending: { label: '待开始', type: 'primary' },
  active: { label: '进行中', type: '' },
  offline: { label: '已下架', type: 'danger' },
  ended: { label: '已结束', type: 'danger' },
}

export const couponTypeOptions = [
  { label: '新人券', value: 'newcomer' },
  { label: '购物赠券', value: 'shopping' },
  { label: '会员赠券', value: 'member' },
  { label: '全场赠券', value: 'sitewide' },
]

export const couponScopeOptions = [
  { label: '全场通用', value: 'all' },
  { label: '指定商品', value: 'product' },
  { label: '指定分类', value: 'category' },
]

export interface PromoActivity {
  id: string
  title: string
  status: string
  startTime: string
  endTime: string
  warning: string
  online: boolean
}

export interface PromoActivityPage {
  list: PromoActivity[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

export interface TimeSlot {
  id: string
  name: string
  start: string
  end: string
  enabled: boolean
}

export interface SeckillSkuItem {
  id?: string
  name: string
  productCode: string
  price: number
  seckillPrice: number
  seckillQty: number
  remainStock: number
  totalStock: number
  warningStock: number
  limitQty: number
  sort: number
}

export interface GroupBuySkuItem {
  id?: string
  name: string
  productCode: string
  price: number
  groupPrice: number
  groupSize: number
  groupQty: number
  remainStock: number
  totalStock: number
  warningStock: number
  limitQty: number
  sort: number
  attrs?: Record<string, unknown>
}

export interface ProductPickerItem {
  id: string
  name: string
  price: number
  stock: number
}

export interface CouponItem {
  id: string
  name: string
  type: string
  typeLabel: string
  products: string
  threshold: string
  faceValue: number
  issueQty: string
  claimed: number
  used: number
  platform: string
  validity: string
  timeRange: string
  status: string
  statusLabel: string
  online: boolean
  scopeType?: string
}

export interface CouponPage {
  list: CouponItem[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

export interface CouponDetail {
  id: string
  name: string
  type: string
  products: string
  threshold: string
  faceValue: number
  status: string
  timeRange: string
  validity: string
  totalIssue: number
  remain: number
  claimed: number
  used: number
  pending: number
  expired: number
  scopeType: string
  productIds?: string[]
  categoryIds?: string[]
  online: boolean
  couponType?: string
  thresholdAmount?: number
  startTime?: string
  endTime?: string
  issueQty?: number
  platform?: string
}

export interface CouponHistoryItem {
  couponId: string
  member: string
  method: string
  claimTime: string
  status: string
  useTime: string
  orderId: string
}

export function fetchProductPicker(keyword?: string) {
  return get<ProductPickerItem[]>('/api/promotion/products/picker', { params: { keyword } })
}

export function fetchSeckillList(params: Record<string, unknown>) {
  return get<PromoActivityPage>('/api/promotion/seckill/list', { params })
}

export function saveSeckill(data: Record<string, unknown>) {
  return post<string>('/api/promotion/seckill/save', data)
}

export function toggleSeckillOnline(activityCode: string, online: boolean) {
  return post<void>(`/api/promotion/seckill/${activityCode}/online`, { online })
}

export function deleteSeckill(activityCode: string) {
  return del<void>(`/api/promotion/seckill/${activityCode}`)
}

export function fetchSeckillTimeList() {
  return get<TimeSlot[]>('/api/promotion/seckill/time/list')
}

export function saveSeckillTime(data: Record<string, unknown>) {
  return post<string>('/api/promotion/seckill/time/save', data)
}

export function toggleSeckillTime(slotCode: string, enabled: boolean) {
  return post<void>(`/api/promotion/seckill/time/${slotCode}/enabled`, { enabled })
}

export function deleteSeckillTime(slotCode: string) {
  return del<void>(`/api/promotion/seckill/time/${slotCode}`)
}

export function fetchSeckillActivitySlots(activityCode: string) {
  return get<TimeSlot[]>(`/api/promotion/seckill/${activityCode}/slots`)
}

export function fetchSeckillSkuList(activityCode: string, slotCode: string) {
  return get<SeckillSkuItem[]>(`/api/promotion/seckill/${activityCode}/sku/${slotCode}`)
}

export function saveSeckillSku(data: { activityId: string; timeId: string; items: SeckillSkuItem[] }) {
  return post<void>('/api/promotion/seckill/sku/save', data)
}

export function fetchGroupBuyList(params: Record<string, unknown>) {
  return get<PromoActivityPage>('/api/promotion/group-buy/list', { params })
}

export function saveGroupBuy(data: Record<string, unknown>) {
  return post<string>('/api/promotion/group-buy/save', data)
}

export function toggleGroupBuyOnline(activityCode: string, online: boolean) {
  return post<void>(`/api/promotion/group-buy/${activityCode}/online`, { online })
}

export function deleteGroupBuy(activityCode: string) {
  return del<void>(`/api/promotion/group-buy/${activityCode}`)
}

export function fetchGroupBuyTimeList(activityCode: string) {
  return get<TimeSlot[]>(`/api/promotion/group-buy/${activityCode}/time/list`)
}

export function saveGroupBuyTime(data: Record<string, unknown>) {
  return post<string>('/api/promotion/group-buy/time/save', data)
}

export function toggleGroupBuyTime(activityCode: string, slotCode: string, enabled: boolean) {
  return post<void>(`/api/promotion/group-buy/${activityCode}/time/${slotCode}/enabled`, { enabled })
}

export function deleteGroupBuyTime(activityCode: string, slotCode: string) {
  return del<void>(`/api/promotion/group-buy/${activityCode}/time/${slotCode}`)
}

export function fetchGroupBuySkuList(activityCode: string, slotCode: string) {
  return get<GroupBuySkuItem[]>(`/api/promotion/group-buy/${activityCode}/sku/${slotCode}`)
}

export function saveGroupBuySku(data: { activityId: string; timeId: string; items: GroupBuySkuItem[] }) {
  return post<void>('/api/promotion/group-buy/sku/save', data)
}

export function fetchCouponList(params: Record<string, unknown>) {
  return get<CouponPage>('/api/promotion/coupon/list', { params })
}

export function fetchCouponDetail(couponCode: string) {
  return get<CouponDetail>(`/api/promotion/coupon/${couponCode}`)
}

export function saveCoupon(data: Record<string, unknown>) {
  return post<string>('/api/promotion/coupon/save', data)
}

export function toggleCouponOnline(couponCode: string, online: boolean) {
  return post<void>(`/api/promotion/coupon/${couponCode}/online`, { online })
}

export function deleteCoupon(couponCode: string) {
  return del<void>(`/api/promotion/coupon/${couponCode}`)
}

export function fetchCouponHistory(couponCode: string, params?: Record<string, unknown>) {
  return get<CouponHistoryItem[]>(`/api/promotion/coupon/${couponCode}/history`, { params })
}
