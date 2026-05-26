<template>
  <div v-loading="loading" class="dashboard-page">
    <!-- Row 1: 数据看板 -->
    <el-row :gutter="16" class="dashboard-row">
      <el-col
        v-for="item in dashboardData.stats"
        :key="item.key"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="4"
        :xl="4"
      >
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card__body">
            <div class="stat-card__icon" :style="{ backgroundColor: item.iconBg }">
              <el-icon :size="22" :color="item.iconColor">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <div class="stat-card__content">
              <p class="stat-card__label">{{ item.label }}</p>
              <p class="stat-card__value">{{ item.displayValue }}</p>
              <p class="stat-card__trend">
                <span :class="item.trend >= 0 ? 'trend-up' : 'trend-down'">
                  {{ item.trend >= 0 ? '+' : '' }}{{ item.trend }}%
                </span>
                <span class="stat-card__compare">{{ item.compareLabel }}</span>
              </p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Row 2: 待处理事务 & 快捷入口 -->
    <el-row :gutter="16" class="dashboard-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="panel-card">
          <template #header>
            <span class="panel-card__title">待处理事务</span>
          </template>
          <ul class="pending-list">
            <li
              v-for="task in dashboardData.pendingTasks"
              :key="task.key"
              class="pending-list__item"
            >
              <span class="pending-list__label">{{ task.label }}</span>
              <el-badge :value="task.count" :max="9999" type="danger" />
            </li>
          </ul>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="panel-card">
          <template #header>
            <span class="panel-card__title">快捷入口</span>
          </template>
          <div class="quick-grid">
            <div
              v-for="entry in dashboardData.quickAccess"
              :key="entry.key"
              class="quick-grid__item"
              @click="handleQuickAccess(entry.path)"
            >
              <div class="quick-grid__icon" :style="{ backgroundColor: entry.iconBg }">
                <el-icon :size="22" :color="entry.iconColor">
                  <component :is="entry.icon" />
                </el-icon>
              </div>
              <span class="quick-grid__label">{{ entry.label }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Row 3: 销售额 / 订单量趋势图 -->
    <el-row :gutter="16" class="dashboard-row">
      <el-col :span="24">
        <el-card shadow="hover" class="panel-card chart-card">
          <template #header>
            <span class="panel-card__title">销售额 / 订单量趋势</span>
          </template>
          <div ref="chartRef" class="chart-container" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount, nextTick, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import {
  Document,
  User,
  CreditCard,
  Money,
  Wallet,
  Goods,
  List,
  DataAnalysis,
  Promotion,
  Setting,
} from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const chartRef = ref(null)
let chartInstance = null

/** 首页全部展示数据，后续由后端接口统一返回 */
const dashboardData = reactive({
  stats: [],
  pendingTasks: [],
  quickAccess: [],
  chart: {
    dates: [],
    sales: [],
    orders: [],
  },
})

/**
 * 获取首页看板数据
 * 此处后续使用 axios 请求 Spring Boot 后端的 /api/dashboard/overview 接口
 */
const fetchDashboardData = async () => {
  loading.value = true
  try {
    await new Promise((resolve) => setTimeout(resolve, 500))

    // Mock 数据 —— 联调后删除，改为接口赋值
    Object.assign(dashboardData, {
      stats: [
        {
          key: 'todayOrders',
          label: '今日订单数',
          value: 2654,
          displayValue: '2,654',
          trend: -50,
          compareLabel: '较昨日',
          icon: markRaw(Document),
          iconBg: '#ecf5ff',
          iconColor: '#409eff',
        },
        {
          key: 'todayNewUsers',
          label: '今日新增用户',
          value: 1652,
          displayValue: '1,652',
          trend: -10,
          compareLabel: '较昨日',
          icon: markRaw(User),
          iconBg: '#f0f9eb',
          iconColor: '#67c23a',
        },
        {
          key: 'pendingPayment',
          label: '待付款订单',
          value: 68,
          displayValue: '68',
          trend: 25,
          compareLabel: '较昨日',
          icon: markRaw(CreditCard),
          iconBg: '#fdf6ec',
          iconColor: '#e6a23c',
        },
        {
          key: 'todaySales',
          label: '今日销售额',
          value: 65658.87,
          displayValue: '¥65,658.87',
          trend: 50,
          compareLabel: '较昨日',
          icon: markRaw(Money),
          iconBg: '#fef0f0',
          iconColor: '#f56c6c',
        },
        {
          key: 'yesterdaySales',
          label: '昨日销售额',
          value: 45258.51,
          displayValue: '¥45,258.51',
          trend: 12,
          compareLabel: '较前日',
          icon: markRaw(Wallet),
          iconBg: '#f4f4f5',
          iconColor: '#909399',
        },
      ],
      pendingTasks: [
        { key: 'pendingShipment', label: '待发货订单', count: 154 },
        { key: 'pendingRefund', label: '待处理退款', count: 1524 },
        { key: 'pendingReceipt', label: '待确认收货', count: 16 },
        { key: 'pendingAfterSale', label: '待处理售后', count: 24 },
        { key: 'outOfStock', label: '缺货登记', count: 45 },
        { key: 'pendingVerify', label: '待核销订单', count: 487 },
        { key: 'pendingReview', label: '待评价回复', count: 25 },
        { key: 'adExpire', label: '广告位到期提醒', count: 8 },
      ],
      quickAccess: [
        {
          key: 'products',
          label: '商品管理',
          path: '/product/list',
          icon: markRaw(Goods),
          iconBg: '#ecf5ff',
          iconColor: '#409eff',
        },
        {
          key: 'orders',
          label: '订单管理',
          path: '/orders',
          icon: markRaw(List),
          iconBg: '#f0f9eb',
          iconColor: '#67c23a',
        },
        {
          key: 'users',
          label: '用户管理',
          path: '/users',
          icon: markRaw(User),
          iconBg: '#fdf6ec',
          iconColor: '#e6a23c',
        },
        {
          key: 'statistics',
          label: '交易统计',
          path: '/statistics/transactions',
          icon: markRaw(DataAnalysis),
          iconBg: '#fef0f0',
          iconColor: '#f56c6c',
        },
        {
          key: 'marketing',
          label: '广告管理',
          path: '/marketing/ads',
          icon: markRaw(Promotion),
          iconBg: '#f4f4f5',
          iconColor: '#909399',
        },
        {
          key: 'settings',
          label: '系统设置',
          path: '/settings',
          icon: markRaw(Setting),
          iconBg: '#ecf5ff',
          iconColor: '#409eff',
        },
      ],
      chart: {
        dates: ['07-22', '07-23', '07-24', '07-25', '07-26', '07-27', '07-28'],
        sales: [32000, 45200, 38100, 52400, 48900, 61200, 65600],
        orders: [180, 220, 195, 260, 240, 310, 265],
      },
    })

    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

/** 构建 ECharts 配置 */
const buildChartOption = () => {
  const { dates, sales, orders } = dashboardData.chart

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
    },
    legend: {
      data: ['销售额', '订单量'],
      top: 0,
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: 48,
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisLabel: { color: '#606266' },
    },
    yAxis: [
      {
        type: 'value',
        name: '销售额 (元)',
        axisLabel: {
          color: '#606266',
          formatter: (val) => (val >= 10000 ? `${val / 10000}w` : val),
        },
        splitLine: { lineStyle: { color: '#ebeef5' } },
      },
      {
        type: 'value',
        name: '订单量',
        axisLabel: { color: '#606266' },
        splitLine: { show: false },
      },
    ],
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        yAxisIndex: 0,
        data: sales,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: '#409eff' },
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.35)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' },
          ]),
        },
      },
      {
        name: '订单量',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: orders,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: '#67c23a' },
        itemStyle: { color: '#67c23a' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.25)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.03)' },
          ]),
        },
      },
    ],
  }
}

