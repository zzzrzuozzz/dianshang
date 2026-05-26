import { del, get, post, put } from '@/utils/request'
import type { PageResult } from '@/api/product'

export interface OrderListItem {
  id: string
  productName: string
  thumb: string
  spec: string
  quantity: number
  actualAmount: number
  discount: number
  freight: number
  freightFree: boolean
  orderStatus: string
  shipStatus: string
  logistics: string
  payTime: string
  autoConfirmTime: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  supplier: string
  supplierPhone: string
}

export interface TabCount {
  key: string
  label: string
  count: number
}

export interface OrderPageResult {
  list: OrderListItem[]
  total: number
  page: number
  pageSize: number
  totalPages: number
  activeTab?: string
  tabs: TabCount[]
}

export interface AfterSaleListItem {
  id: string
  orderId: string
  productName: string
  thumb: string
  orderStatus: string
  shipStatus: string
  afterSaleStatus: string
  afterSaleType: string
  refundAmount: number
  applyTime: string
  processTime: string
}

export interface AfterSalePageResult {
  list: AfterSaleListItem[]
  total: number
  page: number
  pageSize: number
  totalPages: number
  tabs: TabCount[]
}

export interface ReturnReasonItem {
  id: string
  reason: string
  addTime: string
  visible: boolean
  sort: number
}

export interface OrderDetailDto {
  id: string
  orderStatus: string
  orderStatusText: string
  shipStatus: string
  shipStatusText: string
  afterSalesStatus: string
  afterSalesStatusText: string
  deliverySerial: string
  userAccount: string
  payMethod: string
  payMethodDetail: string
  orderSource: string
  orderType: string
  deliveryMethod: string
  logisticsNo: string
  autoConfirmDays: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  userRemark: string
  platformRemark: string
  invoiceType: string
  invoiceStatus: string
  invoiceAttr: string
  invoiceTitle: string
  invoiceTaxNo: string
  invoiceContent: string
  invoiceEmail: string
  products: Array<{
    thumb: string
    name: string
    spec: string
    quantity: number
    sku: string
    unitPrice: number
    payable: number
    discount: number
    freight: number
    subtotal: number
    actualPaid: number
  }>
  payment: {
    productTotal: number
    freight: number
    goldCoin: number
    promotion: number
    discount: number
    coupon: number
    payableSubtotal: number
    actualPaid: number
  }
  steps: Array<{ title: string; time: string; status: string; desc?: string }>
  afterSale: Record<string, unknown> | null
  actions: string[]
}

export interface OrderListQuery {
  product?: string
  orderId?: string
  logisticsNo?: string
  phone?: string
  timeType?: string
  startDate?: string
  endDate?: string
  status?: string
  pageType?: 'list' | 'confirm'
  page?: number
  pageSize?: number
}

/** GET /api/order/list */
export function fetchOrderList(params: OrderListQuery) {
  return get<OrderPageResult>('/api/order/list', { params })
}

/** GET /api/order/detail/{orderNo} */
export function fetchOrderDetail(orderNo: string) {
  return get<OrderDetailDto>(`/api/order/detail/${orderNo}`)
}

export function shipOrder(orderNo: string, data?: { logistics?: string; logisticsNo?: string }) {
  return post(`/api/order/${orderNo}/ship`, data)
}

/** POST /api/order/{orderNo}/reissue — 补发（扣库存 + reissue 流水） */
export function reissueOrder(orderNo: string, data?: { logistics?: string; logisticsNo?: string }) {
  return post(`/api/order/${orderNo}/reissue`, data)
}

export function refundOrder(orderNo: string) {
  return post(`/api/order/${orderNo}/refund`)
}

export function confirmOrder(orderNo: string) {
  return post(`/api/order/${orderNo}/confirm`)
}

export function batchConfirmOrders(ids: string[]) {
  return post('/api/order/batch/confirm', { ids })
}

/** GET /api/order/after-sale/list */
export function fetchAfterSaleList(params: {
  product?: string
  orderId?: string
  logisticsNo?: string
  afterSaleId?: string
  timeType?: string
  startDate?: string
  endDate?: string
  status?: string
  page?: number
  pageSize?: number
}) {
  return get<AfterSalePageResult>('/api/order/after-sale/list', { params })
}

/** POST /api/order/after-sale/{afterSaleNo}/approve */
export function approveAfterSale(afterSaleNo: string) {
  return post(`/api/order/after-sale/${afterSaleNo}/approve`)
}

