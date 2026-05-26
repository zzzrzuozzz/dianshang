import { get } from '@/utils/request'

export type StatsRange = 'week' | 'month'

export interface StatsQuery {
  range?: StatsRange
  startDate?: string
  endDate?: string
}

export interface StatsKpi {
  key: string
  label: string
  value: string
  yesterday: string
  trend: number
  iconBg: string
  iconColor: string
}

export interface NameValue {
  name: string
  value: number
}

export interface TransactionTrend {
  dates: string[]
  paymentAmount: number[]
  refundAmount: number[]
  payUsers: number[]
  payOrders: number[]
  orderRate: number[]
  payRate: number[]
  dealRate: number[]
}

export interface PriceRangeData {
  dates: string[]
  ranges: string[]
  data: number[][]
}

export interface UserSegment {
  payAmount: string
  payAmountTrend: number
  payUsers: number
  payUsersTrend: number
}

export interface SourceDetail {
  name: string
  amount: string
  count: number
  trend: number
}

export interface DeviceSeries {
  dates: string[]
  channels: string[]
  series: number[][]
}

export interface FunnelRates {
  orderRate: number
  payRate: number
  dealRate: number
}

export interface TransactionOverview {
  kpis: StatsKpi[]
  trend: TransactionTrend
  priceRange: PriceRangeData
  funnel: NameValue[]
  userPie: NameValue[]
  newUser: UserSegment
  oldUser: UserSegment
  sourcePie: NameValue[]
  sourceDetail: SourceDetail[]
  deviceSeries: DeviceSeries
  funnelRates: FunnelRates
}

export interface FlowReport {
  kpis: StatsKpi[]
  dates: string[]
  userTrend: Record<string, number[]>
  versionRose: Record<string, NameValue[]>
  pageTrend: Record<string, number[]>
  channelRose: Record<string, NameValue[]>
}

export interface CategoryRow {
  name: string
  qty: number
  qtyRate: string
  amount: string
  amountRate: string
}

export interface ProductCategoryResult {
  list: CategoryRow[]
  pie: NameValue[]
  total: number
  page: number
  pageSize: number
}

export interface ProductRankingRow {
  name: string
  pv: number
  uv: number
  payUsers: number
  conversion: string
  salesQty: number
  salesAmount: string
}

export interface ProductRankingResult {
  list: ProductRankingRow[]
  total: number
  page: number
  pageSize: number
}

function toParams(query?: StatsQuery & { page?: number; pageSize?: number }) {
  const p: Record<string, string | number> = {}
  if (query?.range) p.range = query.range
  if (query?.startDate) p.startDate = query.startDate
  if (query?.endDate) p.endDate = query.endDate
  if (query?.page != null) p.page = query.page
  if (query?.pageSize != null) p.pageSize = query.pageSize
  return p
}

export function fetchTransactionOverview(query?: StatsQuery) {
  return get<TransactionOverview>('/api/stats/transaction/overview', { params: toParams(query) })
}

export function fetchFlowReport(query?: StatsQuery) {
  return get<FlowReport>('/api/stats/flow/report', { params: toParams(query) })
}

export function fetchProductCategory(query?: StatsQuery & { page?: number; pageSize?: number }) {
  return get<ProductCategoryResult>('/api/stats/product/category', { params: toParams(query) })
}

export function fetchProductRanking(query?: StatsQuery & { page?: number; pageSize?: number }) {
  return get<ProductRankingResult>('/api/stats/product/ranking', { params: toParams(query) })
}
