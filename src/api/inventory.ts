import axios from 'axios'
import { del, get, post } from '@/utils/request'
import { getToken } from '@/utils/auth'
import type { TabCount } from '@/api/order'

export interface InventorySkuItem {
  skuName: string
  skuId: string
  skuCode: string
  actualStock: number
  warningStock: number
  warehouseCode: string
}

export interface InventoryListItem {
  id: string
  goodsId: string
  thumb: string
  name: string
  skuName: string
  skuId: string
  category: string
  supplier: string
  actualStock: number
  warningStock: number
  frozenStock: number
  availableStock: number
  status: string
  brand: string
  expandTip: string
  skus: InventorySkuItem[]
}

export interface InventoryPageResult {
  list: InventoryListItem[]
  total: number
  page: number
  pageSize: number
  totalPages: number
  tabs: TabCount[]
}

export interface StockFlowItem {
  id: string
  goodsId: string
  relatedNo: string
  orderId: string
  thumb: string
  name: string
  skuName: string
  skuId: string
  type: string
  beforeQty: number
  changeQty: number
  afterQty: number
  operator: string
  time: string
  remark: string
}

export interface StockFlowPageResult {
  list: StockFlowItem[]
  total: number
  page: number
  pageSize: number
  totalPages: number
  tabs: TabCount[]
}

export const flowTypeMap: Record<string, { label: string; type: string }> = {
  sales_out: { label: '发货', type: 'primary' },
  return_in: { label: '退货', type: 'success' },
  manual_out: { label: '手动出库', type: 'primary' },
  manual_in: { label: '手动入库', type: 'success' },
  reissue: { label: '补发', type: 'info' },
}

export const flowTypeOptions = [
  { label: '发货', value: 'sales_out' },
  { label: '退货', value: 'return_in' },
  { label: '手动出库', value: 'manual_out' },
  { label: '手动入库', value: 'manual_in' },
  { label: '补发', value: 'reissue' },
]

/** GET /api/inventory/list */
export function fetchInventoryList(params: {
  keyword?: string
  category?: string
  supplier?: string
  stockStatus?: string
  tab?: string
  page?: number
  pageSize?: number
}) {
  return get<InventoryPageResult>('/api/inventory/list', { params })
}

/** POST /api/inventory/update */
export function updateInventory(data: { goodsId: string; skus: InventorySkuItem[] }) {
  return post('/api/inventory/update', data)
}

/** GET /api/inventory/flow */
export function fetchInventoryFlow(params: {
  flowNo?: string
  product?: string
  bizType?: string
  tab?: string
  goodsId?: string
  startDate?: string
  endDate?: string
  page?: number
  pageSize?: number
}) {
  return get<StockFlowPageResult>('/api/inventory/flow', { params })
}

/** GET /api/inventory/flow/export — CSV 下载 */
export async function exportInventoryFlow(params: {
  flowNo?: string
  product?: string
  bizType?: string
  tab?: string
  goodsId?: string
  startDate?: string
  endDate?: string
}) {
  const token = getToken()
  const res = await axios.get('/api/inventory/flow/export', {
    params,
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `库存流水_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
}
