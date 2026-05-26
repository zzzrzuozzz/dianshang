import { get } from '@/utils/request'

export interface ExpressSkuItem {
  name: string
  spec: string
  quantity: number
}

export interface ExpressPreviewDto {
  orderId: string
  expressCompany: string
  expressNumber: string
  templateName: string
  templateSpec: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  senderName: string
  senderPhone: string
  senderAddress: string
  skuList: ExpressSkuItem[]
}

/** GET /api/ops/express/preview/{orderId} */
export function fetchExpressPreview(orderId: string) {
  return get<ExpressPreviewDto>(`/api/ops/express/preview/${orderId}`)
}
