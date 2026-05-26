<template>
  <div v-loading="loading" class="promo-page">
    <el-card shadow="never" class="panel-card">
      <div class="action-bar">
        <span class="action-label">操作</span>
        <div>
          <el-button type="primary" size="small">编辑优惠券</el-button>
          <el-button type="danger" size="small">下架</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <template #header><span class="section-title">优惠券明细 - 基本信息</span></template>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="优惠券名称">{{ couponInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="优惠券类型">{{ couponInfo.type }}</el-descriptions-item>
        <el-descriptions-item label="可使用商品">{{ couponInfo.products }}</el-descriptions-item>
        <el-descriptions-item label="使用门槛">{{ couponInfo.threshold }}</el-descriptions-item>
        <el-descriptions-item label="面值">{{ couponInfo.faceValue }}元</el-descriptions-item>
        <el-descriptions-item label="状态">{{ couponInfo.status }}</el-descriptions-item>
        <el-descriptions-item label="开始结束时间" :span="2">{{ couponInfo.timeRange }}</el-descriptions-item>
        <el-descriptions-item label="有效期">{{ couponInfo.validity }}</el-descriptions-item>
        <el-descriptions-item label="总发行量">{{ couponInfo.totalIssue }}</el-descriptions-item>
        <el-descriptions-item label="剩余量">{{ couponInfo.remain }}</el-descriptions-item>
        <el-descriptions-item label="已领取">{{ couponInfo.claimed }}</el-descriptions-item>
        <el-descriptions-item label="已使用">{{ couponInfo.used }}</el-descriptions-item>
        <el-descriptions-item label="待使用">{{ couponInfo.pending }}</el-descriptions-item>
        <el-descriptions-item label="已过期">{{ couponInfo.expired }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="history-header">
          <span class="section-title">领取明细</span>
          <el-form :inline="true" :model="historySearch" size="small">
            <el-form-item>
              <el-input v-model="historySearch.couponId" placeholder="优惠券编号" clearable style="width: 120px" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="historySearch.member" placeholder="会员名称/手机号" clearable style="width: 140px" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="historySearch.orderId" placeholder="订单编号" clearable style="width: 120px" />
            </el-form-item>
            <el-button type="primary" @click="fetchCouponHistory">查询</el-button>
            <el-button @click="resetHistorySearch">重置</el-button>
          </el-form>
        </div>
      </template>

      <el-tabs v-model="historyTab" @tab-change="fetchCouponHistory">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待使用" name="pending" />
        <el-tab-pane label="已使用" name="used" />
        <el-tab-pane label="已过期" name="expired" />
      </el-tabs>

      <el-table :data="historyData" border stripe>
        <el-table-column prop="couponId" label="优惠券编号" width="100" align="center" />
        <el-table-column prop="member" label="领取会员" min-width="160" />
        <el-table-column prop="method" label="领取方式" width="100" />
        <el-table-column prop="claimTime" label="领取时间" width="150" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="statusClass(row.status)">{{ statusLabel(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="使用/过期时间" width="150">
          <template #default="{ row }">{{ row.useTime }}</template>
        </el-table-column>
        <el-table-column label="订单编号" width="130">
          <template #default="{ row }">
            <el-button v-if="row.orderId" type="primary" link @click="viewOrder(row)">{{ row.orderId }}</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <span>第1页 共10页 265条</span>
        <el-pagination :total="265" layout="prev, pager, next, sizes" background />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { mockCouponDetail, mockCouponHistory } from '@/mock/promotion'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const historyTab = ref('all')
const historyData = ref([])

const couponInfo = reactive({ ...mockCouponDetail })
const historySearch = reactive({ couponId: '', member: '', orderId: '' })

const statusLabel = (s) => ({ pending: '待使用', used: '已使用', expired: '已过期' }[s] || s)
const statusClass = (s) => (s === 'expired' ? 'text-muted' : s === 'used' ? 'text-success' : '')

/**
 * GET /api/promotion/coupon/history/{id}
 */
const fetchCouponHistory = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 400))
    let list = [...mockCouponHistory]
    if (historyTab.value !== 'all') list = list.filter((i) => i.status === historyTab.value)
    historyData.value = list
  } finally {
    loading.value = false
  }
}

const resetHistorySearch = () => {
  Object.assign(historySearch, { couponId: '', member: '', orderId: '' })
  fetchCouponHistory()
}

const viewOrder = (row) => router.push(`/order/detail/${row.orderId}`)

onMounted(() => {
  Object.assign(couponInfo, mockCouponDetail)
  fetchCouponHistory()
})
</script>

<style scoped>
.promo-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.action-bar { display: flex; justify-content: space-between; align-items: center; }
.action-label { font-weight: 600; }
.section-title::before { content: ''; display: inline-block; width: 4px; height: 14px; margin-right: 8px; background: #409eff; border-radius: 2px; vertical-align: middle; }
.history-header { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 12px; }
.text-success { color: #67c23a; }
.text-muted { color: #909399; }
.pagination-bar { display: flex; justify-content: space-between; margin-top: 16px; font-size: 13px; color: #606266; }
</style>
