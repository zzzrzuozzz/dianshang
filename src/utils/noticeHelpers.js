/** 相对时间文案 */
export function formatTimeAgo(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr.replace(/-/g, '/'))
  const diff = Date.now() - date.getTime()
  if (Number.isNaN(diff)) return timeStr
  const sec = Math.floor(diff / 1000)
  if (sec < 60) return '刚刚'
  const min = Math.floor(sec / 60)
  if (min < 60) return `${min}分钟前`
  const hour = Math.floor(min / 60)
  if (hour < 24) return `${hour}小时前`
  const day = Math.floor(hour / 24)
  if (day < 30) return `${day}天前`
  return timeStr.slice(0, 16)
}

export const NOTICE_REFRESH_EVENT = 'notice:refresh'

export function triggerNoticeRefresh() {
  window.dispatchEvent(new CustomEvent(NOTICE_REFRESH_EVENT))
}
