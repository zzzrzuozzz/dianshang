<template>
  <div v-loading="loading" class="stats-page">
    <el-row :gutter="16" class="stats-row">
      <el-col v-for="item in kpiList" :key="item.key" :xs="24" :sm="12" :md="8" :lg="4" :xl="4">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-card__body">
            <div class="kpi-card__icon" :style="{ backgroundColor: item.iconBg }">
              <el-icon :size="20" :color="item.iconColor"><User /></el-icon>
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
      </el-col>
    </el-row>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :lg="16">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <div class="panel-head">
            <span class="panel-title">用户数据</span>
            <el-tabs v-model="userTab" class="inline-tabs" @tab-change="renderUserChart">
              <el-tab-pane label="新增用户" name="new" />
              <el-tab-pane label="活跃用户" name="active" />
              <el-tab-pane label="启动次数" name="launch" />
              <el-tab-pane label="累计用户" name="total" />
            </el-tabs>
            <div class="time-actions">
              <el-radio-group v-model="timeRange" size="small">
                <el-radio-button value="week">本周</el-radio-button>
                <el-radio-button value="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div ref="userTrendRef" class="chart-box chart-box--lg" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <div class="panel-head">
            <span class="panel-title">TOP版本</span>
            <el-tabs v-model="versionTab" class="inline-tabs" @tab-change="renderVersionChart">
              <el-tab-pane label="新增用户" name="new" />
              <el-tab-pane label="活跃用户" name="active" />
              <el-tab-pane label="累计用户" name="total" />
            </el-tabs>
          </div>
          <div ref="versionRoseRef" class="chart-box chart-box--lg" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :lg="12">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <div class="panel-head">
            <span class="panel-title">页面访问</span>
            <el-tabs v-model="pageTab" class="inline-tabs" @tab-change="renderPageChart">
              <el-tab-pane label="首页" name="home" />
              <el-tab-pane label="商品详情页" name="product" />
              <el-tab-pane label="banner" name="banner" />
              <el-tab-pane label="活动页" name="activity" />
            </el-tabs>
          </div>
          <div ref="pageTrendRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <div class="panel-head">
            <span class="panel-title">TOP渠道</span>
            <el-tabs v-model="channelTab" class="inline-tabs" @tab-change="renderChannelChart">
              <el-tab-pane label="新增用户" name="new" />
              <el-tab-pane label="活跃用户" name="active" />
              <el-tab-pane label="累计用户" name="total" />
            </el-tabs>
          </div>
          <div ref="channelRoseRef" class="chart-box" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { User } from '@element-plus/icons-vue'
import { useChartGroup } from '@/composables/useChartGroup'
import { flowKpis, userTrendData, versionRose, channelRose, dateLabels } from '@/mock/stats'

const loading = ref(false)
const chartLoading = ref(false)
const kpiList = reactive([...flowKpis])
const userTab = ref('new')
const versionTab = ref('new')
const pageTab = ref('home')
const channelTab = ref('new')
const timeRange = ref('week')

const userTrendRef = ref(null)
const versionRoseRef = ref(null)
const pageTrendRef = ref(null)
const channelRoseRef = ref(null)

const { initChart, setOption, resizeAll, disposeAll } = useChartGroup()

const buildAreaLine = (data, color = '#f56c6c') => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', top: 24, containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: dateLabels },
  yAxis: { type: 'value' },
  series: [{
    type: 'line',
    smooth: true,
    data,
    label: { show: true, position: 'top' },
    lineStyle: { color },
    itemStyle: { color },
    areaStyle: {
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(245, 108, 108, 0.35)' },
          { offset: 1, color: 'rgba(245, 108, 108, 0.05)' },
        ],
      },
    },
  }],
})

const buildRose = (data, colors) => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie',
    radius: [20, 100],
    center: ['50%', '45%'],
    roseType: 'radius',
    itemStyle: { borderRadius: 4 },
    data,
    color: colors,
  }],
})

const renderUserChart = () => {
  if (!userTrendRef.value) return
  initChart(userTrendRef.value, 'userTrend')
  setOption('userTrend', buildAreaLine(userTrendData, '#f56c6c'), true)
}

const renderVersionChart = () => {
  if (!versionRoseRef.value) return
  initChart(versionRoseRef.value, 'versionRose')
  setOption('versionRose', buildRose(versionRose, ['#409eff', '#17a2b8', '#67c23a', '#e6a23c', '#f56c6c']), true)
}

const renderPageChart = () => {
  if (!pageTrendRef.value) return
  initChart(pageTrendRef.value, 'pageTrend')
  setOption('pageTrend', buildAreaLine(userTrendData, '#e6a23c'), true)
}

const renderChannelChart = () => {
  if (!channelRoseRef.value) return
  initChart(channelRoseRef.value, 'channelRose')
  setOption('channelRose', buildRose(channelRose, ['#ff9f7f', '#ffdb5c', '#fb7293', '#e062ae', '#e690d1']), true)
}

const initFlowCharts = () => {
  renderUserChart()
  renderVersionChart()
  renderPageChart()
  renderChannelChart()
  resizeAll()
}

/**
 * POST /api/stats/flow/report
 */
const fetchFlowStatistics = async () => {
  loading.value = true
  chartLoading.value = true
  try {
    await new Promise((r) => setTimeout(r, 500))
    await nextTick()
    initFlowCharts()
  } finally {
    loading.value = false
    chartLoading.value = false
  }
}

const handleResize = () => resizeAll()

onMounted(() => {
  fetchFlowStatistics()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeAll()
})
</script>

<style scoped>
.stats-page { padding: 16px; background: #f5f7fa; min-height: calc(100vh - 88px); }
.stats-row { margin-bottom: 16px; }
.kpi-card { border: none; border-radius: 8px; margin-bottom: 16px; }
.kpi-card :deep(.el-card__body) { padding: 14px; }
.kpi-card__body { display: flex; gap: 10px; }
.kpi-card__icon { width: 40px; height: 40px; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.kpi-card__label { margin: 0 0 4px; font-size: 12px; color: #909399; }
.kpi-card__value { margin: 0 0 4px; font-size: 18px; font-weight: 600; color: #303133; }
.kpi-card__trend { margin: 0; font-size: 11px; color: #909399; }
.panel-card { border: none; border-radius: 8px; }
.panel-head { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; margin-bottom: 8px; }
.panel-title { font-size: 15px; font-weight: 600; padding-left: 10px; border-left: 3px solid #409eff; }
.inline-tabs :deep(.el-tabs__header) { margin: 0; }
.time-actions { margin-left: auto; }
.chart-box { width: 100%; height: 320px; }
.chart-box--lg { height: 360px; }
.trend-up { color: #67c23a; }
.trend-down { color: #f56c6c; }
</style>
