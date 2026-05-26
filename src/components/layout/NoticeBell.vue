<template>
  <el-popover
    v-model:visible="popoverVisible"
    placement="bottom-end"
    :width="320"
    trigger="click"
    popper-class="notice-popover"
  >
    <template #reference>
      <el-badge
        :value="unreadCount"
        :hidden="!unreadCount"
        :max="99"
        class="notice-badge"
      >
        <el-icon class="header-icon notice-bell" :class="{ active: unreadCount > 0 }">
          <Bell />
        </el-icon>
      </el-badge>
    </template>

    <div class="notice-panel">
      <el-tabs v-model="miniTab" class="notice-tabs" @tab-change="loadMiniList">
        <el-tab-pane label="未读通知" name="all" />
        <el-tab-pane name="urgent">
          <template #label>
            紧急待办
            <el-badge v-if="unreadByType.urgent" :value="unreadByType.urgent" :max="99" class="tab-badge" />
          </template>
        </el-tab-pane>
      </el-tabs>

      <div v-loading="miniLoading" class="notice-list">
        <div
          v-for="item in miniList"
          :key="item.id"
          class="notice-item"
          @click="handleItemClick(item)"
        >
          <span class="level-dot" :class="levelClass(item.level)" />
          <div class="notice-body">
            <p class="notice-title">{{ item.title }}</p>
            <p class="notice-time">{{ formatTimeAgo(item.createTime) }}</p>
          </div>
        </div>
        <el-empty v-if="!miniLoading && !miniList.length" description="暂无未读消息" :image-size="56" />
      </div>

      <div class="notice-footer">
        <el-button type="primary" link @click="goMessageCenter">查看全部消息</el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { fetchUnreadNoticeList, markNoticesRead } from '@/api/notice'
import { useNoticeStore } from '@/composables/useNoticeStore'
import { formatTimeAgo, triggerNoticeRefresh } from '@/utils/noticeHelpers'

const router = useRouter()
const { unreadCount, unreadByType, refreshSummary, startPolling, stopPolling } = useNoticeStore()

const popoverVisible = ref(false)
const miniTab = ref('all')
const miniList = ref([])
const miniLoading = ref(false)

const levelClass = (level) => {
  if (level === 'DANGER') return 'danger'
  if (level === 'WARNING') return 'warning'
  return 'info'
}

const loadMiniList = async () => {
  miniLoading.value = true
  try {
    miniList.value = await fetchUnreadNoticeList(5, miniTab.value === 'urgent')
  } finally {
    miniLoading.value = false
  }
}

const handleItemClick = async (item) => {
  if (item.status === 0) {
    await markNoticesRead([item.id])
    triggerNoticeRefresh()
  }
  popoverVisible.value = false
  router.push({ path: '/message/index', query: { id: item.id } })
}

const goMessageCenter = () => {
  popoverVisible.value = false
  router.push('/message/index')
}

watch(popoverVisible, (visible) => {
  if (visible) loadMiniList()
})

onMounted(() => {
  startPolling(60000)
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.notice-badge {
  line-height: 1;
  cursor: pointer;
}

.notice-bell {
  font-size: 18px;
  color: #606266;
  transition: color 0.2s;
}

.notice-bell.active,
.notice-bell:hover {
  color: #409eff;
}

.notice-panel {
  margin: -4px 0;
}

.notice-tabs :deep(.el-tabs__header) {
  margin-bottom: 8px;
}

.tab-badge {
  margin-left: 4px;
  vertical-align: middle;
}

.notice-list {
  max-height: 280px;
  overflow-y: auto;
  min-height: 80px;
}

.notice-item {
  display: flex;
  gap: 10px;
  padding: 10px 4px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.15s;
}

.notice-item:hover {
  background: #f5f7fa;
}

.level-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.level-dot.info {
  background: #409eff;
}

.level-dot.warning {
  background: #e6a23c;
}

.level-dot.danger {
  background: #f56c6c;
}

.notice-body {
  min-width: 0;
  flex: 1;
}

.notice-title {
  margin: 0;
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-time {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
}

.notice-footer {
  border-top: 1px solid #ebeef5;
  padding-top: 8px;
  margin-top: 4px;
  text-align: center;
}
</style>
