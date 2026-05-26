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
        <el-tabs v-model="activeTab" @tab-change="fetchHelpTypes">
          <el-tab-pane :label="`全部 (${counts.all})`" name="all" />
          <el-tab-pane :label="`可用 (${counts.active})`" name="active" />
          <el-tab-pane :label="`已禁用 (${counts.disabled})`" name="disabled" />
        </el-tabs>
        <div class="toolbar-actions">
          <el-button type="primary" @click="router.push('/content/help/type/add')">+ 添加分类</el-button>
          <el-button type="danger">批量删除</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table :data="tableData" border stripe>
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="code" label="编号" width="100" align="center" />
        <el-table-column label="类型图标" width="90" align="center">
          <template #default="{ row }">
            <el-image :src="row.icon" style="width: 48px; height: 48px" fit="cover" />
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
        <el-pagination v-model:current-page="pagination.page" :total="pagination.total" layout="prev, pager, next, sizes" background />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mockHelpTypes } from '@/mock/content'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, total: 265, totalPages: 10 })
const counts = reactive({ all: 50, active: 40, disabled: 10 })

/** POST /api/content/help/type/page */
const fetchHelpTypes = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 300))
    let list = [...mockHelpTypes]
    if (activeTab.value === 'active') list = list.filter((i) => i.visible)
    if (activeTab.value === 'disabled') list = list.filter((i) => !i.visible)
    tableData.value = list
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { fetchHelpTypes(); ElMessage.success('查询成功') }
const handleReset = () => { searchForm.keyword = ''; activeTab.value = 'all'; fetchHelpTypes() }
const toggleVisible = (row) => { row.visible = !row.visible; ElMessage.success('已更新') }

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(() => ElMessage.success('删除成功'))
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
