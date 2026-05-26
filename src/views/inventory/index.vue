<template>
  <div v-loading="loading" class="inventory-page">
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

    <el-card shadow="never" class="panel-card">
      <div class="toolbar">
        <el-tabs v-model="activeTab" @tab-change="onTabChange">
          <el-tab-pane
            v-for="tab in statusTabs"
            :key="tab.key"
            :name="tab.key"
            :label="`${tab.label}(${tab.count})`"
          />
        </el-tabs>
        <div class="toolbar-actions">
          <el-button type="primary" @click="handleBatchAdjust">批量调整库存</el-button>
          <el-button type="primary" plain>导出库存报表</el-button>
        </div>
      </div>
    </el-card>

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
          @current-change="fetchInventoryList"
          @size-change="onPageSizeChange"
        />
      </div>
    </el-card>

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
        <el-table-column prop="skuId" label="规格编号" width="120" />
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

    <el-dialog v-model="batchDialog.visible" title="批量调整库存" width="640px" destroy-on-close>
      <p class="batch-hint">将批量更新各商品主规格库存，扩展 SKU 保持不变。</p>
      <el-table :data="batchDialog.rows" border max-height="360">
        <el-table-column prop="name" label="商品" min-width="160" show-overflow-tooltip />
        <el-table-column label="当前库存" width="140" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.stock" :min="0" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="预警值" width="140" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.warningStock" :min="0" size="small" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="batchDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="batchDialog.saving" @click="handleBatchSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchCategoryTree } from '@/api/product'
import { fetchInventoryList as loadInventoryList, updateInventory } from '@/api/inventory'
import { supplierOptions } from '@/constants/inventory'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const selectedRows = ref([])
const categoryCascaderOptions = ref([])
const statusTabs = ref([
  { key: 'all', label: '全部库存', count: 0 },
  { key: 'warning', label: '预警商品', count: 0 },
  { key: 'out', label: '已售罄商品', count: 0 },
])

const searchForm = reactive({
  keyword: '',
  category: [],
  supplier: '',
  stockStatus: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  totalPages: 1,
})

const stockDialog = reactive({
  visible: false,
  saving: false,
  row: null,
  skus: [],
})

const batchDialog = reactive({
  visible: false,
  saving: false,
  rows: [],
})

const mapCategoryTree = (nodes) =>
  (nodes || []).map((n) => ({
    value: n.id,
    label: n.label,
    children: n.children?.length ? mapCategoryTree(n.children) : undefined,
  }))

const buildQuery = () => ({
  keyword: searchForm.keyword || undefined,
  category: searchForm.category?.length
    ? String(searchForm.category[searchForm.category.length - 1])
    : undefined,
  supplier: searchForm.supplier || undefined,
  stockStatus: searchForm.stockStatus || undefined,
  tab: activeTab.value,
  page: pagination.page,
  pageSize: pagination.pageSize,
})

const isLowStock = (row) => row.actualStock > 0 && row.actualStock <= row.warningStock

const rowClassName = ({ row }) => (isLowStock(row) ? 'row-warning' : '')

const fetchInventoryList = async () => {
  loading.value = true
  try {
    const data = await loadInventoryList(buildQuery())
    tableData.value = data.list
    pagination.total = data.total
    pagination.totalPages = data.totalPages
    if (data.tabs?.length) {
      statusTabs.value = data.tabs
    }
  } finally {
    loading.value = false
  }
}

const onTabChange = () => {
  pagination.page = 1
  fetchInventoryList()
}

const onPageSizeChange = () => {
  pagination.page = 1
  fetchInventoryList()
}

const handleSearch = () => {
  pagination.page = 1
  fetchInventoryList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    category: [],
    supplier: '',
    stockStatus: '',
  })
  activeTab.value = 'all'
  pagination.page = 1
  fetchInventoryList()
}

const handleBatchAdjust = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择要调整的商品')
    return
  }
  if (selectedRows.value.length === 1) {
    openStockDialog(selectedRows.value[0])
    return
  }
  batchDialog.rows = selectedRows.value.map((row) => {
    const primary = row.skus?.[0]
    return {
      goodsId: row.goodsId,
      name: row.name,
      stock: primary?.actualStock ?? row.actualStock ?? 0,
      warningStock: primary?.warningStock ?? row.warningStock ?? 0,
      skus: (row.skus || []).map((s) => ({ ...s })),
    }
  })
  batchDialog.visible = true
}

const handleBatchSave = async () => {
  batchDialog.saving = true
  try {
    await Promise.all(
      batchDialog.rows.map((row) => {
        const skus = row.skus.map((s, idx) =>
          idx === 0 ? { ...s, actualStock: row.stock, warningStock: row.warningStock } : { ...s },
        )
        return updateInventory({ goodsId: row.goodsId, skus })
      }),
    )
    ElMessage.success('批量库存更新成功')
    batchDialog.visible = false
    fetchInventoryList()
  } finally {
    batchDialog.saving = false
  }
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

const handleUpdateStock = async () => {
  if (!stockDialog.row) return
  stockDialog.saving = true
  try {
    await updateInventory({
      goodsId: stockDialog.row.goodsId,
      skus: stockDialog.skus,
    })
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

onMounted(async () => {
  try {
    const tree = await fetchCategoryTree()
    categoryCascaderOptions.value = mapCategoryTree(tree)
  } catch {
    categoryCascaderOptions.value = []
  }
  fetchInventoryList()
})
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

.batch-hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: #909399;
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
