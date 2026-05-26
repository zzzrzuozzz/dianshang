<template>
  <div v-loading="loading" class="level-page">
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="名称">
          <el-input v-model="searchForm.name" placeholder="请输入名称或ID" clearable style="width: 200px" />
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
        <el-button type="primary" @click="router.push('/user/level/edit/new')">新增会员</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table :data="tableData" border stripe>
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="编号" width="100" align="center" />
        <el-table-column prop="name" label="等级名称" width="120" />
        <el-table-column label="默认等级" width="90" align="center">
          <template #default="{ row }">{{ row.isDefault ? '默认' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="growthPoint" label="成长值满足点" width="120" align="center" />
        <el-table-column prop="freeShipping" label="免运费标准" min-width="160" />
        <el-table-column prop="reviewReward" label="评价奖励" width="130" />
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" class="btn-green" @click="goEdit(row)">编辑</el-button>
            <el-button size="small" class="btn-red" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteLevel, fetchLevelList } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ name: '' })

const fetchLevelListData = async () => {
  loading.value = true
  try {
    tableData.value = await fetchLevelList({ name: searchForm.name || undefined })
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { fetchLevelListData(); ElMessage.success('查询成功') }
const handleReset = () => { searchForm.name = ''; fetchLevelListData() }
const goEdit = (row) => router.push(`/user/level/edit/${row.id}`)
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除等级「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteLevel(row.id)
      ElMessage.success('删除成功')
      fetchLevelListData()
    })
    .catch(() => {})
}

onMounted(fetchLevelListData)
</script>

<style scoped>
.level-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.search-actions { margin-left: auto; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.btn-green { color: #67c23a; border-color: #c2e7b0; background: #f0f9eb; }
.btn-red { color: #f56c6c; border-color: #fbc4c4; background: #fef0f0; }
</style>
