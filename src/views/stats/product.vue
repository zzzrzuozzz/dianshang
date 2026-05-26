<template>
  <div v-loading="loading" class="stats-page">
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :lg="12">
        <el-card v-loading="tableLoading" shadow="hover" class="panel-card">
          <StatsCardHeader title="一级类目分析" @time-change="onTimeChange" @custom-change="onCustomChange" />
          <el-table :data="categoryTable" border stripe size="small">
            <el-table-column prop="name" label="分类名称" min-width="90" />
            <el-table-column prop="qty" label="销售数量" width="90" align="center" />
            <el-table-column prop="qtyRate" label="数量比例" width="90" align="center" />
            <el-table-column prop="amount" label="销售金额" width="110" align="center" />
            <el-table-column prop="amountRate" label="金额比例" width="90" align="center" />
          </el-table>
          <div class="pagination-bar">
            <el-pagination
              v-model:current-page="catPage"
              v-model:page-size="catPageSize"
              :total="catTotal"
              layout="prev, pager, next, sizes"
              background
              small
              @current-change="fetchCategoryStats"
              @size-change="onCatSizeChange"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <StatsCardHeader title="一级类目分析图标" @time-change="onTimeChange" @custom-change="onCustomChange" />
          <div ref="categoryPieRef" class="chart-box chart-box--lg" />
        </el-card>
      </el-col>
    </el-row>

    <el-card v-loading="tableLoading" shadow="hover" class="panel-card">
      <StatsCardHeader title="商品销售分析" @time-change="onTimeChange" @custom-change="onCustomChange" />
      <el-table :data="productTable" border stripe>
        <el-table-column prop="name" label="商品名称" min-width="120" />
        <el-table-column prop="pv" label="浏览量" width="90" align="center" />
        <el-table-column prop="uv" label="浏览人数" width="90" align="center" />
        <el-table-column prop="payUsers" label="付款人数" width="90" align="center" />
        <el-table-column prop="conversion" label="转化率" width="90" align="center" />
        <el-table-column prop="salesQty" label="销售数量" width="90" align="center" />
        <el-table-column prop="salesAmount" label="销售金额" width="110" align="center" />
      </el-table>
      <div class="pagination-bar">
        <span>共 {{ rankTotal }} 条</span>
        <el-pagination
          v-model:current-page="rankPage"
          v-model:page-size="rankPageSize"
          :page-sizes="[10, 20, 50]"
          :total="rankTotal"
          layout="prev, pager, next, sizes"
          background
          @current-change="fetchProductRankingList"
          @size-change="onRankSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated, onBeforeUnmount, nextTick } from 'vue'
import StatsCardHeader from '@/components/stats/StatsCardHeader.vue'
import { useChartGroup } from '@/composables/useChartGroup'
import { fetchProductCategory, fetchProductRanking } from '@/api/stats'

const loading = ref(false)
const tableLoading = ref(false)
const chartLoading = ref(false)

const categoryTable = ref([])
const categoryPie = ref([])
const productTable = ref([])
const catPage = ref(1)
const catPageSize = ref(10)
const catTotal = ref(0)
const rankPage = ref(1)
const rankPageSize = ref(10)
const rankTotal = ref(0)

const query = ref({ range: 'week', startDate: '', endDate: '' })

const categoryPieRef = ref(null)
const { initChart, setOption, resizeAll, disposeAll } = useChartGroup()

const formatDate = (d) => {
  if (!d) return ''
  const dt = d instanceof Date ? d : new Date(d)
  const y = dt.getFullYear()
  const m = String(dt.getMonth() + 1).padStart(2, '0')
  const day = String(dt.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const statsParams = () => ({
  range: query.value.startDate ? undefined : query.value.range,
  startDate: query.value.startDate || undefined,
  endDate: query.value.endDate || undefined,
})

const onTimeChange = (range) => {
  query.value = { range: range || 'week', startDate: '', endDate: '' }
  catPage.value = 1
  rankPage.value = 1
  loadAll()
}

const onCustomChange = (range) => {
  if (!range || range.length !== 2) return
  query.value = {
    range: 'week',
    startDate: formatDate(range[0]),
    endDate: formatDate(range[1]),
  }
  catPage.value = 1
  rankPage.value = 1
  loadAll()
}

const buildCategoryPieOption = () => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, type: 'scroll' },
  series: [{
    type: 'pie',
    radius: ['35%', '58%'],
    center: ['50%', '42%'],
    data: categoryPie.value.map((c) => ({ name: c.name, value: c.value })),
    label: {
      formatter: '{b}\n{c}',
      lineHeight: 16,
    },
    labelLine: { length: 12, length2: 8 },
  }],
})

const renderCategoryPie = () => {
  if (!categoryPieRef.value) return
  initChart(categoryPieRef.value, 'categoryPie')
  setOption('categoryPie', buildCategoryPieOption(), true)
}

const fetchCategoryStats = async () => {
  tableLoading.value = true
  chartLoading.value = true
  try {
    const data = await fetchProductCategory({
      ...statsParams(),
      page: catPage.value,
      pageSize: catPageSize.value,
    })
    categoryTable.value = data.list || []
    categoryPie.value = data.pie || []
    catTotal.value = data.total || 0
    await nextTick()
    renderCategoryPie()
  } finally {
    tableLoading.value = false
    chartLoading.value = false
  }
}

const fetchProductRankingList = async () => {
  tableLoading.value = true
  try {
    const data = await fetchProductRanking({
      ...statsParams(),
      page: rankPage.value,
      pageSize: rankPageSize.value,
    })
    productTable.value = data.list || []
    rankTotal.value = data.total || 0
  } finally {
    tableLoading.value = false
  }
}

const onCatSizeChange = () => {
  catPage.value = 1
  fetchCategoryStats()
}

const onRankSizeChange = () => {
  rankPage.value = 1
  fetchProductRankingList()
}

const loadAll = async () => {
  loading.value = true
  try {
    await Promise.all([fetchCategoryStats(), fetchProductRankingList()])
  } finally {
    loading.value = false
  }
}

const handleResize = () => resizeAll()

onMounted(() => {
  loadAll()
  window.addEventListener('resize', handleResize)
})

onActivated(() => {
  loadAll()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeAll()
})
</script>

<style scoped>
.stats-page { padding: 16px; background: #f5f7fa; min-height: calc(100vh - 88px); }
.stats-row { margin-bottom: 16px; }
.panel-card { border: none; border-radius: 8px; }
.chart-box { width: 100%; height: 320px; }
.chart-box--lg { height: 360px; }
.pagination-bar { display: flex; align-items: center; justify-content: flex-end; gap: 12px; margin-top: 12px; }
</style>
