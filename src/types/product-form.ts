/** 添加/编辑商品表单模型（与页面字段一一对应） */
export interface ProductFormModel {
  category: string
  name: string
  subtitle: string
  brand: string
  intro: string
  shipping: string
  /** 配送区域：多选省市区代码路径 */
  deliveryRegions: string[][]
  sku: string
  price: string
  marketPrice: string
  stock: string
  stockWarning: string
  unit: string
  weight: string
  preSale: boolean
  onSale: boolean
  recommend: string[]
  services: string[]
  tags: string
  mainImages: string[]
  whiteImage: string
  video: string
  detail: string
  /** 多规格 SKU 行（Step2） */
  skuRows: ProductSkuRow[]
}

export interface ProductSkuRow {
  skuId: string
  skuName: string
  skuCode: string
  stock: string
  stockWarning: string
  isPrimary: boolean
}

export interface ProductSavePayload {
  category: string
  name: string
  subtitle?: string
  brand: string
  intro: string
  shipping?: string
  deliveryRegions?: string[][]
  sku?: string
  price: number
  marketPrice?: number
  stock: number
  stockWarning?: number
  unit?: string
  weight?: string
  preSale?: boolean
  onSale?: boolean
  recommend?: string[]
  services?: string[]
  tags?: string
  mainImages?: string[]
  whiteImage?: string
  video?: string
  detail?: string
}

export interface ProductDetailDto extends ProductSavePayload {
  id: string
  status?: string
  auditStatus?: string
}
