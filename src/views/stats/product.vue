<template>
  <div v-loading="loading" class="stats-page">
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :lg="12">
        <el-card v-loading="tableLoading" shadow="hover" class="panel-card">
          <StatsCardHeader title="一级类目分析" @time-change="fetchCategoryStats" />
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
            />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card v-loading="chartLoading" shadow="hover" class="panel-card">
          <StatsCardHeader title="一级类目分析图标" @time-change="fetchCategoryStats" />
          <div ref="categoryPieRef" class="chart-box chart-box--lg" />
        </el-card>
      </el-col>
    </el-row>

    <el-card v-loading="tableLoading" shadow="hover" class="panel-card">
      <StatsCardHeader title="商品销售分析" @time-change="fetchProductRankingList" />
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
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import StatsCardHeader from '@/components/stats/StatsCardHeader.vue'
import { useChartGroup } from '@/composables/useChartGroup'
import { categoryTable as mockCategoryTable, categoryPie, productRanking } from '@/mock/stats'

const loading = ref(false)
const tableLoading = ref(false)
const chartLoading = ref(false)

const categoryTable = ref([])
const productTable = ref([])
const catPage = ref(1)
const catPageSize = ref(10)
const catTotal = ref(265)
const rankPage = ref(1)
const rankPageSize = ref(10)
const rankTotal = ref(265)

const categoryPieRef = ref(null)
const { initChart, setOption, resizeAll, disposeAll } = useChartGroup()

const buildCategoryPieOption = () => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, type: 'scroll' },
  series: [{
    type: 'pie',
    radius: ['35%', '58%'],
    center: ['50%', '42%'],
    data: categoryPie,
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

/**
 * POST /api/stats/product/category
 */
const fetchCategoryStats = async () => {
  tableLoading.value = true
  chartLoading.value = true
  try {
    await new Promise((r) => setTimeout(r, 400))
    categoryTable.value = [...mockCategoryTable]
    await nextTick()
    renderCategoryPie()
  } finally {
    tableLoading.value = false
    chartLoading.value = false
  }
}

/**
 * POST /api/stats/product/ranking
 */
const fetchProductRankingList = async () => {
  tableLoading.value = true
  try {
    await new Promise((r) => setTimeout(r, 400))
    productTable.value = [...productRanking]
  } finally {
    tableLoading.value = false
  }
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

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeAll()
})
</script>

<style scoped>
.stats-page { padding: 16px; background: #f5f7fa; min-height: calc(100vh - 88px); }
.stats-row { margin-bottom: 16px; }
.panel-card { border: none; border-radius: 8px; margin-bottom: 16px; }
.chart-box { width: 100%; height: 380px; }
.chart-box--lg { height: 420px; }
.pagination-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; font-size: 13px; color: #606266; }
</style>
