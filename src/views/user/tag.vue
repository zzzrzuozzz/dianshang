<template>
  <div v-loading="loading" class="tag-page">
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="账号">
          <el-input v-model="searchForm.account" placeholder="请输入手机号或ID" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="searchForm.nickname" placeholder="请输入昵称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <div class="toolbar">
        <el-checkbox>全选</el-checkbox>
        <el-button type="primary" @click="router.push('/user/tag/edit/new')">新增标签</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table :data="tableData" border stripe>
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="编号" width="100" align="center" />
        <el-table-column prop="name" label="标签名称" width="120" />
        <el-table-column label="会员人数" width="100" align="center">
          <template #default="{ row }">
            <el-link type="primary" @click="goTagUsers(row)">{{ row.memberCount }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="condition" label="自动打标签条件" min-width="280" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button size="small" class="btn-green" @click="goEdit(row)">编辑</el-button>
            <el-button size="small" class="btn-red" @click="handleDelete(row)">删除</el-button>
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
import { mockTagList } from '@/mock/user'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ account: '', nickname: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 265, totalPages: 10 })

/** GET /api/user/tag/list */
const fetchTagList = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 400))
    tableData.value = [...mockTagList]
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { fetchTagList(); ElMessage.success('查询成功') }
const handleReset = () => { searchForm.account = ''; searchForm.nickname = ''; fetchTagList() }
const goTagUsers = (row) => router.push({ path: '/user/tag/users', query: { tagId: row.id, tagName: row.name } })
const goEdit = (row) => router.push(`/user/tag/edit/${row.id}`)
const handleView = (row) => router.push(`/user/tag/edit/${row.id}`)
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除标签「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(() => ElMessage.success('删除成功')).catch(() => {})
}

onMounted(fetchTagList)
</script>

<style scoped>
.tag-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.search-actions { margin-left: auto; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.btn-green { color: #67c23a; border-color: #c2e7b0; background: #f0f9eb; }
.btn-red { color: #f56c6c; border-color: #fbc4c4; background: #fef0f0; }
.pagination-bar { display: flex; justify-content: space-between; margin-top: 16px; font-size: 13px; color: #606266; }
</style>