/** POST /api/order/after-sale/{afterSaleNo}/reject */
export function rejectAfterSale(afterSaleNo: string, remark?: string) {
  return post(`/api/order/after-sale/${afterSaleNo}/reject`, { remark })
}

/** POST /api/order/after-sale/{afterSaleNo}/confirm-return */
export function confirmAfterSaleReturn(afterSaleNo: string) {
  return post(`/api/order/after-sale/${afterSaleNo}/confirm-return`)
}

/** GET /api/order/setting/return-reason/list */
export function fetchReturnReasonList(params: { keyword?: string; page?: number; pageSize?: number }) {
  return get<PageResult<ReturnReasonItem>>('/api/order/setting/return-reason/list', { params })
}

export function createReturnReason(data: { reason: string; visible?: boolean; sort?: number }) {
  return post<ReturnReasonItem>('/api/order/setting/return-reason', data)
}

export function updateReturnReason(code: string, data: { reason: string; sort?: number }) {
  return put<ReturnReasonItem>(`/api/order/setting/return-reason/${code}`, data)
}

export function deleteReturnReason(code: string) {
  return del(`/api/order/setting/return-reason/${code}`)
}

export function updateReturnReasonVisible(code: string, visible: boolean) {
  return put(`/api/order/setting/return-reason/${code}/visible`, { visible })
}

export interface ExpressTemplateItem {
  id: string
  templateName: string
  expressCompany: string
  templateSpec: string
  remark: string
  isDefault: boolean
  visible: boolean
  sort: number
  addTime: string
}

export interface OrderAddressItem {
  id: string
  contactName: string
  phone: string
  province: string
  city: string
  district: string
  provinceCode?: string
  cityCode?: string
  districtCode?: string
  detailAddress: string
  fullAddress: string
  zipCode: string
  isDefault: boolean
  visible: boolean
  sort: number
  addTime: string
}

export function fetchExpressTemplateList(params: { keyword?: string; page?: number; pageSize?: number }) {
  return get<PageResult<ExpressTemplateItem>>('/api/order/setting/express-template/list', { params })
}

export function createExpressTemplate(data: {
  templateName: string
  expressCompany: string
  templateSpec?: string
  remark?: string
  isDefault?: boolean
  visible?: boolean
  sort?: number
}) {
  return post<ExpressTemplateItem>('/api/order/setting/express-template', data)
}

export function updateExpressTemplate(
  code: string,
  data: {
    templateName: string
    expressCompany: string
    templateSpec?: string
    remark?: string
    isDefault?: boolean
    visible?: boolean
    sort?: number
  },
) {
  return put<ExpressTemplateItem>(`/api/order/setting/express-template/${code}`, data)
}

export function deleteExpressTemplate(code: string) {
  return del(`/api/order/setting/express-template/${code}`)
}

export function updateExpressTemplateVisible(code: string, visible: boolean) {
  return put(`/api/order/setting/express-template/${code}/visible`, { visible })
}

export function updateExpressTemplateDefault(code: string, isDefault: boolean) {
  return put(`/api/order/setting/express-template/${code}/default`, { isDefault })
}

export function fetchOrderAddressList(params: {
  type: 'ship' | 'return'
  keyword?: string
  page?: number
  pageSize?: number
}) {
  return get<PageResult<OrderAddressItem>>('/api/order/setting/address/list', { params })
}

export function createOrderAddress(data: {
  type: 'ship' | 'return'
  contactName: string
  phone: string
  province?: string
  city?: string
  district?: string
  provinceCode?: string
  cityCode?: string
  districtCode?: string
  detailAddress: string
  zipCode?: string
  isDefault?: boolean
  visible?: boolean
  sort?: number
}) {
  return post<OrderAddressItem>('/api/order/setting/address', data)
}

export function updateOrderAddress(
  code: string,
  data: {
    type: 'ship' | 'return'
    contactName: string
    phone: string
    province?: string
    city?: string
    district?: string
    detailAddress: string
    zipCode?: string
    isDefault?: boolean
    visible?: boolean
    sort?: number
  },
) {
  return put<OrderAddressItem>(`/api/order/setting/address/${code}`, data)
}

export function deleteOrderAddress(code: string) {
  return del(`/api/order/setting/address/${code}`)
}

export function updateOrderAddressVisible(code: string, visible: boolean) {
  return put(`/api/order/setting/address/${code}/visible`, { visible })
}

export function updateOrderAddressDefault(code: string, isDefault: boolean) {
  return put(`/api/order/setting/address/${code}/default`, { isDefault })
}
