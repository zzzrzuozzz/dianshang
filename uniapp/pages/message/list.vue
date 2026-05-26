<template>
  <view class="message-list-page">
    <view class="tabs">
      <view
        v-for="cat in categories"
        :key="cat.key"
        class="tab-item"
        :class="{ active: activeCategory === cat.key }"
        @tap="switchCategory(cat.key)"
      >
        {{ cat.name }}
        <view v-if="unread[cat.key] > 0" class="tab-dot" />
      </view>
    </view>

    <view class="list-toolbar">
      <text class="toolbar-title">{{ currentName }}</text>
      <text class="toolbar-action" @tap="handleCategoryReadAll">本类已读</text>
    </view>

    <view v-if="!list.length && !loading" class="empty">暂无消息</view>

    <view
      v-for="item in list"
      :key="item.id"
      class="msg-card"
      :class="{ read: item.isRead === 1 }"
      @tap="onMessageTap(item)"
    >
      <view v-if="item.isRead === 0" class="unread-dot" />
      <view class="msg-body">
        <view class="msg-head">
          <text class="msg-title">{{ item.title }}</text>
          <text class="msg-time">{{ item.createTime }}</text>
        </view>
        <text class="msg-content">{{ item.content }}</text>
      </view>
    </view>

    <view v-if="loading" class="loading-tip">加载中...</view>
    <view v-else-if="!hasMore && list.length" class="loading-tip">没有更多了</view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onLoad, onShow, onReachBottom } from '@dcloudio/uni-app'
import {
  CATEGORY_CONFIG,
  fetchUnreadCount,
  fetchMessagePage,
  markMessagesRead,
  markCategoryAllRead,
} from '../../api/message.js'

const categories = CATEGORY_CONFIG
const activeCategory = ref('SYSTEM')
const list = ref([])
const unread = reactive({ SYSTEM: 0, ORDER: 0, PROMOTION: 0 })
const page = ref(1)
const pageSize = 10
const hasMore = ref(true)
const loading = ref(false)

const currentName = computed(
  () => categories.find((c) => c.key === activeCategory.value)?.name || '',
)

onLoad((query) => {
  if (query.category) activeCategory.value = query.category
  loadList(true)
})

onShow(() => {
  refreshUnread()
})

const refreshUnread = async () => {
  try {
    const counts = await fetchUnreadCount()
    Object.assign(unread, counts)
  } catch {
    /* 静默失败，列表仍可展示 */
  }
}

/**
 * GET /api/u/message/page?category=xxx
 */
const loadList = async (reset = false) => {
  if (loading.value) return
  if (!reset && !hasMore.value) return

  loading.value = true
  if (reset) {
    page.value = 1
    hasMore.value = true
  }

  if (reset) uni.showLoading({ title: '加载中', mask: true })

  try {
    const res = await fetchMessagePage(activeCategory.value, page.value, pageSize)
    const records = res.list || []
    if (reset) list.value = records
    else list.value = [...list.value, ...records]

    hasMore.value = res.hasMore !== false && records.length >= pageSize
    if (hasMore.value) page.value += 1
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    uni.hideLoading()
  }
}

const switchCategory = (key) => {
  if (activeCategory.value === key) return
  activeCategory.value = key
  loadList(true)
}

onReachBottom(() => {
  loadList(false)
})

/**
 * 单条已读 + 业务跳转
 * PUT /api/u/message/read
 */
const onMessageTap = async (item) => {
  if (item.isRead === 0) {
    try {
      await markMessagesRead([item.id])
      item.isRead = 1
      await refreshUnread()
    } catch {
      uni.showToast({ title: '标记已读失败', icon: 'none' })
    }
  }

  const jumpUrl = item.jumpUrl || item.jump_url
  if (jumpUrl) {
    uni.navigateTo({
      url: jumpUrl,
      fail: () => {
        uni.showToast({ title: '页面暂未开放', icon: 'none' })
      },
    })
  }
}

const handleCategoryReadAll = async () => {
  uni.showLoading({ title: '处理中', mask: true })
  try {
    await markCategoryAllRead(activeCategory.value)
    list.value.forEach((m) => {
      m.isRead = 1
    })
    await refreshUnread()
    uni.showToast({ title: '已全部已读', icon: 'success' })
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

</script>

<style scoped>
.message-list-page {
  min-height: 100vh;
  background: #f5f6f8;
  padding-bottom: 40rpx;
}

.tabs {
  display: flex;
  background: #fff;
  padding: 0 16rpx;
  border-bottom: 1rpx solid #ebeef5;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 28rpx;
  color: #606266;
  position: relative;
}

.tab-item.active {
  color: #409eff;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 25%;
  width: 50%;
  height: 4rpx;
  background: #409eff;
  border-radius: 2rpx;
}

.tab-dot {
  position: absolute;
  top: 16rpx;
  right: 24rpx;
  width: 12rpx;
  height: 12rpx;
  background: #f56c6c;
  border-radius: 50%;
}

.list-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 32rpx;
}

.toolbar-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
}

.toolbar-action {
  font-size: 26rpx;
  color: #409eff;
}

.msg-card {
  position: relative;
  margin: 0 24rpx 20rpx;
  padding: 28rpx 28rpx 28rpx 40rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.msg-card.read {
  opacity: 0.85;
}

.unread-dot {
  position: absolute;
  left: 16rpx;
  top: 36rpx;
  width: 16rpx;
  height: 16rpx;
  background: #409eff;
  border-radius: 50%;
}

.msg-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12rpx;
  gap: 16rpx;
}

.msg-title {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
}

.msg-time {
  font-size: 22rpx;
  color: #c0c4cc;
  flex-shrink: 0;
}

.msg-content {
  font-size: 26rpx;
  color: #909399;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.empty,
.loading-tip {
  text-align: center;
  padding: 48rpx;
  font-size: 26rpx;
  color: #909399;
}
</style>
