/** Mock 全局搜索数据源 —— 联调后删除，改由接口返回 GlobalSearchResultVO */

const allProducts = [
  { id: 'BZ6542', name: '100角磨机 工业级切割机', targetUrl: '/product/list' },
  { id: 'BZ6543', name: '春季家电家具疯狂秒杀', targetUrl: '/product/list' },
  { id: '025342', name: '2024新款夏季纯棉T恤', targetUrl: '/product/list' },
]

const allOrders = [
  { id: 'BZ6542', name: '待付款', targetUrl: '/order/detail/BZ6542' },
  { id: 'BZ6543', name: '待发货', targetUrl: '/order/detail/BZ6543' },
  { id: 'ORD202408001', name: '已完成', targetUrl: '/order/detail/ORD202408001' },
]

const allUsers = [
  { id: '13066660001', name: '张三', targetUrl: '/user/list?search=13066660001' },
  { id: '13066660002', name: '李四', targetUrl: '/user/list?search=13066660002' },
  { id: 'U10086', name: '王五', targetUrl: '/user/list?search=U10086' },
]

function matchItem(item, keyword) {
  const kw = keyword.toLowerCase()
  return (
    item.id.toLowerCase().includes(kw) ||
    item.name.toLowerCase().includes(kw)
  )
}

/**
 * 模拟 GET /api/ops/global/search?keyword=
 * @returns {Promise<{ products: [], orders: [], users: [] }>}
 */
export async function mockGlobalSearch(keyword) {
  await new Promise((r) => setTimeout(r, 200))
  const kw = keyword.trim()
  if (!kw) {
    return { products: [], orders: [], users: [] }
  }
  return {
    products: allProducts.filter((p) => matchItem(p, kw)).slice(0, 5),
    orders: allOrders.filter((o) => matchItem(o, kw)).slice(0, 5),
    users: allUsers.filter((u) => matchItem(u, kw)).slice(0, 5),
  }
}
