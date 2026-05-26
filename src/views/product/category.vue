<template>
  <div v-loading="loading" class="category-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入名称或编号"
            clearable
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 级别 Tabs + 操作按钮 -->
    <el-card shadow="never" class="panel-card">
      <div class="toolbar">
        <el-tabs v-model="activeLevel" @tab-change="fetchData">
          <el-tab-pane label="一级类目" name="1" />
          <el-tab-pane label="二级类目" name="2" />
          <el-tab-pane label="三级类目" name="3" />
          <el-tab-pane label="四级类目" name="4" />
        </el-tabs>
        <div class="toolbar-actions">
          <el-button type="primary" :icon="Plus">添加分类</el-button>
          <el-button type="danger" :icon="Delete">批量删除</el-button>
        </div>
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
            <el-table-column prop="id" label="类目编号" width="110" align="center" />
            <el-table-column prop="name" label="分类名称" min-width="120" />
            <el-table-column prop="level" label="级别" width="80" align="center" />
            <el-table-column prop="count" label="商品数量" width="100" align="center" />
            <el-table-column prop="unit" label="数量单位" width="90" align="center" />
            <el-table-column label="是否显示" width="90" align="center">
              <template #default="{ row }">
                <span :class="row.visible ? 'visible-yes' : 'visible-no'">
                  {{ row.visible ? '显示' : '隐藏' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="sort" label="排序" width="70" align="center" />

            <el-table-column label="设置" width="320" align="center">
              <template #default="{ row }">
                <el-button size="small" class="btn-green" @click="handleAddChild(row)">新增下级</el-button>
                <el-button size="small" class="btn-green" @click="handleViewChild(row)">查看下级</el-button>
                <el-button size="small" class="btn-green" @click="handleTransfer(row)">转移商品</el-button>
                <el-button
                  size="small"
                  :class="row.visible ? 'btn-red' : 'btn-green-solid'"
                  @click="toggleVisible(row)"
                >
                  {{ row.visible ? '隐藏' : '显示' }}
                </el-button>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="120" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-bar">
            <span class="total-text">共 {{ pagination.total }} 条</span>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { categoryTreeData } from '@/mock/product'

const loading = ref(false)
const activeLevel = ref('1')
const selected = ref([])
const tableData = ref([])

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 300 })

/**
 * 获取分类列表
 * 此处后续使用 axios 请求 Spring Boot 后端的 /api/product/category/list 接口
 */
const fetchData = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 300))
    tableData.value = [
      { id: '625543', name: '日用百货', level: '一级', count: 100, unit: '件', visible: true, sort: 1 },
      { id: '625544', name: '母婴宠物', level: '一级', count: 80, unit: '件', visible: true, sort: 2 },
      { id: '625545', name: '大牌', level: '一级', count: 60, unit: '件', visible: false, sort: 3 },
      { id: '625546', name: '女装', level: '一级', count: 45, unit: '件', visible: true, sort: 4 },
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

const handleAddChild = (row) => ElMessage.info(`为 ${row.name} 新增下级分类`)
const handleViewChild = (row) => ElMessage.info(`查看 ${row.name} 的下级分类`)
const handleTransfer = (row) => ElMessage.info(`转移 ${row.name} 下的商品`)
const handleEdit = (row) => ElMessage.info(`编辑分类 ${row.name}`)
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除分类 ${row.name} 吗？`, '提示', { type: 'warning' })
    .then(() => ElMessage.success('删除成功'))
    .catch(() => {})
}

onMounted(fetchData)
</script>

<style scoped>
.category-page {
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

.visible-yes {
  color: #303133;
}

.visible-no {
  color: #f56c6c;
  font-weight: 500;
}

.btn-green {
  color: #67c23a;
  border-color: #c2e7b0;
  background: #f0f9eb;
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
