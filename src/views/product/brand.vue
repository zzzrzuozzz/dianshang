<template>
  <div v-loading="loading" class="brand-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入品牌名称或编号"
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

    <!-- 状态 Tabs + 操作 -->
    <el-card shadow="never" class="panel-card">
      <div class="toolbar">
        <el-tabs v-model="activeTab" @tab-change="fetchData">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane label="可用" name="active" />
          <el-tab-pane label="已禁用" name="disabled" />
        </el-tabs>
        <div class="toolbar-actions">
          <el-button type="primary" :icon="Plus">添加品牌</el-button>
          <el-button type="danger" :icon="Delete">批量删除</el-button>
        </div>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="panel-card">
      <el-table :data="tableData" border stripe @selection-change="(r) => (selected = r)">
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="品牌编号" width="110" align="center" />
        <el-table-column prop="name" label="品牌名称" min-width="120" />
        <el-table-column prop="initial" label="首字母" width="80" align="center" />
        <el-table-column prop="count" label="商品数量" width="100" align="center" />
        <el-table-column prop="supplier" label="供应商" width="100" align="center" />
        <el-table-column label="是否显示" width="90" align="center">
          <template #default="{ row }">
            <span :class="row.visible ? 'visible-yes' : 'visible-no'">
              {{ row.visible ? '显示' : '隐藏' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />

        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" class="btn-blue" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" class="btn-red" @click="handleDelete(row)">删除</el-button>
            <el-button
              size="small"
              :class="row.visible ? 'btn-red' : 'btn-green-solid'"
              @click="toggleVisible(row)"
            >
              {{ row.visible ? '隐藏' : '显示' }}
            </el-button>
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
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const activeTab = ref('all')
const selected = ref([])
const tableData = ref([])

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 200 })

/**
 * 获取品牌列表
 * 此处后续使用 axios 请求 Spring Boot 后端的 /api/product/brand/list 接口
 */
const fetchData = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 300))
    tableData.value = [
      { id: 'B001', name: '华为', initial: 'H', count: 120, supplier: '自营', visible: true, sort: 1 },
      { id: 'B002', name: '小米', initial: 'X', count: 98, supplier: '第三方', visible: true, sort: 2 },
      { id: 'B003', name: '百事可乐', initial: 'B', count: 45, supplier: '自营', visible: false, sort: 3 },
      { id: 'B004', name: '耐克', initial: 'N', count: 67, supplier: '第三方', visible: true, sort: 4 },
    ]
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
  fetchData()
}

const toggleVisible = (row) => {
  row.visible = !row.visible
  ElMessage.success(row.visible ? '已显示' : '已隐藏')
}

const handleEdit = (row) => ElMessage.info(`编辑品牌 ${row.name}`)
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除品牌 ${row.name} 吗？`, '提示', { type: 'warning' })
    .then(() => ElMessage.success('删除成功'))
    .catch(() => {})
}

onMounted(fetchData)
</script>

<style scoped>
.brand-page {
  min-height: calc(100vh - 120px);
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

.visible-yes {
  color: #303133;
}

.visible-no {
  color: #f56c6c;
  font-weight: 500;
}

.btn-blue {
  color: #409eff;
  border-color: #b3d8ff;
  background: #ecf5ff;
}

.btn-red {
  color: #f56c6c;
  border-color: #fbc4c4;
  background: #fef0f0;
}

.btn-green-solid {
  color: #fff;
  border-color: #67c23a;
  background: #67c23a;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

.total-text {
  font-size: 13px;
  color: #606266;
}
</style>
