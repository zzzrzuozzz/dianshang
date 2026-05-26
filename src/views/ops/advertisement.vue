<template>
  <div v-loading="loading" class="adv-page">
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="请输入标题或编号" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item label="时间">
          <el-select v-model="searchForm.timeType" style="width: 110px">
            <el-option label="发布时间" value="publish" />
          </el-select>
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            value-format="YYYY-MM-DD"
            style="width: 260px; margin-left: 8px"
          />
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
        <div class="toolbar-right">
          <el-tabs v-model="activeTab" @tab-change="fetchAdvertisements">
            <el-tab-pane label="全部" name="all" />
            <el-tab-pane label="上线" name="online" />
            <el-tab-pane label="下架" name="offline" />
          </el-tabs>
          <el-button type="primary" @click="router.push('/ops/advertisement/add')">+ 添加广告</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table :data="tableData" border stripe>
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="编号" width="100" align="center" />
        <el-table-column prop="name" label="广告名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="publishTime" label="发布时间" width="150" />
        <el-table-column prop="endTime" label="结束时间" width="150" />
        <el-table-column prop="advType" label="广告类型" width="130" />
        <el-table-column prop="exposureCount" label="曝光次数" width="90" align="center" />
        <el-table-column prop="clickCount" label="点击次数" width="90" align="center" />
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column label="上架状态" width="140" align="center">
          <template #default="{ row }">
            <span :class="row.status === 1 ? 'text-primary' : ''">{{ row.statusText }}</span>
            <el-button
              size="small"
              :class="row.status === 1 ? 'btn-red' : 'btn-green'"
              class="status-btn"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上线' }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button size="small" class="btn-green" @click="handlePin(row)">置顶</el-button>
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
import { mockAdvertisements } from '@/mock/ops'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])

const searchForm = reactive({ title: '', timeType: 'publish', dateRange: ['2024-08-02', '2024-08-23'] })
const pagination = reactive({ page: 1, total: 265, totalPages: 10 })

/**
 * POST /api/ops/advertisement/page
 * 上下架时需同步刷新 Redis 广告缓存（String/ZSet）
 */
const fetchAdvertisements = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 400))
    let list = [...mockAdvertisements]
    if (activeTab.value === 'online') list = list.filter((i) => i.status === 1)
    if (activeTab.value === 'offline') list = list.filter((i) => i.status === 0)
    tableData.value = list
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { fetchAdvertisements(); ElMessage.success('查询成功') }
const handleReset = () => {
  searchForm.title = ''
  searchForm.dateRange = ['2024-08-02', '2024-08-23']
  activeTab.value = 'all'
  fetchAdvertisements()
}

const toggleStatus = async (row) => {
  row.status = row.status === 1 ? 0 : 1
  row.statusText = row.status === 1 ? '已上线' : '已下架'
  ElMessage.success('状态已更新（联调时后端需清除 Redis 缓存）')
}

const handleView = (row) => router.push(`/ops/advertisement/edit/${row.id}`)
const handlePin = (row) => ElMessage.success(`${row.name} 已置顶`)
const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该广告吗？', '提示', { type: 'warning' })
    .then(() => ElMessage.success('删除成功'))
    .catch(() => {})
}

onMounted(fetchAdvertisements)
</script>

<style scoped>
.adv-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.search-form { display: flex; flex-wrap: wrap; }
.search-actions { margin-left: auto; }
.toolbar { display: flex; justify-content: space-between; align-items: flex-start; }
.toolbar-right { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; }
.text-primary { color: #409eff; }
.status-btn { margin-left: 6px; }
.btn-green { color: #67c23a; border-color: #c2e7b0; background: #f0f9eb; }
.btn-red { color: #f56c6c; border-color: #fbc4c4; background: #fef0f0; }
.pagination-bar { display: flex; justify-content: space-between; margin-top: 16px; font-size: 13px; color: #606266; }
</style>
