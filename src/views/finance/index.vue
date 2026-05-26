<template>
  <div v-loading="loading" class="finance-page">
    <el-row :gutter="16" class="finance-row">
      <el-col v-for="item in overview.kpis" :key="item.key" :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="kpi-card" :class="`kpi-card--${item.key}`">
          <div class="kpi-card__body">
            <div class="kpi-card__icon" :style="{ backgroundColor: item.iconBg }">
              <el-icon :size="22" :color="item.iconColor">
                <component :is="iconMap[item.iconKey] || Wallet" />
              </el-icon>
            </div>
            <div class="kpi-card__content">
              <div class="kpi-card__head">
                <span class="kpi-card__label">{{ item.label }}</span>
                <el-tag v-if="item.tag" size="small" :type="item.tagType" effect="plain">{{ item.tag }}</el-tag>
              </div>
              <p class="kpi-card__value">{{ item.display }}</p>
              <p v-if="item.key === 'today'" class="kpi-card__trend">
                <span :class="item.trend >= 0 ? 'up' : 'down'">
                  {{ item.trend >= 0 ? '↑' : '↓' }}{{ Math.abs(item.trend) }}%
                </span>
                <span class="muted">较昨日</span>
              </p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="panel-card finance-row">
      <div class="panel-head">
        <span class="panel-title">资金走势</span>
        <el-radio-group v-model="granularity" size="small" @change="loadOverview">
          <el-radio-button value="day">按日</el-radio-button>
          <el-radio-button value="month">按月</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="chartRef" class="chart-box" />
    </el-card>

    <el-card shadow="hover" class="panel-card">
      <template #header>
        <div class="panel-head">
          <span class="panel-title">最新待审核提现</span>
          <el-button type="primary" link @click="router.push('/finance/withdraw')">前往审批中心</el-button>
        </div>
      </template>
      <el-table :data="overview.pendingWithdraws" border stripe size="small">
        <el-table-column prop="applyNo" label="申请编号" width="180" />
        <el-table-column prop="memberName" label="申请人" min-width="120" />
        <el-table-column prop="applyAmount" label="申请金额" width="120" align="right" />
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="goWithdraw(row.applyNo)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!overview.pendingWithdraws.length" description="暂无待审核提现" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onActivated, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { Wallet, Clock, TrendCharts, RefreshLeft, Warning } from '@element-plus/icons-vue'
import { fetchFinanceOverview, reconcileFinance } from '@/api/finance'

const router = useRouter()

const goWithdraw = (applyNo) => {
  router.push({ path: '/finance/withdraw', query: applyNo ? { applyNo } : {} })
}

const loading = ref(false)
const granularity = ref('day')
const chartRef = ref(null)
let chartInstance = null

const iconMap = { Wallet, Clock, TrendCharts, RefreshLeft, Warning }

const overview = reactive({
  kpis: [],
  chart: { dates: [], incomeSeries: [], refundSeries: [] },
  pendingWithdraws: [],
})

/** 接口返回聚合大盘与时序 List，驱动 ECharts 重绘 */
const loadOverview = async () => {
  loading.value = true
  try {
    const data = await fetchFinanceOverview(granularity.value)
    overview.kpis = data.kpis || []
    overview.chart = data.chart || { dates: [], incomeSeries: [], refundSeries: [] }
    overview.pendingWithdraws = data.pendingWithdraws || []
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

const renderChart = () => {
  if (!chartRef.value) return
  if (!chartInstance) chartInstance = echarts.init(chartRef.value)
  const c = overview.chart
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入流水走势', '退款流出走势'], top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', top: 48, containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: c.dates },
    yAxis: { type: 'value', name: '金额(元)' },
    series: [
      {
        name: '收入流水走势',
        type: 'line',
        smooth: true,
        data: c.incomeSeries,
        areaStyle: { color: 'rgba(64, 158, 255, 0.15)' },
        itemStyle: { color: '#409eff' },
      },
      {
        name: '退款流出走势',
        type: 'line',
        smooth: true,
        data: c.refundSeries,
        areaStyle: { color: 'rgba(245, 108, 108, 0.12)' },
        itemStyle: { color: '#f56c6c' },
      },
    ],
  }, true)
}

const onResize = () => chartInstance?.resize()

const bootstrap = async () => {
  try {
    await reconcileFinance()
  } catch {
    /* 对账失败不阻断看板展示 */
  }
  await loadOverview()
}

onMounted(() => {
  bootstrap()
  window.addEventListener('resize', onResize)
})

onActivated(() => {
  loadOverview()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.finance-page { padding: 4px; }
.finance-row { margin-bottom: 16px; }
.kpi-card { border: none; border-radius: 10px; border-top: 3px solid #409eff; }
.kpi-card--pending { border-top-color: #e6a23c; }
.kpi-card--today { border-top-color: #67c23a; }
.kpi-card--refund { border-top-color: #f56c6c; }
.kpi-card__body { display: flex; gap: 14px; align-items: flex-start; }
.kpi-card__icon {
  width: 48px; height: 48px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.kpi-card__head { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.kpi-card__label { font-size: 13px; color: #909399; }
.kpi-card__value { margin: 0; font-size: 22px; font-weight: 700; color: #1a3a5c; font-family: 'DIN Alternate', sans-serif; }
.kpi-card__trend { margin: 6px 0 0; font-size: 12px; }
.kpi-card__trend .up { color: #67c23a; }
.kpi-card__trend .down { color: #f56c6c; }
.kpi-card__trend .muted { color: #909399; margin-left: 6px; }
.panel-card { border: none; border-radius: 10px; }
.panel-head { display: flex; align-items: center; justify-content: space-between; }
.panel-title {
  font-size: 15px; font-weight: 600; color: #303133;
  padding-left: 10px; border-left: 3px solid #409eff;
}
.chart-box { width: 100%; height: 360px; }
</style>
