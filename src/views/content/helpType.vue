<template>
  <div v-loading="loading" class="content-page">
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="名称">
          <el-input v-model="searchForm.keyword" placeholder="请输入名称或编号" clearable style="width: 220px" />
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
          <el-tab-pane :label="`全部 (${counts.all})`" name="all" />
          <el-tab-pane :label="`可用 (${counts.active})`" name="active" />
          <el-tab-pane :label="`已禁用 (${counts.disabled})`" name="disabled" />
        </el-tabs>
        <div class="toolbar-actions">
          <el-button type="primary" @click="router.push('/content/help/type/add')">+ 添加分类</el-button>
          <el-button type="danger" :disabled="!selectedRows.length" @click="handleBatchDelete">批量删除</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table ref="tableRef" :data="tableData" row-key="id" border stripe @selection-change="onSelectionChange">
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="code" label="编号" width="100" align="center" />
        <el-table-column label="类型图标" width="90" align="center">
          <template #default="{ row }">
            <el-image v-if="row.icon" :src="row.icon" style="width: 48px; height: 48px" fit="cover" />
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" min-width="120" />
        <el-table-column prop="articleCount" label="文章数量" width="100" align="center" />
        <el-table-column label="是否显示" width="90" align="center">
          <template #default="{ row }">
            <span :class="row.visible ? 'text-show' : 'text-hide'">{{ row.visible ? '显示' : '隐藏' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="router.push(`/content/help/type/edit/${row.id}`)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            <el-button size="small" :class="row.visible ? 'btn-hide' : 'btn-show'" @click="toggleVisible(row)">
              {{ row.visible ? '隐藏' : '显示' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <span>第{{ pagination.page }}页 共{{ pagination.totalPages }}页 {{ pagination.total }}条</span>
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          layout="prev, pager, next, sizes"
          background
          @current-change="fetchHelpTypes"
          @size-change="onPageSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  batchDeleteHelpTypes,
  deleteHelpType,
  fetchHelpTypeList,
  toggleHelpTypeVisible,
} from '@/api/content'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const tableRef = ref()
const selectedRows = ref([])

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0, totalPages: 1 })
const counts = reactive({ all: 0, active: 0, disabled: 0 })

const onSelectionChange = (rows) => {
  selectedRows.value = rows
}

const fetchHelpTypes = async () => {
  loading.value = true
  try {
    const data = await fetchHelpTypeList({
      keyword: searchForm.keyword || undefined,
      tab: activeTab.value,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = data.list
    pagination.total = data.total
    pagination.totalPages = data.totalPages
    if (data.counts) {
      counts.all = data.counts.all ?? 0
      counts.active = data.counts.active ?? 0
      counts.disabled = data.counts.disabled ?? 0
    }
    selectedRows.value = []
    tableRef.value?.clearSelection()
  } finally {
    loading.value = false
  }
}

const onTabChange = () => {
  pagination.page = 1
  fetchHelpTypes()
}

const onPageSizeChange = () => {
  pagination.page = 1
  fetchHelpTypes()
}

const handleSearch = () => {
  pagination.page = 1
  fetchHelpTypes()
  ElMessage.success('查询成功')
}

const handleReset = () => {
  searchForm.keyword = ''
  activeTab.value = 'all'
  pagination.page = 1
  fetchHelpTypes()
}

const toggleVisible = async (row) => {
  const next = !row.visible
  await toggleHelpTypeVisible(row.id, next)
  row.visible = next
  ElMessage.success('已更新')
  fetchHelpTypes()
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteHelpType(row.id)
      ElMessage.success('删除成功')
      fetchHelpTypes()
    })
    .catch(() => {})
}

const handleBatchDelete = () => {
  const ids = selectedRows.value.map((r) => r.id)
  if (!ids.length) return
  ElMessageBox.confirm(`确定删除选中的 ${ids.length} 个分类吗？`, '批量删除', { type: 'warning' })
    .then(async () => {
      const { count } = await batchDeleteHelpTypes(ids)
      ElMessage.success(`已删除 ${count} 条`)
      fetchHelpTypes()
    })
    .catch(() => {})
}

onMounted(fetchHelpTypes)
</script>

<style scoped>
.content-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.search-actions { margin-left: auto; }
.toolbar { display: flex; justify-content: space-between; align-items: flex-start; }
.toolbar-actions { display: flex; gap: 8px; }
.text-show { color: #409eff; }
.text-hide { color: #f56c6c; }
.btn-show { color: #67c23a; border-color: #c2e7b0; background: #f0f9eb; }
.btn-hide { color: #f56c6c; border-color: #fbc4c4; background: #fef0f0; }
.pagination-bar { display: flex; justify-content: space-between; margin-top: 16px; font-size: 13px; color: #606266; }
</style>
