<template>
  <div v-loading="loading" class="product-panel">
    <!-- 顶部检索 -->
    <el-card shadow="never" class="panel-card search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入名称或编号"
            clearable
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="searchForm.category"
            placeholder="请选择商品分类"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="opt in categoryOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 中间操作栏 -->
    <el-card shadow="never" class="panel-card toolbar-card">
      <div class="toolbar">
        <el-tabs v-model="activeTab" class="status-tabs" @tab-change="handleTabChange">
          <el-tab-pane
            v-for="tab in statusTabs"
            :key="tab.key"
            :name="tab.key"
            :label="`${tab.label}(${tab.count})`"
          />
        </el-tabs>
        <div class="toolbar-actions">
          <template v-if="pageType === 'list'">
            <el-button type="primary" :icon="Plus" @click="router.push('/product/add')">
              添加商品
            </el-button>
            <el-button type="success" :icon="Top">批量上架</el-button>
            <el-button type="warning" :icon="Bottom">批量下架</el-button>
            <el-button type="danger" :icon="Delete">批量删除</el-button>
          </template>
          <template v-else>
            <el-button type="primary" :icon="Plus" @click="router.push('/product/add')">
              添加商品
            </el-button>
            <el-button type="success" :icon="Select">批量审核</el-button>
          </template>
        </div>
      </div>
    </el-card>

    <!-- 主体：分类树 + 表格 -->
    <el-card shadow="never" class="panel-card body-card">
      <div class="body-layout">
        <aside class="category-tree">
          <el-tree
            :data="categoryTreeData"
            :props="{ label: 'label', children: 'children' }"
            default-expand-all
            highlight-current
            show-checkbox
            node-key="id"
          />
        </aside>

        <div class="table-wrap">
          <el-table
            :data="tableData"
            border
            stripe
            @selection-change="handleSelectionChange"
          >
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

            <template v-if="pageType === 'list'">
              <el-table-column label="状态" width="120" align="center">
                <template #default="{ row }">
                  <div class="status-cell">
                    <span :class="row.status === 'on' ? 'status-on' : 'status-off'">
                      {{ row.status === 'on' ? '已上架' : '已下架' }}
                    </span>
                    <el-button
                      size="small"
                      :type="row.status === 'on' ? 'danger' : 'success'"
                      plain
                      @click="toggleStatus(row)"
                    >
                      {{ row.status === 'on' ? '下架' : '上架' }}
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </template>

            <template v-if="pageType === 'audit'">
              <el-table-column label="审核状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="auditStatusMap[row.auditStatus]?.type" size="small">
                    {{ auditStatusMap[row.auditStatus]?.label }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" width="120" show-overflow-tooltip />
            </template>

            <el-table-column prop="sku" label="货号" width="100" align="center" />
            <el-table-column prop="sort" label="排序" width="70" align="center" />
            <el-table-column prop="stock" label="库存" width="80" align="center" />

            <el-table-column label="销量" width="110" align="center">
              <template #default="{ row }">
                <div class="sales-cell">
                  <span>月销: {{ row.monthSales }}</span>
                  <span>总销量: {{ row.totalSales }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="supplier" label="供货商" width="90" align="center" />

            <el-table-column label="操作" width="130" fixed="right" align="center">
              <template #default="{ row }">
                <template v-if="pageType === 'list'">
                  <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
                  <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
                </template>
                <template v-else>
                  <el-button type="primary" link @click="handleView(row)">查看</el-button>
                  <el-button type="success" link @click="handleAudit(row)">审核</el-button>
                </template>
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
            />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Top, Bottom, Delete, Select } from '@element-plus/icons-vue'
import {
  categoryTreeData,
  categoryOptions,
  mockProductList,
  auditStatusMap,
  listStatusTabs,
  auditStatusTabs,
} from '@/mock/product'

const props = defineProps({
  /** list | audit */
  pageType: {
    type: String,
    default: 'list',
    validator: (v) => ['list', 'audit'].includes(v),
  },
})

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const selectedRows = ref([])

const statusTabs = props.pageType === 'list' ? listStatusTabs : auditStatusTabs

const searchForm = reactive({
  keyword: '',
  category: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 200,
})

/**
 * 获取商品列表/审核数据
 * 此处后续使用 axios 请求 Spring Boot 后端接口
 * 列表: GET /api/product/list
 * 审核: GET /api/product/audit/list
 */
const fetchData = async () => {
  loading.value = true
  try {
    await new Promise((resolve) => setTimeout(resolve, 300))
    tableData.value = [...mockProductList]
    pagination.total = props.pageType === 'list' ? 1800 : 200
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
  ElMessage.success('查询成功')
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.category = ''
  pagination.page = 1
  fetchData()
}

const handleTabChange = () => {
  pagination.page = 1
  fetchData()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const toggleStatus = (row) => {
  row.status = row.status === 'on' ? 'off' : 'on'
  ElMessage.success(row.status === 'on' ? '已上架' : '已下架')
}

const handleEdit = (row) => {
  router.push({ path: '/product/add', query: { id: row.id } })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除商品 ${row.id} 吗？`, '提示', { type: 'warning' })
    .then(() => ElMessage.success('删除成功'))
    .catch(() => {})
}

const handleView = (row) => {
  ElMessage.info(`查看商品 ${row.id}`)
}

const handleAudit = (row) => {
  ElMessageBox.confirm(`是否通过商品 ${row.id} 的审核？`, '商品审核', {
    confirmButtonText: '通过',
    cancelButtonText: '驳回',
    distinguishCancelAndClose: true,
    type: 'info',
  })
    .then(() => {
      row.auditStatus = 'passed'
      ElMessage.success('审核通过')
    })
    .catch((action) => {
      if (action === 'cancel') {
        row.auditStatus = 'rejected'
        row.remark = '审核未通过'
        ElMessage.warning('已驳回')
      }
    })
}

onMounted(fetchData)
</script>

<style scoped>
.product-panel {
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
  margin-right: 0;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.status-tabs {
  flex: 1;
  min-width: 0;
}

.status-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.body-layout {
  display: flex;
  gap: 16px;
  min-height: 480px;
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
  align-items: flex-start;
  gap: 10px;
}

.product-thumb {
  width: 56px;
  height: 56px;
  border-radius: 4px;
  flex-shrink: 0;
}

.product-title {
  margin: 0 0 4px;
  font-size: 13px;
  color: #303133;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-sub {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.price-cell,
.sales-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: #606266;
}

.discount {
  color: #f56c6c;
}

.status-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
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
  flex-wrap: wrap;
  gap: 12px;
}

.total-text {
  font-size: 13px;
  color: #606266;
}
</style>
