<template>
  <view class="message-center">
    <view class="nav-bar">
      <text class="nav-title">消息中心</text>
      <text class="nav-action" @tap="handleReadAll">一键已读</text>
    </view>

    <view class="category-row">
      <view
        v-for="cat in categories"
        :key="cat.key"
        class="category-card"
        @tap="goList(cat.key)"
      >
        <view class="icon-wrap" :style="{ background: cat.bg }">
          <text class="icon-text">{{ cat.icon }}</text>
          <view v-if="unread[cat.key] > 0" class="badge">
            <text class="badge-text">{{ unread[cat.key] > 99 ? '99+' : unread[cat.key] }}</text>
          </view>
        </view>
        <text class="cat-name">{{ cat.name }}</text>
        <view v-if="preview[cat.key]" class="preview">
          <text class="preview-title">{{ preview[cat.key].title }}</text>
          <text class="preview-time">{{ preview[cat.key].createTime }}</text>
        </view>
        <text v-else class="preview-empty">暂无消息</text>
      </view>
    </view>

    <view class="tip">公告类消息由后台统一推送，打开 App 时自动同步到您的收件箱</view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  CATEGORY_CONFIG,
  fetchUnreadCount,
  fetchLatestPreview,
  markCategoryAllRead,
} from '../../api/message.js'

const categories = CATEGORY_CONFIG
const unread = reactive({ SYSTEM: 0, ORDER: 0, PROMOTION: 0 })
const preview = reactive({ SYSTEM: null, ORDER: null, PROMOTION: null })
const loading = ref(false)

/**
 * onShow 拉取未读数 —— GET /api/u/message/unread-count
 */
const loadDashboard = async () => {
  if (loading.value) return
  loading.value = true
  uni.showLoading({ title: '加载中', mask: true })
  try {
    const counts = await fetchUnreadCount()
    Object.assign(unread, counts)

    await Promise.all(
      categories.map(async (cat) => {
        preview[cat.key] = await fetchLatestPreview(cat.key)
      }),
    )
  } catch {
    uni.showToast({ title: '加载失败，请稍后重试', icon: 'none' })
  } finally {
    loading.value = false
    uni.hideLoading()
  }
}

const goList = (category) => {
  uni.navigateTo({ url: `/pages/message/list?category=${category}` })
}

/** 三个分类全部标记已读 */
const handleReadAll = async () => {
  uni.showLoading({ title: '处理中', mask: true })
  try {
    for (const cat of categories) {
      await markCategoryAllRead(cat.key)
    }
    await loadDashboard()
    uni.showToast({ title: '已全部标记已读', icon: 'success' })
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

onShow(() => {
  loadDashboard()
})
</script>

<style scoped>
.message-center {
  min-height: 100vh;
  background: #f5f6f8;
  padding-bottom: 40rpx;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  background: #fff;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #303133;
}

.nav-action {
  font-size: 28rpx;
  color: #409eff;
}

.category-row {
  display: flex;
  flex-direction: row;
  gap: 20rpx;
  padding: 24rpx;
}

.category-card {
  flex: 1;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.icon-wrap {
  position: relative;
  width: 88rpx;
  height: 88rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16rpx;
}

.icon-text {
  font-size: 40rpx;
}

.badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: #f56c6c;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid #fff;
}

.badge-text {
  font-size: 20rpx;
  color: #fff;
  line-height: 1;
}

.cat-name {
  display: block;
  text-align: center;
  font-size: 28rpx;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16rpx;
}

.preview {
  border-top: 1rpx solid #ebeef5;
  padding-top: 12rpx;
}

.preview-title,
.preview-empty {
  font-size: 22rpx;
  color: #909399;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.preview-time {
  display: block;
  font-size: 20rpx;
  color: #c0c4cc;
  margin-top: 8rpx;
}

.tip {
  margin: 16rpx 32rpx;
  font-size: 22rpx;
  color: #c0c4cc;
  text-align: center;
  line-height: 1.6;
}
</style>