/** 初始化 / 更新图表 */
const renderChart = () => {
  if (!chartRef.value) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption(buildChartOption(), true)
}

/** 窗口尺寸变化时重绘图表 */
const handleResize = () => {
  chartInstance?.resize()
}

const handleQuickAccess = (path) => {
  router.push(path)
}

onMounted(() => {
  fetchDashboardData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<style scoped>
.dashboard-page {
  min-height: 100vh;
  padding: 20px;
  background-color: #f5f7fa;
}

.dashboard-row {
  margin-bottom: 16px;
}

.dashboard-row:last-child {
  margin-bottom: 0;
}

/* 统计卡片 */
.stat-card {
  margin-bottom: 16px;
  border-radius: 8px;
  border: none;
}

.stat-card :deep(.el-card__body) {
  padding: 18px 16px;
}

.stat-card__body {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.stat-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 8px;
}

.stat-card__content {
  flex: 1;
  min-width: 0;
}

.stat-card__label {
  margin: 0 0 6px;
  font-size: 13px;
  color: #909399;
  line-height: 1.4;
}

.stat-card__value {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-card__trend {
  margin: 0;
  font-size: 12px;
  line-height: 1.4;
}

.stat-card__compare {
  margin-left: 6px;
  color: #909399;
}

.trend-up {
  color: #67c23a;
  font-weight: 500;
}

.trend-down {
  color: #f56c6c;
  font-weight: 500;
}

/* 通用面板卡片 */
.panel-card {
  margin-bottom: 16px;
  border-radius: 8px;
  border: none;
}

.panel-card :deep(.el-card__header) {
  padding: 14px 20px;
  border-bottom: 1px solid #ebeef5;
}

.panel-card :deep(.el-card__body) {
  padding: 16px 20px 20px;
}

.panel-card__title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

/* 待处理事务 */
.pending-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0;
  margin: 0;
  padding: 0;
  list-style: none;
}

.pending-list__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 8px;
  border-bottom: 1px solid #f2f6fc;
}

.pending-list__label {
  font-size: 14px;
  color: #606266;
}

/* 快捷入口 */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px 12px;
}

.quick-grid__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.quick-grid__item:hover {
  background-color: #f5f7fa;
}

.quick-grid__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 8px;
}

.quick-grid__label {
  font-size: 13px;
  color: #606266;
  text-align: center;
}

/* 图表区域 */
.chart-card :deep(.el-card__body) {
  padding-top: 8px;
}

.chart-container {
  width: 100%;
  height: 380px;
}

/* 1280px 及以上：5 个统计卡片单行均分 */
@media (min-width: 1280px) {
  .dashboard-page {
    padding: 24px;
  }

  .stat-card {
    margin-bottom: 0;
  }

  .dashboard-row:first-child :deep(.el-col) {
    flex: 1;
    max-width: 20%;
  }
}

@media (max-width: 991px) {
  .quick-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 575px) {
  .pending-list {
    grid-template-columns: 1fr;
  }

  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
