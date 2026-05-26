import { mockGlobalSearch } from '@/mock/globalSearch'

const GROUP_LABELS = {
  product: '商品',
  order: '订单',
  user: '用户',
}

/**
 * 将后端 GlobalSearchResultVO 转为 el-autocomplete 可用的扁平列表（含分组标题行）
 * @param {{ products?: Array, orders?: Array, users?: Array }} vo
 */
export function flattenSearchResult(vo) {
  const sections = [
    { key: 'product', items: vo.products || [] },
    { key: 'order', items: vo.orders || [] },
    { key: 'user', items: vo.users || [] },
  ]

  const list = []
  sections.forEach(({ key, items }) => {
    if (!items.length) return
    list.push({
      isGroup: true,
      value: `__group_${key}__`,
      label: GROUP_LABELS[key],
    })
    items.forEach((item) => {
      list.push({
        isGroup: false,
        value: `${key}_${item.id}`,
        category: GROUP_LABELS[key],
        categoryKey: key,
        id: item.id,
        name: item.name,
        targetUrl: item.targetUrl,
        label: `${item.id} - ${item.name}`,
      })
    })
  })
  return list
}

/**
 * GET /api/ops/global/search?keyword=${keyword}
 *
 * 联调说明：后端若面对海量数据，建议对商品标题、订单号建立复合索引，
 * 或走 Redis 词云热搜缓存，避免模糊查询全表扫描导致数据库压力。
 */
export async function fetchGlobalSearch(keyword) {
  const trimmed = (keyword || '').trim()
  if (!trimmed) return []

  // const { data } = await axios.get('/api/ops/global/search', { params: { keyword: trimmed } })
  // return flattenSearchResult(data)

  const vo = await mockGlobalSearch(trimmed)
  return flattenSearchResult({
    products: vo.products,
    orders: vo.orders,
    users: vo.users,
  })
}
