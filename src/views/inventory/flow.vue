<template>
  <div v-loading="loading" class="flow-page">
    <el-alert
      v-if="filterGoodsId"
      type="info"
      :closable="false"
      show-icon
      class="filter-tip"
    >
      当前仅展示商品 ID：{{ filterGoodsId }} 的库存流水
      <el-button type="primary" link @click="clearGoodsFilter">查看全部</el-button>
    </el-alert>

    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="流水号/单号">
          <el-input
            v-model="searchForm.flowNo"
            placeholder="请输入订单号或流水号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="商品">
          <el-input
            v-model="searchForm.product"
            placeholder="请输入商品名称"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select
            v-model="searchForm.bizType"
            placeholder="请选择"
            clearable
            style="width: 140px"
          >
            <el-option
              v-for="opt in flowTypeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <div class="toolbar">
        <el-tabs v-model="activeTab" @tab-change="onTabChange">
          <el-tab-pane
            v-for="tab in flowTabs"
            :key="tab.key"
            :name="tab.key"
            :label="tabLabel(tab)"
          />
        </el-tabs>
        <el-button type="primary" :loading="exporting" @click="handleExport">导出数据</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card table-card">
      <el-table :data="tableData" border stripe>
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="流水编号" width="100" align="center" />
        <el-table-column prop="relatedNo" label="相关单号" width="120" align="center">
          <template #default="{ row }">
            {{ row.relatedNo || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="商品信息" min-width="200">
          <template #default="{ row }">
            <div class="product-info">
              <el-image :src="row.thumb" fit="cover" class="product-thumb" />
              <span class="product-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="skuName" label="SKU规格" width="130" show-overflow-tooltip />
        <el-table-column label="变动类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="flowTypeMap[row.type]?.type" size="small">
              {{ flowTypeMap[row.type]?.label || row.type }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="beforeQty" label="变动前数量" width="100" align="center" />
        <el-table-column label="变动数量" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.changeQty >= 0 ? 'qty-in' : 'qty-out'">
              {{ row.changeQty >= 0 ? `+${row.changeQty}` : row.changeQty }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="afterQty" label="变动后数量" width="100" align="center" />
        <el-table-column prop="operator" label="操作人账号" width="110" align="center" />
        <el-table-column prop="time" label="操作时间" width="150" />
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />

        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.orderId"
              type="primary"
              link
              @click="viewOrder(row)"
            >
              查看订单
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <span class="page-info">
          第{{ pagination.page }}页 共{{ pagination.totalPages }}页 {{ pagination.total }}条
        </span>
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="prev, pager, next, sizes"
          background
          @current-change="fetchInventoryFlow"
          @size-change="onPageSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  exportInventoryFlow,
  fetchInventoryFlow as loadInventoryFlow,
  flowTypeMap,
  flowTypeOptions,
} from '@/api/inventory'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const exporting = ref(false)
const activeTab = ref('all')
const tableData = ref([])

const filterGoodsId = computed(() => route.query.goodsId || '')

const flowTabs = ref([
  { key: 'all', label: '全部', count: 0 },
  { key: 'sales_out', label: '发货', count: 0 },
  { key: 'return_in', label: '退货', count: 0 },
  { key: 'manual_out', label: '手动出库', count: 0 },
  { key: 'manual_in', label: '手动入库', count: 0 },
  { key: 'reissue', label: '补发', count: 0 },
])

const searchForm = reactive({
  flowNo: '',
  product: '',
  bizType: '',
  dateRange: null,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  totalPages: 1,
})

const tabLabel = (tab) => (tab.key === 'all' ? `全部(${tab.count})` : tab.label)

const buildQuery = () => ({
  flowNo: searchForm.flowNo || undefined,
  product: searchForm.product || undefined,
  bizType: searchForm.bizType || undefined,
  tab: activeTab.value,
  goodsId: filterGoodsId.value || undefined,
  startDate: searchForm.dateRange?.[0],
  endDate: searchForm.dateRange?.[1],
  page: pagination.page,
  pageSize: pagination.pageSize,
})

const fetchInventoryFlow = async () => {
  loading.value = true
  try {
    const data = await loadInventoryFlow(buildQuery())
    tableData.value = data.list
    pagination.total = data.total
    pagination.totalPages = data.totalPages
    if (data.tabs?.length) {
      flowTabs.value = data.tabs
    }
  } finally {
    loading.value = false
  }
}

const onTabChange = () => {
  pagination.page = 1
  searchForm.bizType = ''
  fetchInventoryFlow()
}

const onPageSizeChange = () => {
  pagination.page = 1
  fetchInventoryFlow()
}

const handleSearch = () => {
  pagination.page = 1
  fetchInventoryFlow()
}

const handleReset = () => {
  Object.assign(searchForm, {
    flowNo: '',
    product: '',
    bizType: '',
    dateRange: null,
  })
  activeTab.value = 'all'
  pagination.page = 1
  fetchInventoryFlow()
}

const handleExport = async () => {
  exporting.value = true
  try {
    const { page, pageSize, ...rest } = buildQuery()
    await exportInventoryFlow(rest)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const clearGoodsFilter = () => {
  router.replace({ path: '/inventory/flow' })
}

const viewOrder = (row) => {
  if (row.orderId) {
    router.push(`/order/detail/${row.orderId}`)
  }
}

onMounted(fetchInventoryFlow)

watch(
  () => route.query.goodsId,
  () => {
    pagination.page = 1
    fetchInventoryFlow()
  },
)
</script>

<style scoped>
.flow-page {
  min-height: calc(100vh - 120px);
}

.filter-tip {
  margin-bottom: 12px;
}

.panel-card {
  margin-bottom: 12px;
  border-radius: 8px;
  border: none;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.search-actions {
  margin-left: auto;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-thumb {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  flex-shrink: 0;
}

.product-name {
  font-size: 13px;
  color: #303133;
}

.qty-in {
  color: #67c23a;
  font-weight: 600;
}

.qty-out {
  color: #409eff;
  font-weight: 600;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

.page-info {
  font-size: 13px;
  color: #606266;
}
</style>
