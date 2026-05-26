import { get, put, post } from '@/utils/request'

export interface SysConfigItem {
  id?: number
  configKey: string
  configValue: string
  remark?: string
  updateTime?: string
}

export interface PublicConfig {
  shop_name?: string
  service_phone?: string
  free_ship_threshold?: string
  stock_deduct_strategy?: string
}

export interface PlatformConfig {
  shopName: string
  servicePhone: string
  freeShipThreshold: number
  unpaidCloseMinutes: number
  stockDeductStrategy: 'order' | 'pay'
  stockDeductStrategyLabel?: string
  lastUpdateTime?: string
}

export interface InitializerLine {
  name: string
  status: string
  detail: string
}

export interface MaintenanceStatus {
  initializers: InitializerLine[]
  cacheClearedAt: string
  serverTimeMs: number
}

/** GET /api/system/config/public — 登录页/侧栏（无需 Token） */
export function fetchPublicConfig() {
  return get<PublicConfig>('/api/system/config/public')
}

/** GET /api/system/config/platform — 平台基础信息表单 */
export function fetchPlatformConfig() {
  return get<PlatformConfig>('/api/system/config/platform')
}

/** PUT /api/system/config/platform */
export function savePlatformConfig(config: PlatformConfig) {
  return put<void>('/api/system/config/platform', config)
}

/** GET /api/system/config/get-all */
export function fetchSysConfig() {
  return get<SysConfigItem[]>('/api/system/config/get-all')
}

/** PUT /api/system/config/update-batch */
export function submitSysConfig(configs: SysConfigItem[]) {
  return put<void>('/api/system/config/update-batch', configs)
}

/** POST /api/system/maintenance/clear-cache */
export function clearSystemCache() {
  return post<{ message: string; clearedAt: string }>('/api/system/maintenance/clear-cache')
}

/** GET /api/system/maintenance/initializer-status */
export function fetchInitializerStatus() {
  return get<MaintenanceStatus>('/api/system/maintenance/initializer-status')
}
