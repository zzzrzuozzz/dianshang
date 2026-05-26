import { get } from '@/utils/request'

const GROUP_LABELS = {
  product: '商品',
  order: '订单',
  user: '用户',
  finance: '财务流水',
}

/**
 * 将后端 GlobalSearchResultVO 转为 el-autocomplete 可用的扁平列表（含分组标题行）
 */
export function flattenSearchResult(vo) {
  const sections = [
    { key: 'product', items: vo.products || [] },
    { key: 'order', items: vo.orders || [] },
    { key: 'user', items: vo.users || [] },
    { key: 'finance', items: vo.finance || [] },
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
 * GET /api/ops/global/search?keyword=
 */
export async function fetchGlobalSearch(keyword) {
  const trimmed = (keyword || '').trim()
  if (!trimmed) return []

  const data = await get('/api/ops/global/search', { params: { keyword: trimmed } })
  return flattenSearchResult({
    products: data.products,
    orders: data.orders,
    users: data.users,
    finance: data.finance,
  })
}
