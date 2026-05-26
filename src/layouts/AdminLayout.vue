<template>
  <el-container class="admin-layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="admin-aside">
      <div class="brand" @click="router.push('/dashboard')">
        <svg class="brand-logo" viewBox="0 0 32 32" fill="none" aria-hidden="true">
          <rect x="6" y="10" width="20" height="14" rx="4" fill="#409EFF" />
          <rect x="10" y="6" width="12" height="8" rx="3" fill="#409EFF" />
          <circle cx="12" cy="17" r="2" fill="#fff" />
          <circle cx="20" cy="17" r="2" fill="#fff" />
          <rect x="13" y="21" width="6" height="2" rx="1" fill="#fff" />
        </svg>
        <span v-show="!collapsed" class="brand-text">暴走电商</span>
      </div>

      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="activeMenu"
          :collapse="collapsed"
          :collapse-transition="false"
          background-color="#001529"
          text-color="rgba(255,255,255,0.75)"
          active-text-color="#fff"
          class="admin-menu"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>

          <el-sub-menu index="product">
            <template #title>
              <el-icon><Goods /></el-icon>
              <span>商品</span>
            </template>
            <el-menu-item index="/product/list">商品列表</el-menu-item>
            <el-menu-item index="/product/add">添加商品</el-menu-item>
            <el-menu-item index="/product/audit">商品审核</el-menu-item>
            <el-menu-item index="/product/recycle">回收站</el-menu-item>
            <el-menu-item index="/product/comment">商品评价</el-menu-item>
            <el-menu-item index="/product/category">商品分类</el-menu-item>
            <el-menu-item index="/product/brand">品牌管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="order">
            <template #title>
              <el-icon><List /></el-icon>
              <span>订单</span>
            </template>
            <el-menu-item index="/order/list">订单列表</el-menu-item>
            <el-menu-item index="/order/confirm">确认收货</el-menu-item>
            <el-menu-item index="/order/after-sale">售后列表</el-menu-item>
            <el-menu-item index="/order/setting">订单设置</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="inventory">
            <template #title>
              <el-icon><Box /></el-icon>
              <span>库存管理</span>
            </template>
            <el-menu-item index="/inventory/list">库存看板</el-menu-item>
            <el-menu-item index="/inventory/flow">出入库流水</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="user">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户</span>
            </template>
            <el-menu-item index="/user/list">用户列表</el-menu-item>
            <el-menu-item index="/user/tag">标签管理</el-menu-item>
            <el-menu-item index="/user/level">会员等级</el-menu-item>
            <el-menu-item index="/user/growth">成长值积分</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="promotion">
            <template #title>
              <el-icon><Ticket /></el-icon>
              <span>营销</span>
            </template>
            <el-menu-item index="/promotion/seckill">秒杀活动</el-menu-item>
            <el-menu-item index="/promotion/group-buy">团购活动</el-menu-item>
            <el-menu-item index="/promotion/coupon">优惠券</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="ops">
            <template #title>
              <el-icon><Bell /></el-icon>
              <span>运营</span>
            </template>
            <el-menu-item index="/ops/system-message">系统消息</el-menu-item>
            <el-menu-item index="/ops/sms">短信</el-menu-item>
            <el-menu-item index="/ops/station-message">站内信</el-menu-item>
            <el-menu-item index="/ops/advertisement">广告位</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container class="admin-main-wrap">
      <el-header class="admin-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="collapsed = !collapsed">
            <Fold v-if="!collapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="(item, idx) in breadcrumbs" :key="idx">
              {{ item }}
            </el-breadcrumb-item>
          </el-breadcrumb>
          <el-icon class="refresh-btn" @click="handleRefresh"><Refresh /></el-icon>
        </div>
        <div class="header-right">
          <el-icon class="header-icon"><Search /></el-icon>
          <el-badge :value="99" :max="99" class="header-badge">
            <el-icon class="header-icon"><Bell /></el-icon>
          </el-badge>
          <div class="user-info">
            <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <span class="username">admin</span>
            <el-tag size="small" type="primary">管理员</el-tag>
          </div>
          <el-button size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeFilled,
  Goods,
  List,
  Box,
  User,
  Ticket,
  Fold,
  Expand,
  Refresh,
  Search,
  Bell,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const collapsed = ref(false)

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  const meta = route.meta
  if (meta?.title && meta?.subTitle) {
    return [meta.title, meta.subTitle]
  }
  if (meta?.title) {
    return [meta.title]
  }
  return ['首页']
})

const handleRefresh = () => {
  router.replace({ path: '/redirect' + route.fullPath }).catch(() => {})
}

const handleLogout = () => {
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

.admin-aside {
  display: flex;
  flex-direction: column;
  background-color: #001529;
  transition: width 0.2s;
  overflow: hidden;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 56px;
  padding: 0 16px;
  cursor: pointer;
  flex-shrink: 0;
}

.brand-logo {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
}

.brand-text {
  font-size: 16px;
  font-weight: 700;
  color: #409eff;
  white-space: nowrap;
}

.menu-scroll {
  flex: 1;
}

.admin-menu {
  border-right: none;
}

.admin-menu:not(.el-menu--collapse) {
  width: 220px;
}

.admin-menu :deep(.el-menu-item) {
  margin: 4px 8px;
  border-radius: 6px;
  height: 44px;
}

.admin-menu :deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
  color: #fff !important;
}

.admin-menu :deep(.el-sub-menu .el-menu-item) {
  min-width: auto;
  padding-left: 48px !important;
}

.admin-menu :deep(.el-sub-menu__title) {
  margin: 4px 8px;
  border-radius: 6px;
  height: 44px;
}

.admin-main-wrap {
  min-width: 0;
  background: #f0f2f5;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn,
.refresh-btn,
.header-icon {
  font-size: 18px;
  color: #606266;
  cursor: pointer;
}

.collapse-btn:hover,
.refresh-btn:hover,
.header-icon:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-badge {
  line-height: 1;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 14px;
  color: #303133;
}

.admin-main {
  padding: 16px;
  overflow: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
