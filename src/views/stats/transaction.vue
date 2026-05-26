<template>
  <div v-loading="loading" class="stats-page">
    <!-- Row 1: 10 KPI cards -->
    <div class="kpi-grid">
      <el-card v-for="item in kpiList" :key="item.key" shadow="hover" class="kpi-card">
        <div class="kpi-card__body">
          <div class="kpi-card__icon" :style="{ backgroundColor: item.iconBg }">
            <el-icon :size="20" :color="item.iconColor"><TrendCharts /></el-icon>
          </div>
          <div class="kpi-card__content">
            <p class="kpi-card__label">{{ item.label }}</p>
            <p class="kpi-card__value">{{ item.value }}</p>
            <p class="kpi-card__trend">
              <span>昨日 {{ item.yesterday }}</span>
              <span :class="item.trend >= 0 ? 'trend-up' : 'trend-down'">
                {{ item.trend >= 0 ? '↑' : '↓' }}{{ Math.abs(item.trend) }}%
              </span>
            </p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- Row 2: trend + funnel -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :lg="16">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <StatsCardHeader title="订单总览" @time-change="onTimeChange" />
          <div ref="trendRef" class="chart-box chart-box--lg" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <div class="panel-title">转化率</div>
          <div ref="funnelRef" class="chart-box chart-box--lg" />
          <ul class="funnel-legend">
            <li>下单转化率 <strong>5.63%</strong></li>
            <li>付款转化率 <strong>75.85%</strong></li>
            <li>成交转化率 <strong>2.85%</strong></li>
          </ul>
        </el-card>
      </el-col>
    </el-row>

    <!-- Row 3: price bar + user donut -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :lg="12">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <StatsCardHeader title="价格区间占比" @time-change="onTimeChange" />
          <div ref="priceBarRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <StatsCardHeader title="新老用户占比" @time-change="onTimeChange" />
          <div class="split-chart">
            <div ref="userPieRef" class="chart-box chart-box--sm" />
            <div class="user-detail">
              <div class="user-block">
                <h4>新用户</h4>
                <p>付款金额 <span>2654.56</span> <em class="trend-down">-16%</em></p>
                <p>付款人数 <span>35</span> <em class="trend-down">-16%</em></p>
              </div>
              <div class="user-block">
                <h4>老用户</h4>
                <p>付款金额 <span>6523.00</span> <em class="trend-down">-16%</em></p>
                <p>付款人数 <span>654</span> <em class="trend-down">-16%</em></p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Row 4: source + device -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :lg="12">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <StatsCardHeader title="来源占比" @time-change="onTimeChange" />
          <div class="split-chart">
            <div ref="sourcePieRef" class="chart-box chart-box--sm" />
            <div class="user-detail">
              <div v-for="s in sourceDetail" :key="s.name" class="user-block">
                <h4>{{ s.name }}</h4>
                <p>付款金额 <span>{{ s.amount }}</span> <em :class="s.trend >= 0 ? 'trend-up' : 'trend-down'">{{ s.trend }}%</em></p>
                <p>付款人数 <span>{{ s.count }}</span></p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <StatsCardHeader title="设备占比" @time-change="onTimeChange" />
          <div ref="deviceAreaRef" class="chart-box" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { TrendCharts } from '@element-plus/icons-vue'
import StatsCardHeader from '@/components/stats/StatsCardHeader.vue'
import { useChartGroup } from '@/composables/useChartGroup'
import { transactionKpis, transactionTrend, priceRangeSeries, dateLabels } from '@/mock/stats'

const loading = ref(false)
const chartLoading = ref(false)
const kpiList = reactive([...transactionKpis])
const statsData = reactive({ trend: { ...transactionTrend }, price: { ...priceRangeSeries } })

const trendRef = ref(null)
const funnelRef = ref(null)
const priceBarRef = ref(null)
const userPieRef = ref(null)
const sourcePieRef = ref(null)
const deviceAreaRef = ref(null)

