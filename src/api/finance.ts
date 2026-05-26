import axios from 'axios'
import { get, post, put } from '@/utils/request'
import { getToken } from '@/utils/auth'

export interface FinanceKpi {
  key: string
  label: string
  value: string
  display: string
  trend: number
  tag: string
  tagType: string
  iconKey: string
  iconBg: string
  iconColor: string
}

export interface FinanceChart {
  granularity: string
  dates: string[]
  incomeSeries: number[]
  refundSeries: number[]
}

export interface WithdrawBrief {
  applyNo: string
  memberName: string
  applyAmount: string
  createTime: string
}

export interface FinanceOverview {
  kpis: FinanceKpi[]
  chart: FinanceChart
  pendingWithdraws: WithdrawBrief[]
}

export interface TransactionRecord {
  recordNo: string
  orderNo: string
  tradeType: string
  tradeTypeLabel: string
  amount: string
  amountDisplay: string
  paymentChannel: string
  paymentChannelLabel: string
  status: number
  statusLabel: string
  createTime: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface StatementQuery {
  keyword?: string
  tradeType?: string
  paymentChannel?: string
  startTime?: string
  endTime?: string
  page?: number
  pageSize?: number
}

export interface WithdrawApply {
  applyNo: string
  userId: number
  userNo: string
  nickname: string
  avatar: string
  shopName: string
  applyAmount: string
  feeAmount: string
  actualAmount: string
  accountType: string
  accountNo: string
  bankName: string
  holderName: string
  verifyStatus: number
  verifyStatusLabel: string
  verifyUser: string
  verifyTime: string
  remark: string
  createTime: string
}

export interface WithdrawQuery {
  tab?: string
  keyword?: string
  page?: number
  pageSize?: number
}

/** GET /api/finance/overview */
export function fetchFinanceOverview(granularity: 'day' | 'month' = 'day') {
  return get<FinanceOverview>('/api/finance/overview', { params: { granularity } })
}

/** POST /api/finance/statement/page */
export function fetchStatementPage(query: StatementQuery) {
  return post<PageResult<TransactionRecord>>('/api/finance/statement/page', query)
}

/**
 * POST /api/finance/statement/export
 * 后端按 create_time 索引范围查询并流式输出 CSV，避免一次性全表加载 OOM
 */
export async function exportFinanceStatement(query: StatementQuery) {
  const res = await axios.post('/api/finance/statement/export', query, {
    responseType: 'blob',
    headers: { Authorization: `Bearer ${getToken()}` },
  })
  const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `finance_statement_${Date.now()}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

/** POST /api/finance/withdraw/page */
export function fetchWithdrawPage(query: WithdrawQuery) {
  return post<PageResult<WithdrawApply>>('/api/finance/withdraw/page', query)
}

/** PUT /api/finance/withdraw/verify */
export function verifyWithdraw(payload: { applyNo: string; passed: boolean; remark?: string }) {
  return put('/api/finance/withdraw/verify', payload)
}

/** POST /api/finance/reconcile 全库对账，同步订单流水与首页指标 */
export function reconcileFinance() {
  return post<{ transactionCount: number; pendingWithdrawCount: number }>('/api/finance/reconcile')
}
