/** Mock 数据 —— 联调成功后由真实接口替换 */

const store = {
  SYSTEM: [
    {
      id: 1,
      userId: 10001,
      category: 'SYSTEM',
      title: '系统维护通知',
      content: '平台将于今晚 2:00-4:00 进行系统升级，期间可能短暂无法访问。',
      jumpUrl: '',
      isRead: 0,
      createTime: '2024-08-08 10:00',
    },
    {
      id: 2,
      userId: 10001,
      category: 'SYSTEM',
      title: '账号安全提醒',
      content: '您的账号在新设备登录，如非本人操作请及时修改密码。',
      jumpUrl: '',
      isRead: 1,
      createTime: '2024-08-07 18:30',
    },
  ],
  ORDER: [
    {
      id: 3,
      userId: 10001,
      category: 'ORDER',
      title: '您的订单已发货',
      content: '订单 BZ6542 已由顺丰速运揽收，运单号 SF1234567890。',
      jumpUrl: '/pages/order/detail?id=BZ6542',
      isRead: 0,
      createTime: '2024-08-08 16:14',
    },
    {
      id: 4,
      userId: 10001,
      category: 'ORDER',
      title: '订单待付款提醒',
      content: '您有一笔订单即将超时，请尽快完成支付。',
      jumpUrl: '/pages/order/detail?id=BZ6543',
      isRead: 0,
      createTime: '2024-08-08 09:00',
    },
  ],
  PROMOTION: [
    {
      id: 5,
      userId: 10001,
      category: 'PROMOTION',
      title: '春季家电疯狂秒杀',
      content: '限时 24 小时，爆款低至 5 折，点击查看活动详情。',
      jumpUrl: '/pages/activity/seckill',
      isRead: 0,
      createTime: '2024-08-08 12:00',
    },
  ],
}

export function mockUnreadCount() {
  const count = (list) => list.filter((m) => m.isRead === 0).length
  return {
    SYSTEM: count(store.SYSTEM),
    ORDER: count(store.ORDER),
    PROMOTION: count(store.PROMOTION),
  }
}

export function mockMessagePage(category, page, size) {
  const all = store[category] || []
  const start = (page - 1) * size
  const records = all.slice(start, start + size)
  return {
    list: records.map((m) => ({ ...m })),
    total: all.length,
    page,
    size,
    hasMore: start + size < all.length,
  }
}

export function mockMarkAsRead(messageIds) {
  Object.keys(store).forEach((cat) => {
    store[cat].forEach((m) => {
      if (messageIds.includes(m.id)) m.isRead = 1
    })
  })
}

export function mockLatestPreview(category) {
  const list = store[category] || []
  if (!list.length) return null
  return list[0]
}

export function mockMarkCategoryRead(category) {
  const ids = (store[category] || []).filter((m) => m.isRead === 0).map((m) => m.id)
  mockMarkAsRead(ids)
  return ids
}