const { initChart, setOption, resizeAll, disposeAll } = useChartGroup()

const sourceDetail = [
  { name: 'iOS', amount: '2654.56', count: 35, trend: -16 },
  { name: '安卓', amount: '6523.00', count: 654, trend: -16 },
  { name: '鸿蒙', amount: '1200.00', count: 88, trend: 12 },
]

const onTimeChange = () => fetchTransactionData()

/**
 * POST /api/stats/transaction/overview
 * 接口回调中通过 chart.setOption 动态刷新所有图表
 */
const fetchTransactionData = async () => {
  loading.value = true
  chartLoading.value = true
  try {
    await new Promise((r) => setTimeout(r, 500))
    Object.assign(statsData.trend, transactionTrend)
    await nextTick()
    initTransactionCharts()
  } finally {
    loading.value = false
    chartLoading.value = false
  }
}

const buildTrendOption = () => {
  const t = statsData.trend
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['付款金额', '退款金额', '付款人数', '付款订单数', '下单转化率', '付款转化率', '成交转化率'], top: 0, type: 'scroll' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: 56, containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: t.dates },
    yAxis: [
      { type: 'value', name: '金额' },
      { type: 'value', name: '比率%', max: 100 },
    ],
    series: [
      { name: '付款金额', type: 'line', smooth: true, data: t.paymentAmount, areaStyle: { opacity: 0.2 }, itemStyle: { color: '#409eff' } },
      { name: '退款金额', type: 'line', smooth: true, data: t.refundAmount, areaStyle: { opacity: 0.15 }, itemStyle: { color: '#f56c6c' } },
      { name: '付款人数', type: 'line', smooth: true, data: t.payUsers, itemStyle: { color: '#67c23a' } },
      { name: '付款订单数', type: 'line', smooth: true, data: t.payOrders, itemStyle: { color: '#e6a23c' } },
      { name: '下单转化率', type: 'line', smooth: true, yAxisIndex: 1, data: t.orderRate, itemStyle: { color: '#909399' } },
      { name: '付款转化率', type: 'line', smooth: true, yAxisIndex: 1, data: t.payRate, itemStyle: { color: '#9b59b6' } },
      { name: '成交转化率', type: 'line', smooth: true, yAxisIndex: 1, data: t.dealRate, itemStyle: { color: '#17a2b8' } },
    ],
  }
}

const buildFunnelOption = () => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'funnel',
    left: '10%',
    width: '80%',
    label: { formatter: '{b}' },
    data: [
      { value: 100, name: '浏览' },
      { value: 60, name: '下单' },
      { value: 45, name: '付款' },
    ],
    itemStyle: { borderColor: '#fff', borderWidth: 2 },
    color: ['#409eff', '#17a2b8', '#67c23a'],
  }],
})

const buildPriceBarOption = () => {
  const p = statsData.price
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: p.ranges, bottom: 0, type: 'scroll' },
    grid: { left: '3%', right: '4%', bottom: 48, top: 24, containLabel: true },
    xAxis: { type: 'category', data: p.dates },
    yAxis: { type: 'value' },
    series: p.ranges.map((name, i) => ({
      name,
      type: 'bar',
      stack: 'total',
      emphasis: { focus: 'series' },
      data: p.data[i],
    })),
  }
}

const buildUserPieOption = () => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie',
    radius: ['45%', '70%'],
    center: ['50%', '45%'],
    data: [{ name: '新用户', value: 35 }, { name: '老用户', value: 654 }],
    label: { show: false },
  }],
})

const buildSourcePieOption = () => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie',
    radius: ['45%', '70%'],
    center: ['50%', '50%'],
    data: [
      { name: 'iOS', value: 35 },
      { name: '安卓', value: 654 },
      { name: '鸿蒙', value: 88 },
    ],
  }],
})

