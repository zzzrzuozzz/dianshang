<template>
  <div v-loading="loading" class="user-detail-page">
    <el-card shadow="never" class="panel-card">
      <div class="action-bar">
        <span class="action-label">操作</span>
        <div class="action-btns">
          <el-button type="primary" size="small">赠送优惠券</el-button>
          <el-button type="primary" size="small">发短信</el-button>
          <el-button type="primary" size="small">发站内信</el-button>
          <el-button type="primary" size="small">APP推送</el-button>
          <el-button type="primary" size="small">设置标签</el-button>
          <el-button type="success" size="small" @click="goEdit">编辑资料</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card section-card">
      <template #header><span class="section-title">基本信息</span></template>
      <div class="basic-layout">
        <div class="avatar-block">
          <el-avatar :size="80" :src="userInfo.avatar" />
          <p class="phone-mask">{{ userInfo.phoneMasked }}</p>
          <el-tag type="warning" size="small">VIP {{ userInfo.level }}</el-tag>
        </div>
        <el-descriptions :column="3" border class="basic-desc">
          <el-descriptions-item label="用户ID">{{ userInfo.userId }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ userInfo.statusText }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ userInfo.nickname }}</el-descriptions-item>
          <el-descriptions-item label="当前IP">{{ userInfo.ip }}</el-descriptions-item>
          <el-descriptions-item label="用户来源">{{ userInfo.source }}</el-descriptions-item>
          <el-descriptions-item label="标签属性">{{ userInfo.tags }}</el-descriptions-item>
          <el-descriptions-item label="生日">{{ userInfo.birthday }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ userInfo.registerTime }}</el-descriptions-item>
          <el-descriptions-item label="城市">{{ userInfo.city }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ userInfo.gender }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card section-card">
      <template #header><span class="section-title">统计信息</span></template>
      <div class="stats-grid">
        <div v-for="item in mainStats" :key="item.key" class="stat-item">
          <span class="stat-label">{{ item.label }}</span>
          <div class="stat-value-row">
            <span class="stat-value">{{ item.value }}</span>
            <el-link v-if="item.link" type="primary" @click="item.onClick">查看 &gt;</el-link>
          </div>
        </div>
      </div>
      <div class="stats-grid sub-stats">
        <div v-for="item in subStats" :key="item.key" class="stat-item small">
          <span class="stat-label">{{ item.label }}</span>
          <span class="stat-value">{{ item.value }}</span>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card section-card">
      <template #header><span class="section-title">收货地址</span></template>
      <el-table :data="userInfo.addresses" border>
        <el-table-column prop="name" label="收货姓名" width="100" />
        <el-table-column prop="phone" label="收货手机" width="120" />
        <el-table-column prop="region" label="收货地址" width="180" />
        <el-table-column prop="detail" label="详细地址" min-width="200" />
      </el-table>
    </el-card>

    <el-card shadow="never" class="panel-card section-card">
      <template #header><span class="section-title">订单记录</span></template>
      <el-table :data="userInfo.orders" border stripe>
        <el-table-column prop="id" label="订单编号" width="140" />
        <el-table-column prop="time" label="提交时间" width="160" />
        <el-table-column label="订单金额" width="100" align="center">
          <template #default="{ row }">{{ row.amount.toFixed(2) }}元</template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="90" />
        <el-table-column prop="source" label="订单来源" width="90" />
        <el-table-column prop="status" label="订单状态" width="90" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewOrder(row)">查看订单</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchUserDetail } from '@/api/user'

const route = useRoute()
const router = useRouter()
const loading = ref(false)

const userInfo = reactive({
  userId: '',
  nickname: '',
  avatar: '',
  phoneMasked: '',
  level: '',
  statusText: '',
  ip: '',
  source: '',
  tags: '',
  birthday: '',
  registerTime: '',
  city: '',
  gender: '',
  consumeAmount: 0,
  orderCount: 0,
  points: 0,
  growth: 0,
  couponCount: 0,
  reviewCount: 0,
  returnCount: 0,
  loginCount: 0,
  favoriteProducts: 0,
  favoriteTopics: 0,
  orderFriends: 0,
  lotteryCount: 0,
  addresses: [],
  orders: [],
})

const mainStats = computed(() => [
  { key: 'consume', label: '消费金额', value: `${userInfo.consumeAmount?.toFixed(2)}元`, link: false },
  { key: 'orders', label: '订单数量', value: userInfo.orderCount, link: true, onClick: () => {} },
  { key: 'points', label: '可用积分', value: userInfo.points, link: true, onClick: () => router.push('/user/growth') },
  { key: 'growth', label: '成长值', value: userInfo.growth, link: true, onClick: () => router.push('/user/growth') },
  { key: 'coupon', label: '优惠券(张)', value: userInfo.couponCount, link: true, onClick: () => {} },
  { key: 'review', label: '商品评价', value: userInfo.reviewCount, link: true, onClick: () => {} },
])

const subStats = computed(() => [
  { key: 'return', label: '退货记录', value: userInfo.returnCount },
  { key: 'login', label: '登录次数', value: userInfo.loginCount },
  { key: 'favP', label: '收藏商品', value: userInfo.favoriteProducts },
  { key: 'favT', label: '收藏专题', value: userInfo.favoriteTopics },
  { key: 'friends', label: '下单好友', value: userInfo.orderFriends },
  { key: 'lottery', label: '剩余抽奖次数', value: userInfo.lotteryCount },
])

/**
 * GET /api/user/detail/{id}
 */
const getUserDetail = async (userId) => {
  loading.value = true
  try {
    const data = await fetchUserDetail(userId)
    Object.assign(userInfo, data)
  } finally {
    loading.value = false
  }
}

const goEdit = () => router.push(`/user/edit/${userInfo.userId}`)
const viewOrder = (row) => router.push(`/order/detail/${row.id}`)

onMounted(() => getUserDetail(route.params.userId))
</script>

<style scoped>
.user-detail-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.action-bar { display: flex; justify-content: space-between; align-items: center; }
.action-label { font-weight: 600; }
.action-btns { display: flex; flex-wrap: wrap; gap: 8px; }
.section-title::before { content: ''; display: inline-block; width: 4px; height: 14px; margin-right: 8px; background: #409eff; border-radius: 2px; vertical-align: middle; }
.basic-layout { display: flex; gap: 24px; }
.avatar-block { text-align: center; flex-shrink: 0; }
.phone-mask { margin: 8px 0; font-size: 13px; color: #606266; }
.basic-desc { flex: 1; }
.stats-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 16px; }
.sub-stats { margin-top: 16px; padding-top: 16px; border-top: 1px solid #ebeef5; }
.stat-item { padding: 12px; background: #fafafa; border-radius: 6px; }
.stat-item.small .stat-value { font-size: 18px; }
.stat-label { font-size: 12px; color: #909399; }
.stat-value-row { display: flex; align-items: baseline; justify-content: space-between; margin-top: 6px; }
.stat-value { font-size: 20px; font-weight: 600; color: #303133; }
@media (max-width: 1200px) { .stats-grid { grid-template-columns: repeat(3, 1fr); } }
</style>
