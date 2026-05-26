import { request } from '../utils/request.js'
import {
  mockUnreadCount,
  mockMessagePage,
  mockMarkAsRead,
  mockLatestPreview,
  mockMarkCategoryRead,
} from '../mock/message.js'

/** 是否使用 Mock（联调时改为 false） */
const USE_MOCK = true

const CATEGORY_MAP = {
  SYSTEM: 'SYSTEM',
  ORDER: 'ORDER',
  PROMOTION: 'PROMOTION',
}

/**
 * GET /api/u/message/unread-count
 * @returns {Promise<{ SYSTEM: number, ORDER: number, PROMOTION: number }>}
 */
export async function fetchUnreadCount() {
  if (USE_MOCK) {
    await delay(200)
    return mockUnreadCount()
  }
  return request({ url: '/api/u/message/unread-count' })
}

/**
 * GET /api/u/message/page
 * @param {string} category SYSTEM | ORDER | PROMOTION
 */
export async function fetchMessagePage(category, page = 1, size = 10) {
  const cat = CATEGORY_MAP[category] || category
  if (USE_MOCK) {
    await delay(300)
    return mockMessagePage(cat, page, size)
  }
  return request({
    url: '/api/u/message/page',
    data: { category: cat, page, size },
  })
}

/**
 * PUT /api/u/message/read
 * @param {number[]} messageIds
 */
export async function markMessagesRead(messageIds) {
  if (!messageIds?.length) return
  if (USE_MOCK) {
    await delay(200)
    mockMarkAsRead(messageIds)
    return
  }
  return request({
    url: '/api/u/message/read',
    method: 'PUT',
    data: messageIds,
  })
}

/** 获取分类最新一条预览（Mock；生产可由 page 接口 size=1 替代） */
export async function fetchLatestPreview(category) {
  if (USE_MOCK) {
    return mockLatestPreview(category)
  }
  const res = await fetchMessagePage(category, 1, 1)
  return res.list?.[0] || null
}

/** 当前分类一键已读 */
export async function markCategoryAllRead(category) {
  if (USE_MOCK) {
    await delay(200)
    return mockMarkCategoryRead(category)
  }
  const page = await fetchMessagePage(category, 1, 100)
  const ids = (page.list || []).filter((m) => m.isRead === 0).map((m) => m.id)
  if (ids.length) await markMessagesRead(ids)
  return ids
}

function delay(ms) {
  return new Promise((r) => setTimeout(r, ms))
}

export const CATEGORY_CONFIG = [
  {
    key: 'SYSTEM',
    name: '系统通知',
    icon: '🔔',
    bg: 'linear-gradient(135deg, #409eff, #66b1ff)',
    badgeType: 'primary',
  },
  {
    key: 'ORDER',
    name: '交易物流',
    icon: '📦',
    bg: 'linear-gradient(135deg, #e6a23c, #f3d19e)',
    badgeType: 'warning',
  },
  {
    key: 'PROMOTION',
    name: '促销优惠',
    icon: '🎁',
    bg: 'linear-gradient(135deg, #f56c6c, #fab6b6)',
    badgeType: 'error',
  },
]
