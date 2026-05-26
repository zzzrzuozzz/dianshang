<template>
  <div v-loading="loading" class="stats-page">
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

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :lg="16">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <StatsCardHeader title="订单总览" @time-change="onTimeChange" @custom-change="onCustomChange" />
          <div ref="trendRef" class="chart-box chart-box--lg" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <div class="panel-title">转化率</div>
          <div ref="funnelRef" class="chart-box chart-box--lg" />
          <ul class="funnel-legend">
            <li>下单转化率 <strong>{{ funnelRates.orderRate }}%</strong></li>
            <li>付款转化率 <strong>{{ funnelRates.payRate }}%</strong></li>
            <li>成交转化率 <strong>{{ funnelRates.dealRate }}%</strong></li>
          </ul>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :lg="12">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <StatsCardHeader title="价格区间占比" @time-change="onTimeChange" @custom-change="onCustomChange" />
          <div ref="priceBarRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <StatsCardHeader title="新老用户占比" @time-change="onTimeChange" @custom-change="onCustomChange" />
          <div class="split-chart">
            <div ref="userPieRef" class="chart-box chart-box--sm" />
            <div class="user-detail">
              <div class="user-block">
                <h4>新用户</h4>
                <p>付款金额 <span>{{ newUser.payAmount }}</span>
                  <em :class="newUser.payAmountTrend >= 0 ? 'trend-up' : 'trend-down'">{{ newUser.payAmountTrend }}%</em>
                </p>
                <p>付款人数 <span>{{ newUser.payUsers }}</span>
                  <em :class="newUser.payUsersTrend >= 0 ? 'trend-up' : 'trend-down'">{{ newUser.payUsersTrend }}%</em>
                </p>
              </div>
              <div class="user-block">
                <h4>老用户</h4>
                <p>付款金额 <span>{{ oldUser.payAmount }}</span>
                  <em :class="oldUser.payAmountTrend >= 0 ? 'trend-up' : 'trend-down'">{{ oldUser.payAmountTrend }}%</em>
                </p>
                <p>付款人数 <span>{{ oldUser.payUsers }}</span>
                  <em :class="oldUser.payUsersTrend >= 0 ? 'trend-up' : 'trend-down'">{{ oldUser.payUsersTrend }}%</em>
                </p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :lg="12">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <StatsCardHeader title="来源占比" @time-change="onTimeChange" @custom-change="onCustomChange" />
          <div class="split-chart">
            <div ref="sourcePieRef" class="chart-box chart-box--sm" />
            <div class="user-detail">
              <div v-for="s in sourceDetail" :key="s.name" class="user-block">
                <h4>{{ s.name }}</h4>
                <p>付款金额 <span>{{ s.amount }}</span>
                  <em :class="s.trend >= 0 ? 'trend-up' : 'trend-down'">{{ s.trend }}%</em>
                </p>
                <p>付款人数 <span>{{ s.count }}</span></p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <StatsCardHeader title="设备占比" @time-change="onTimeChange" @custom-change="onCustomChange" />
          <div ref="deviceAreaRef" class="chart-box" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onActivated, onBeforeUnmount, nextTick } from 'vue'
import { TrendCharts } from '@element-plus/icons-vue'
import StatsCardHeader from '@/components/stats/StatsCardHeader.vue'
import { useChartGroup } from '@/composables/useChartGroup'
import { fetchTransactionOverview } from '@/api/stats'

const loading = ref(false)
const chartLoading = ref(false)
const kpiList = reactive([])
const funnelRates = reactive({ orderRate: 0, payRate: 0, dealRate: 0 })
const newUser = reactive({ payAmount: '0', payAmountTrend: 0, payUsers: 0, payUsersTrend: 0 })
const oldUser = reactive({ payAmount: '0', payAmountTrend: 0, payUsers: 0, payUsersTrend: 0 })
const sourceDetail = ref([])

const statsData = reactive({
  trend: { dates: [], paymentAmount: [], refundAmount: [], payUsers: [], payOrders: [], orderRate: [], payRate: [], dealRate: [] },
  price: { dates: [], ranges: [], data: [] },
  funnel: [],
  userPie: [],
  sourcePie: [],
  device: { dates: [], channels: [], series: [] },
})

const query = reactive({ range: 'week', startDate: '', endDate: '' })

const trendRef = ref(null)
const funnelRef = ref(null)
const priceBarRef = ref(null)
const userPieRef = ref(null)
const sourcePieRef = ref(null)
const deviceAreaRef = ref(null)

const { initChart, setOption, resizeAll, disposeAll } = useChartGroup()

const formatDate = (d) => {
  if (!d) return ''
  const dt = d instanceof Date ? d : new Date(d)
  const y = dt.getFullYear()
  const m = String(dt.getMonth() + 1).padStart(2, '0')
  const day = String(dt.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const onTimeChange = (range) => {
  query.range = range || 'week'
  query.startDate = ''
  query.endDate = ''
  fetchTransactionData()
}

const onCustomChange = (range) => {
  if (!range || range.length !== 2) return
  query.startDate = formatDate(range[0])
  query.endDate = formatDate(range[1])
  fetchTransactionData()
}

const fetchTransactionData = async () => {
  loading.value = true
  chartLoading.value = true
  try {
    const data = await fetchTransactionOverview({
      range: query.startDate ? undefined : query.range,
      startDate: query.startDate || undefined,
      endDate: query.endDate || undefined,
    })
    kpiList.splice(0, kpiList.length, ...(data.kpis || []))
    Object.assign(statsData.trend, data.trend || {})
    Object.assign(statsData.price, data.priceRange || {})
    statsData.funnel = data.funnel || []
    statsData.userPie = data.userPie || []
    statsData.sourcePie = data.sourcePie || []
    Object.assign(statsData.device, data.deviceSeries || {})
    Object.assign(funnelRates, data.funnelRates || {})
    Object.assign(newUser, data.newUser || {})
    Object.assign(oldUser, data.oldUser || {})
    sourceDetail.value = data.sourceDetail || []
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
    data: statsData.funnel.map((f) => ({ name: f.name, value: f.value })),
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
    series: (p.ranges || []).map((name, i) => ({
      name,
      type: 'bar',
      stack: 'total',
      emphasis: { focus: 'series' },
      data: (p.data || [])[i] || [],
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
    data: statsData.userPie.map((u) => ({ name: u.name, value: u.value })),
    label: { show: false },
  }],
})

const buildSourcePieOption = () => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie',
    radius: ['45%', '70%'],
    center: ['50%', '50%'],
    data: statsData.sourcePie.map((s) => ({ name: s.name, value: s.value })),
  }],
})

const buildDeviceAreaOption = () => {
  const d = statsData.device
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: d.channels, bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: 40, top: 24, containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: d.dates },
    yAxis: { type: 'value' },
    series: (d.channels || []).map((name, i) => ({
      name,
      type: 'line',
      stack: 'Total',
      smooth: true,
      areaStyle: {},
      data: (d.series || [])[i] || [],
    })),
  }
}

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

onActivated(() => {
  fetchTransactionData()
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
