import { ref, computed } from 'vue'
import { fetchPublicConfig, fetchPlatformConfig } from '@/api/system'

const shopName = ref('暴走电商')
const servicePhone = ref('400-888-8888')
const freeShipThreshold = ref(99)
const stockDeductStrategy = ref('order')
const lastUpdateTime = ref('')
let publicLoaded = false

const stockDeductLabel = computed(() =>
  stockDeductStrategy.value === 'pay' ? '付款减库存' : '下单减库存',
)

const freeShipRuleText = computed(
  () => `满 ${freeShipThreshold.value} 元包邮`,
)

function applyPublic(data) {
  if (!data) return
  if (data.shop_name) shopName.value = data.shop_name
  if (data.service_phone) servicePhone.value = data.service_phone
  if (data.free_ship_threshold) freeShipThreshold.value = Number(data.free_ship_threshold)
  if (data.stock_deduct_strategy) stockDeductStrategy.value = data.stock_deduct_strategy
  document.title = `${shopName.value} | 管理后台`
}

function applyPlatform(data) {
  if (!data) return
  shopName.value = data.shopName || shopName.value
  servicePhone.value = data.servicePhone || servicePhone.value
  freeShipThreshold.value = data.freeShipThreshold ?? freeShipThreshold.value
  stockDeductStrategy.value = data.stockDeductStrategy || stockDeductStrategy.value
  lastUpdateTime.value = data.lastUpdateTime || ''
  document.title = `${shopName.value} | 管理后台`
}

/** 公开配置：登录页、未登录场景 */
export async function loadPublicPlatformConfig(force = false) {
  if (publicLoaded && !force) return
  try {
    const data = await fetchPublicConfig()
    applyPublic(data)
    publicLoaded = true
  } catch {
    /* 保留默认 */
  }
}

/** 管理端完整配置（需登录） */
export async function loadPlatformConfig() {
  try {
    const data = await fetchPlatformConfig()
    applyPlatform(data)
    publicLoaded = true
    return data
  } catch {
    return null
  }
}

export async function refreshPlatformConfig() {
  publicLoaded = false
  await loadPublicPlatformConfig(true)
  try {
    await loadPlatformConfig()
  } catch {
    /* 仅公开配置也可展示品牌 */
  }
}

/** @deprecated 请使用 usePlatformConfig */
export function useShopBrand() {
  return {
    shopName,
    loadShopBrand: loadPublicPlatformConfig,
    refreshShopBrand: refreshPlatformConfig,
  }
}

export function usePlatformConfig() {
  return {
    shopName,
    servicePhone,
    freeShipThreshold,
    stockDeductStrategy,
    stockDeductLabel,
    freeShipRuleText,
    lastUpdateTime,
    loadPublicPlatformConfig,
    loadPlatformConfig,
    refreshPlatformConfig,
  }
}
