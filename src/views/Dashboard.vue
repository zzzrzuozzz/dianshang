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
              @click="handlePendingTask(task)"
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
import { reactive, ref, onMounted, onActivated, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { fetchDashboardOverview } from '@/api/dashboard'
import { resolveDashboardIcon } from '@/utils/dashboardIcons'

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

function mapOverviewItems(items) {
  return items.map((item) => ({
    ...item,
    icon: resolveDashboardIcon(item.iconKey),
  }))
}

/** GET /api/dashboard/overview */
const fetchDashboardData = async () => {
  loading.value = true
  try {
    const data = await fetchDashboardOverview()
    dashboardData.stats = mapOverviewItems(data.stats || [])
    dashboardData.pendingTasks = data.pendingTasks || []
    dashboardData.quickAccess = mapOverviewItems(data.quickAccess || [])
    dashboardData.chart = data.chart || { dates: [], sales: [], orders: [] }
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

const PENDING_ROUTES = {
  pendingShipment: '/order/list',
  pendingRefund: '/order/after-sale',
  pendingReceipt: '/order/confirm',
  pendingAfterSale: '/order/after-sale',
  pendingWithdraw: '/finance/withdraw',
}

const handlePendingTask = (task) => {
  const path = PENDING_ROUTES[task.key]
  if (path) router.push(path)
}

onMounted(() => {
  fetchDashboardData()
  window.addEventListener('resize', handleResize)
})

onActivated(() => {
  fetchDashboardData()
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
  cursor: pointer;
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
