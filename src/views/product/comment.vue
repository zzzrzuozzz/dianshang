<template>
  <div v-loading="loading" class="comment-page">
    <!-- 顶部指标卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col v-for="card in dashboardData.stats" :key="card.key" :xs="24" :sm="12" :md="8" :lg="4">
        <el-card shadow="hover" class="stat-card">
          <p class="stat-label">{{ card.label }}</p>
          <p class="stat-value">{{ card.value }}</p>
          <div class="stat-footer">
            <template v-if="card.action">
              <el-link type="primary" @click="handleAction(card.key)">{{ card.action }}</el-link>
              <span class="stat-extra">今日新增 {{ card.todayNew }}</span>
            </template>
            <template v-else>
              <span :class="card.trend >= 0 ? 'trend-up' : 'trend-down'">
                前7天 {{ card.trend >= 0 ? '↑' : '' }}{{ card.trend }}%
              </span>
            </template>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选栏 -->
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入商品名称或编号"
            clearable
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Tabs + 批量操作 -->
    <el-card shadow="never" class="panel-card">
      <div class="toolbar">
        <el-tabs v-model="activeTab" @tab-change="fetchTableData">
          <el-tab-pane
            v-for="tab in ratingTabs"
            :key="tab.key"
            :name="tab.key"
            :label="`${tab.label} (${tab.count})`"
          />
        </el-tabs>
        <el-button type="danger" :icon="Delete">批量删除</el-button>
      </div>
    </el-card>

    <!-- 分类树 + 表格 -->
    <el-card shadow="never" class="panel-card body-card">
      <div class="body-layout">
        <aside class="category-tree">
          <el-tree
            :data="categoryTreeData"
            :props="{ label: 'label', children: 'children' }"
            default-expand-all
            highlight-current
          />
        </aside>

        <div class="table-wrap">
          <el-table :data="tableData" border stripe @selection-change="(r) => (selected = r)">
            <el-table-column type="selection" width="48" align="center" />
            <el-table-column prop="id" label="商品编号" width="100" align="center" />

            <el-table-column label="商品信息" min-width="220">
              <template #default="{ row }">
                <div class="product-info">
                  <el-image :src="row.thumb" fit="cover" class="product-thumb" />
                  <p class="product-title">{{ row.title }}</p>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="评价" width="100" align="center">
              <template #default="{ row }">
                <div class="rating-cell">
                  <span :class="['star', `star-${row.rating}`]">⭐</span>
                  <span>{{ ratingLabel[row.rating] }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="content" label="评价内容" min-width="140" show-overflow-tooltip />

            <el-table-column label="价格" width="110" align="center">
              <template #default="{ row }">
                <div class="price-cell">
                  <span>原价: {{ row.originalPrice }}元</span>
                  <span class="discount">优惠价: {{ row.discountPrice }}元</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="上架状态" width="90" align="center">
              <template #default="{ row }">
                <span :class="row.status === 'on' ? 'status-on' : 'status-off'">
                  {{ row.status === 'on' ? '已上架' : '已下架' }}
                </span>
              </template>
            </el-table-column>

            <el-table-column prop="sku" label="货号" width="90" align="center" />
            <el-table-column prop="sort" label="排序" width="70" align="center" />
            <el-table-column prop="stock" label="库存" width="70" align="center" />
            <el-table-column prop="sales" label="销量" width="70" align="center" />

            <el-table-column label="总评价" width="120" align="center">
              <template #default="{ row }">
                <div class="total-rating">
                  <span>好评 {{ row.totalGood }}</span>
                  <span>中评 {{ row.totalNeutral }}</span>
                  <span>差评 {{ row.totalBad }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="180" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleView(row)">查看评价</el-button>
                <el-button type="success" size="small" plain @click="handleFeature(row)">加精</el-button>
                <el-button type="danger" size="small" plain @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-bar">
            <span class="total-text">共 {{ pagination.total }} 条记录</span>
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="pagination.total"
              layout="prev, pager, next, sizes"
              background
              @current-change="fetchTableData"
              @size-change="onPageSizeChange"
            />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import {
  deleteComment,
  featureComment,
  fetchCategoryTree,
  fetchCommentList,
  fetchCommentOverview,
} from '@/api/product'

const loading = ref(false)
const activeTab = ref('all')
const selected = ref([])
const tableData = ref([])
const categoryTreeData = ref([])

const ratingLabel = { good: '好评', neutral: '中评', bad: '差评' }

const ratingTabs = ref([])

const searchForm = reactive({ keyword: '' })

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const dashboardData = reactive({
  stats: [],
})

const fetchDashboardData = async () => {
  const data = await fetchCommentOverview()
  dashboardData.stats = data.stats || []
}

const fetchTableData = async () => {
  loading.value = true
  try {
    const data = await fetchCommentList({
      keyword: searchForm.keyword || undefined,
      rating: activeTab.value,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = data.list
    pagination.total = data.total
    ratingTabs.value = data.tabs || []
  } finally {
    loading.value = false
  }
}

const onPageSizeChange = () => {
  pagination.page = 1
  fetchTableData()
}

const loadTree = async () => {
  categoryTreeData.value = await fetchCategoryTree()
}

const handleSearch = () => {
  pagination.page = 1
  fetchTableData()
  ElMessage.success('查询成功')
}

const handleReset = () => {
  searchForm.keyword = ''
  fetchTableData()
}

const handleAction = (key) => {
  ElMessage.info(`跳转到 ${key} 处理`)
}

const handleView = (row) => ElMessage.info(`查看商品 ${row.id} 的评价`)
const handleFeature = async (row) => {
  await featureComment(row.id)
  ElMessage.success(`商品 ${row.id} 已加精`)
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该评价吗？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteComment(row.id)
      ElMessage.success('删除成功')
      fetchTableData()
      fetchDashboardData()
    })
    .catch(() => {})
}

onMounted(async () => {
  await loadTree()
  await fetchDashboardData()
  await fetchTableData()
})
</script>

<style scoped>
.comment-page {
  min-height: calc(100vh - 120px);
}

.stat-row {
  margin-bottom: 12px;
}

.stat-card {
  margin-bottom: 12px;
  border-radius: 8px;
  border: none;
}

.stat-card :deep(.el-card__body) {
  padding: 16px;
}

.stat-label {
  margin: 0 0 8px;
  font-size: 13px;
  color: #909399;
}

.stat-value {
  margin: 0 0 10px;
  font-size: 26px;
  font-weight: 600;
  color: #303133;
}

.stat-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
}

.stat-extra {
  color: #909399;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

.panel-card {
  margin-bottom: 12px;
  border-radius: 8px;
  border: none;
}

.search-actions {
  margin-left: auto;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toolbar :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.body-layout {
  display: flex;
  gap: 16px;
}

.category-tree {
  flex-shrink: 0;
  width: 180px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.table-wrap {
  flex: 1;
  min-width: 0;
  overflow: auto;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-thumb {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  flex-shrink: 0;
}

.product-title {
  margin: 0;
  font-size: 13px;
  color: #303133;
  line-height: 1.4;
}

.rating-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 12px;
}

.star-good {
  filter: none;
  color: #e6a23c;
}

.star-neutral {
  color: #67c23a;
}

.star-bad {
  color: #f56c6c;
}

.price-cell,
.total-rating {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: #606266;
}

.discount {
  color: #f56c6c;
}

.status-on {
  color: #303133;
}

.status-off {
  color: #f56c6c;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}
</style>