const buildDeviceAreaOption = () => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['APP', '微信小程序', 'H5', 'PC站'], bottom: 0 },
  grid: { left: '3%', right: '4%', bottom: 40, top: 24, containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: dateLabels },
  yAxis: { type: 'value' },
  series: [
    { name: 'APP', type: 'line', stack: 'Total', smooth: true, areaStyle: {}, data: [120, 132, 101, 134, 90, 230, 210] },
    { name: '微信小程序', type: 'line', stack: 'Total', smooth: true, areaStyle: {}, data: [80, 92, 91, 94, 120, 130, 110] },
    { name: 'H5', type: 'line', stack: 'Total', smooth: true, areaStyle: {}, data: [60, 72, 71, 74, 90, 100, 80] },
    { name: 'PC站', type: 'line', stack: 'Total', smooth: true, areaStyle: {}, data: [40, 52, 51, 54, 60, 70, 50] },
  ],
})

const initTransactionCharts = () => {
  if (trendRef.value) {
    initChart(trendRef.value, 'trend')
    setOption('trend', buildTrendOption(), true)
  }
  if (funnelRef.value) {
    initChart(funnelRef.value, 'funnel')
    setOption('funnel', buildFunnelOption(), true)
  }
  if (priceBarRef.value) {
    initChart(priceBarRef.value, 'priceBar')
    setOption('priceBar', buildPriceBarOption(), true)
  }
  if (userPieRef.value) {
    initChart(userPieRef.value, 'userPie')
    setOption('userPie', buildUserPieOption(), true)
  }
  if (sourcePieRef.value) {
    initChart(sourcePieRef.value, 'sourcePie')
    setOption('sourcePie', buildSourcePieOption(), true)
  }
  if (deviceAreaRef.value) {
    initChart(deviceAreaRef.value, 'deviceArea')
    setOption('deviceArea', buildDeviceAreaOption(), true)
  }
  resizeAll()
}

const handleResize = () => resizeAll()

onMounted(() => {
  fetchTransactionData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeAll()
})
</script>

<style scoped>
.stats-page { padding: 16px; background: #f5f7fa; min-height: calc(100vh - 88px); }
.kpi-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; margin-bottom: 16px; }
@media (max-width: 1400px) { .kpi-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .kpi-grid { grid-template-columns: repeat(2, 1fr); } }
.kpi-card { border: none; border-radius: 8px; }
.kpi-card :deep(.el-card__body) { padding: 14px; }
.kpi-card__body { display: flex; gap: 10px; }
.kpi-card__icon { width: 40px; height: 40px; border-radius: 8px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.kpi-card__label { margin: 0 0 4px; font-size: 12px; color: #909399; }
.kpi-card__value { margin: 0 0 4px; font-size: 18px; font-weight: 600; color: #303133; }
.kpi-card__trend { margin: 0; font-size: 11px; color: #909399; display: flex; flex-direction: column; gap: 2px; }
.stats-row { margin-bottom: 16px; }
.panel-card { border: none; border-radius: 8px; margin-bottom: 0; }
.panel-title { font-size: 15px; font-weight: 600; padding-left: 10px; border-left: 3px solid #409eff; margin-bottom: 8px; }
.chart-box { width: 100%; height: 320px; }
.chart-box--lg { height: 360px; }
.chart-box--sm { height: 260px; flex: 1; min-width: 200px; }
.split-chart { display: flex; gap: 16px; align-items: center; }
.user-detail { flex: 1; font-size: 13px; color: #606266; }
.user-block { margin-bottom: 12px; }
.user-block h4 { margin: 0 0 6px; font-size: 14px; color: #303133; }
.user-block p { margin: 4px 0; }
.user-block span { color: #303133; font-weight: 500; }
.funnel-legend { list-style: none; padding: 0 16px 8px; margin: 0; font-size: 13px; color: #606266; }
.funnel-legend li { margin-bottom: 6px; }
.trend-up { color: #67c23a; font-style: normal; margin-left: 6px; }
.trend-down { color: #f56c6c; font-style: normal; margin-left: 6px; }
</style>
