<template>
  <div v-loading="loading" class="recycle-page">
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品">
          <el-input
            v-model="searchForm.keyword"
            placeholder="名称 / 编号 / SKU"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="panel-card toolbar-card">
      <div class="toolbar">
        <span class="toolbar-tip">共 {{ pagination.total }} 件已删除商品</span>
        <div class="toolbar-actions">
          <el-button type="primary" :icon="RefreshRight" :disabled="!selectedRows.length" @click="handleBatchRestore">
            批量恢复
          </el-button>
          <el-button type="danger" :icon="Delete" :disabled="!selectedRows.length" @click="handleBatchPurge">
            批量彻底删除
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table :data="tableData" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="商品编号" width="100" align="center" />
        <el-table-column label="商品信息" min-width="240">
          <template #default="{ row }">
            <div class="product-info">
              <el-image :src="row.thumb" fit="cover" class="product-thumb" />
              <div class="product-text">
                <p class="product-title">{{ row.title }}</p>
                <p class="product-sub">{{ row.subtitle }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120" align="center">
          <template #default="{ row }">
            <div class="price-cell">
              <span>售价: {{ row.originalPrice }}元</span>
              <span class="discount">优惠价: {{ row.discountPrice }}元</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="deletedAt" label="删除时间" width="160" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleRestore(row)">恢复</el-button>
            <el-button type="danger" link @click="handlePurge(row)">彻底删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, prev, pager, next, sizes"
          background
          @current-change="fetchData"
          @size-change="onPageSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, RefreshRight } from '@element-plus/icons-vue'
import {
  batchPurgeRecycleProducts,
  fetchRecycleList,
  purgeRecycleProduct,
  restoreRecycleProducts,
} from '@/api/product'

const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const data = await fetchRecycleList({
      keyword: searchForm.keyword || undefined,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = data.list || []
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.keyword = ''
  pagination.page = 1
  fetchData()
}

const onPageSizeChange = () => {
  pagination.page = 1
  fetchData()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const requireSelection = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择商品')
    return null
  }
  return selectedRows.value.map((r) => r.id)
}

const handleRestore = (row) => {
  ElMessageBox.confirm(`确定恢复商品「${row.title}」吗？`, '恢复商品', { type: 'info' })
    .then(async () => {
      await restoreRecycleProducts([row.id])
      ElMessage.success('已恢复')
      fetchData()
    })
    .catch(() => {})
}

const handlePurge = (row) => {
  ElMessageBox.confirm(`彻底删除后无法恢复，确定删除「${row.title}」吗？`, '彻底删除', { type: 'warning' })
    .then(async () => {
      await purgeRecycleProduct(row.id)
      ElMessage.success('已彻底删除')
      fetchData()
    })
    .catch(() => {})
}

const handleBatchRestore = () => {
  const ids = requireSelection()
  if (!ids) return
  ElMessageBox.confirm(`确定恢复选中的 ${ids.length} 个商品吗？`, '批量恢复', { type: 'info' })
    .then(async () => {
      await restoreRecycleProducts(ids)
      ElMessage.success('批量恢复成功')
      fetchData()
    })
    .catch(() => {})
}

const handleBatchPurge = () => {
  const ids = requireSelection()
  if (!ids) return
  ElMessageBox.confirm(`彻底删除后无法恢复，确定删除选中的 ${ids.length} 个商品吗？`, '批量彻底删除', { type: 'warning' })
    .then(async () => {
      await batchPurgeRecycleProducts(ids)
      ElMessage.success('批量删除成功')
      fetchData()
    })
    .catch(() => {})
}

onMounted(fetchData)
</script>

<style scoped>
.recycle-page {
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.panel-card {
  border: none;
  border-radius: 8px;
}
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}
.toolbar-tip {
  font-size: 13px;
  color: #909399;
}
.toolbar-actions {
  display: flex;
  gap: 8px;
}
.product-info {
  display: flex;
  gap: 10px;
  align-items: center;
}
.product-thumb {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  flex-shrink: 0;
}
.product-title {
  margin: 0 0 4px;
  font-size: 13px;
  color: #303133;
}
.product-sub {
  margin: 0;
  font-size: 12px;
  color: #909399;
}
.price-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
}
.price-cell .discount {
  color: #f56c6c;
}
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
