import { ref } from 'vue'
import { fetchUnreadNoticeSummary } from '@/api/notice'
import { NOTICE_REFRESH_EVENT } from '@/utils/noticeHelpers'

const unreadCount = ref(0)
const unreadByType = ref({
  SYSTEM: 0,
  AUDIT: 0,
  ALARM: 0,
  urgent: 0,
})

let pollTimer = null
let listenerBound = false

async function refreshSummary() {
  try {
    const data = await fetchUnreadNoticeSummary()
    unreadCount.value = data?.total ?? 0
    unreadByType.value = {
      SYSTEM: data?.systemCount ?? 0,
      AUDIT: data?.auditCount ?? 0,
      ALARM: data?.alarmCount ?? 0,
      urgent: data?.urgentCount ?? 0,
    }
  } catch {
    /* 未登录或接口暂不可用时静默 */
  }
}

function bindRefreshListener() {
  if (listenerBound) return
  listenerBound = true
  window.addEventListener(NOTICE_REFRESH_EVENT, refreshSummary)
}

export function useNoticeStore() {
  bindRefreshListener()

  const startPolling = (intervalMs = 60000) => {
    stopPolling()
    refreshSummary()
    pollTimer = window.setInterval(refreshSummary, intervalMs)
  }

  const stopPolling = () => {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  return {
    unreadCount,
    unreadByType,
    refreshSummary,
    startPolling,
    stopPolling,
  }
}

export { refreshSummary as refreshNoticeSummary }
