import { ref } from 'vue'
import { fetchPublicConfig } from '@/api/system'

const shopName = ref('暴走电商')
let loaded = false

/** 从 sys_config 拉取商城品牌名，供登录页与侧栏复用 */
export function useShopBrand() {
  const loadShopBrand = async () => {
    if (loaded) return shopName.value
    try {
      const data = await fetchPublicConfig()
      if (data?.shop_name) {
        shopName.value = data.shop_name
        document.title = `${data.shop_name} | 管理后台`
      }
      loaded = true
    } catch {
      /* 接口不可用时保留默认 */
    }
    return shopName.value
  }

  const refreshShopBrand = async () => {
    loaded = false
    return loadShopBrand()
  }

  return { shopName, loadShopBrand, refreshShopBrand }
}
