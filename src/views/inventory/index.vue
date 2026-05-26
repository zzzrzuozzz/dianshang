<template>
  <div v-loading="loading" class="inventory-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入名称或编码"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-cascader
            v-model="searchForm.category"
            :options="categoryCascaderOptions"
            placeholder="请选择分类"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="供应商">
          <el-select
            v-model="searchForm.supplier"
            placeholder="请选择供应商"
            clearable
            style="width: 140px"
          >
            <el-option
              v-for="opt in supplierOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="searchForm.stockStatus" placeholder="全部" style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="充足" value="sufficient" />
            <el-option label="预警" value="warning" />
            <el-option label="断货" value="out" />
          </el-select>
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Tabs + 操作按钮 -->
    <el-card shadow="never" class="panel-card">
      <div class="toolbar">
        <el-tabs v-model="activeTab" @tab-change="fetchInventoryList">
          <el-tab-pane label="全部库存" name="all">
            <template #label>全部库存({{ tabCounts.all }})</template>
          </el-tab-pane>
          <el-tab-pane label="预警商品" name="warning">
            <template #label>预警商品({{ tabCounts.warning }})</template>
          </el-tab-pane>
          <el-tab-pane label="已售罄商品" name="out">
            <template #label>已售罄商品({{ tabCounts.out }})</template>
          </el-tab-pane>
        </el-tabs>
        <div class="toolbar-actions">
          <el-button type="primary" @click="handleBatchAdjust">批量调整库存</el-button>
          <el-button type="primary" plain>导出库存报表</el-button>
        </div>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="panel-card table-card">
      <el-table
        :data="tableData"
        border
        stripe
        :row-class-name="rowClassName"
        @selection-change="(rows) => (selectedRows = rows)"
      >
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="商品编号" width="100" align="center" />

        <el-table-column label="商品图片" width="80" align="center">
          <template #default="{ row }">
            <el-image :src="row.thumb" fit="cover" class="product-thumb" />
          </template>
        </el-table-column>

        <el-table-column label="商品名称" min-width="200">
          <template #default="{ row }">
            <div>
              <p class="product-name">{{ row.name }}</p>
              <p v-if="row.expandTip" class="expand-tip">{{ row.expandTip }}</p>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="skuName" label="SKU规格" width="140" show-overflow-tooltip />
        <el-table-column prop="category" label="所属分类" width="130" show-overflow-tooltip />
        <el-table-column prop="supplier" label="供应商" width="90" align="center" />

        <el-table-column label="当前实际库存" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="isLowStock(row)" type="danger" effect="dark">{{ row.actualStock }}</el-tag>
            <el-tag v-else-if="row.actualStock === 0" type="warning">{{ row.actualStock }}</el-tag>
            <span v-else>{{ row.actualStock }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="warningStock" label="库存预警值" width="110" align="center" />
        <el-table-column prop="frozenStock" label="冻结库存" width="100" align="center" />
        <el-table-column prop="availableStock" label="可用库存" width="100" align="center" />

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openStockDialog(row)">调整库存</el-button>
            <el-button type="success" link @click="goFlow(row)">查看流水</el-button>
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
        />
      </div>
    </el-card>

    <!-- 调整库存弹窗 -->
    <el-dialog
      v-model="stockDialog.visible"
      title="编辑库存"
      width="720px"
      destroy-on-close
      @closed="resetStockDialog"
    >
      <div v-if="stockDialog.row" class="dialog-product">
        <el-image :src="stockDialog.row.thumb" fit="cover" class="dialog-thumb" />
        <div class="dialog-info">
          <p class="dialog-title">{{ stockDialog.row.name }}</p>
          <p class="dialog-meta">商品ID: {{ stockDialog.row.goodsId }} | 品牌: {{ stockDialog.row.brand }}</p>
        </div>
        <el-link type="primary">添加新规格 &gt;</el-link>
      </div>

      <el-table :data="stockDialog.skus" border class="sku-table">
        <el-table-column prop="skuName" label="规格名称" min-width="120" />
        <el-table-column prop="skuId" label="规格编号" width="100" />
        <el-table-column prop="skuCode" label="货号" width="110" />
        <el-table-column label="仓库库存" width="140" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.actualStock" :min="0" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="仓库预警值" width="140" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.warningStock" :min="0" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="warehouseCode" label="仓库编号" width="100" />
      </el-table>

      <template #footer>
        <el-button @click="stockDialog.visible = false">关闭</el-button>
        <el-button type="primary" :loading="stockDialog.saving" @click="handleUpdateStock">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  categoryCascaderOptions,
  supplierOptions,
  mockInventoryList,
  inventoryTabCounts,
} from '@/mock/inventory'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const selectedRows = ref([])

