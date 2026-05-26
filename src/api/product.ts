import { del, get, post, put } from '@/utils/request'
import type { ProductDetailDto, ProductSavePayload } from '@/types/product-form'

export interface ProductVO {
  id: string
  title: string
  subtitle: string
  thumb: string
  originalPrice: number
  discountPrice: number
  status: 'on' | 'off'
  auditStatus: 'pending' | 'passed' | 'rejected'
  remark: string
  sku: string
  sort: number
  stock: number
  monthSales: number
  totalSales: number
  supplier: string
}

export interface TabCount {
  key: string
  label: string
  count: number
}

export interface ProductPageResult {
  list: ProductVO[]
  total: number
  page: number
  pageSize: number
  tabs: TabCount[]
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface CategoryVO {
  id: string
  name: string
  level: string
  count: number
  unit: string
  visible: boolean
  sort: number
}

export interface BrandVO {
  id: string
  name: string
  initial: string
  count: number
  supplier: string
  visible: boolean
  sort: number
}

export interface TreeNode {
  id: number
  label: string
  children?: TreeNode[]
}

export interface CategoryOption {
  label: string
  value: string
}

export interface CommentStatCard {
  key: string
  label: string
  value: string
  action?: string
  todayNew?: number
  trend?: number
}

export interface CommentListVO {
  id: string
  title: string
  thumb: string
  rating: string
  content: string
  originalPrice: number
  discountPrice: number
  status: string
  sku: string
  sort: number
  stock: number
  sales: number
  totalGood: number
  totalNeutral: number
  totalBad: number
}

export interface CommentPageResult {
  list: CommentListVO[]
  total: number
  page: number
  pageSize: number
  tabs: TabCount[]
}

/** GET /api/product/{id} */
export function fetchProductDetail(productNo: string) {
  return get<ProductDetailDto>(`/api/product/${productNo}`)
}

/** POST /api/product */
export function createProduct(data: ProductSavePayload) {
  return post<{ id: string }>('/api/product', data)
}

/** PUT /api/product/{id} */
export function updateProduct(productNo: string, data: ProductSavePayload) {
  return put<{ id: string }>(`/api/product/${productNo}`, data)
}

/** GET /api/product/list */
export function fetchProductList(params: {
  keyword?: string
  category?: string
  status?: string
  page?: number
  pageSize?: number
}) {
  return get<ProductPageResult>('/api/product/list', { params })
}

/** GET /api/product/audit/list */
export function fetchProductAuditList(params: {
  keyword?: string
  category?: string
  auditStatus?: string
  page?: number
  pageSize?: number
}) {
  return get<ProductPageResult>('/api/product/audit/list', { params })
}

export function updateProductStatus(id: string, status: 'on' | 'off') {
  return put(`/api/product/${id}/status`, { status })
}

export function deleteProduct(id: string) {
  return del(`/api/product/${id}`)
}

export function batchProductOn(ids: string[]) {
  return post('/api/product/batch/on', { ids })
}

export function batchProductOff(ids: string[]) {
  return post('/api/product/batch/off', { ids })
}

export function batchDeleteProduct(ids: string[]) {
  return del('/api/product/batch', { data: { ids } })
}

export function auditProduct(id: string, passed: boolean, remark?: string) {
  return post(`/api/product/audit/${id}`, { passed, remark })
}

export function batchAuditProduct(ids: string[], passed: boolean) {
  return post('/api/product/audit/batch', { ids, passed })
}

export function fetchCategoryTree() {
  return get<TreeNode[]>('/api/product/category/tree')
}

export function fetchCategoryOptions() {
  return get<CategoryOption[]>('/api/product/category/options')
}

export function fetchCategoryList(params: {
  keyword?: string
  level?: number
  page?: number
  pageSize?: number
}) {
  return get<PageResult<CategoryVO>>('/api/product/category/list', { params })
}

export function updateCategoryVisible(id: string, visible: boolean) {
  return put(`/api/product/category/${id}/visible`, { visible })
}

export function deleteCategory(id: string) {
  return del(`/api/product/category/${id}`)
}

export function createCategory(data: Partial<CategoryVO>) {
  return post<CategoryVO>('/api/product/category', data)
}

export function updateCategory(id: string, data: Partial<CategoryVO>) {
  return put<CategoryVO>(`/api/product/category/${id}`, data)
}

export function transferCategoryProducts(fromCategoryCode: string, toCategoryCode: string) {
  return post<{ moved: number }>('/api/product/category/transfer', {
    fromCategoryCode,
    toCategoryCode,
  })
}

export function fetchBrandList(params: {
  keyword?: string
  status?: string
  page?: number
  pageSize?: number
}) {
  return get<PageResult<BrandVO>>('/api/product/brand/list', { params })
}

export function updateBrandVisible(id: string, visible: boolean) {
  return put(`/api/product/brand/${id}/visible`, { visible })
}

export function deleteBrand(id: string) {
  return del(`/api/product/brand/${id}`)
}

export function createBrand(data: Partial<BrandVO>) {
  return post<BrandVO>('/api/product/brand', data)
}

export function updateBrand(id: string, data: Partial<BrandVO>) {
  return put<BrandVO>(`/api/product/brand/${id}`, data)
}

export function fetchCommentOverview() {
  return get<{ stats: CommentStatCard[] }>('/api/product/comment/overview')
}

export function fetchCommentList(params: {
  keyword?: string
  rating?: string
  page?: number
  pageSize?: number
}) {
  return get<CommentPageResult>('/api/product/comment/list', { params })
}

export function featureComment(productNo: string) {
  return post(`/api/product/comment/${productNo}/feature`)
}

export function deleteComment(productNo: string) {
  return del(`/api/product/comment/${productNo}`)
}

export interface ProductRecycleVO extends ProductVO {
  deletedAt?: string
}

/** GET /api/product/recycle/list */
export function fetchRecycleList(params: {
  keyword?: string
  page?: number
  pageSize?: number
}) {
  return get<PageResult<ProductRecycleVO>>('/api/product/recycle/list', { params })
}

/** POST /api/product/recycle/restore */
export function restoreRecycleProducts(ids: string[]) {
  return post('/api/product/recycle/restore', { ids })
}

/** DELETE /api/product/recycle/{productNo} 彻底删除 */
export function purgeRecycleProduct(productNo: string) {
  return del(`/api/product/recycle/${productNo}`)
}

/** DELETE /api/product/recycle/batch 批量彻底删除 */
export function batchPurgeRecycleProducts(ids: string[]) {
  return del('/api/product/recycle/batch', { data: { ids } })
}