const tabCounts = { ...inventoryTabCounts }

const searchForm = reactive({
  keyword: '',
  category: [],
  supplier: '',
  stockStatus: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 265,
  totalPages: 10,
})

const stockDialog = reactive({
  visible: false,
  saving: false,
  row: null,
  skus: [],
})

const isLowStock = (row) => row.actualStock > 0 && row.actualStock <= row.warningStock

const rowClassName = ({ row }) => (isLowStock(row) ? 'row-warning' : '')

/**
 * 获取库存列表
 * 此处后续使用 axios 请求 Spring Boot 后端的 /api/inventory/list 接口
 */
const fetchInventoryList = async () => {
  loading.value = true
  try {
    await new Promise((resolve) => setTimeout(resolve, 500))
    let list = [...mockInventoryList]
    if (activeTab.value === 'warning') {
      list = list.filter((item) => item.status === 'warning')
    } else if (activeTab.value === 'out') {
      list = list.filter((item) => item.status === 'out')
    }
    if (searchForm.stockStatus) {
      list = list.filter((item) => item.status === searchForm.stockStatus)
    }
    tableData.value = list
    pagination.total = activeTab.value === 'all' ? 265 : list.length
    pagination.totalPages = Math.ceil(pagination.total / pagination.pageSize)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchInventoryList()
  ElMessage.success('查询成功')
}

const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    category: [],
    supplier: '',
    stockStatus: '',
  })
  activeTab.value = 'all'
  fetchInventoryList()
}

const handleBatchAdjust = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择要调整的商品')
    return
  }
  ElMessage.info(`已选择 ${selectedRows.value.length} 个商品，批量调整功能开发中`)
}

const openStockDialog = (row) => {
  stockDialog.row = row
  stockDialog.skus = row.skus.map((s) => ({ ...s }))
  stockDialog.visible = true
}

const resetStockDialog = () => {
  stockDialog.row = null
  stockDialog.skus = []
}

/**
 * 提交库存调整
 * POST /api/inventory/update
 */
const handleUpdateStock = async () => {
  stockDialog.saving = true
  try {
    // const { data } = await axios.post('/api/inventory/update', {
    //   goodsId: stockDialog.row.goodsId,
    //   skus: stockDialog.skus,
    // })
    await new Promise((resolve) => setTimeout(resolve, 400))
    const mainSku = stockDialog.skus[0]
    if (stockDialog.row && mainSku) {
      stockDialog.row.actualStock = mainSku.actualStock
      stockDialog.row.warningStock = mainSku.warningStock
      stockDialog.row.availableStock = mainSku.actualStock - stockDialog.row.frozenStock
      stockDialog.row.status =
        mainSku.actualStock === 0
          ? 'out'
          : mainSku.actualStock <= mainSku.warningStock
            ? 'warning'
            : 'sufficient'
    }
    ElMessage.success('库存更新成功')
    stockDialog.visible = false
    fetchInventoryList()
  } finally {
    stockDialog.saving = false
  }
}

const goFlow = (row) => {
  router.push({ path: '/inventory/flow', query: { goodsId: row.goodsId } })
}

onMounted(fetchInventoryList)
</script>

<style scoped>
.inventory-page {
  min-height: calc(100vh - 120px);
}

.panel-card {
  margin-bottom: 12px;
  border-radius: 8px;
  border: none;
}

.panel-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
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

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.product-thumb {
  width: 48px;
  height: 48px;
  border-radius: 4px;
}

.product-name {
  margin: 0;
  font-size: 13px;
  color: #303133;
  line-height: 1.4;
}

.expand-tip {
  margin: 4px 0 0;
  font-size: 12px;
  color: #f56c6c;
}

:deep(.row-warning) {
  background-color: #fef0f0 !important;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.page-info {
  font-size: 13px;
  color: #606266;
}

.dialog-product {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.dialog-thumb {
  width: 64px;
  height: 64px;
  border-radius: 4px;
  flex-shrink: 0;
}

.dialog-info {
  flex: 1;
}

.dialog-title {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.dialog-meta {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.sku-table {
  margin-top: 8px;
}
</style>
