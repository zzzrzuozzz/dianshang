<template>
  <div v-loading="loading" class="user-list-page">
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="账号">
          <el-input v-model="searchForm.account" placeholder="请输入手机号或ID" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="searchForm.nickname" placeholder="请输入昵称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="时间">
          <el-select v-model="searchForm.timeType" style="width: 110px">
            <el-option label="发布时间" value="register" />
          </el-select>
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
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
      <div class="action-bar">
        <el-button type="primary" size="small">赠送优惠券</el-button>
        <el-button type="primary" size="small">群发短信</el-button>
        <el-button type="primary" size="small">群发站内信</el-button>
        <el-button type="primary" size="small">APP推送</el-button>
        <el-button type="primary" size="small">设置标签</el-button>
        <el-button type="primary" size="small" @click="handleExport">导出数据</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="正常" name="normal" />
        <el-tab-pane label="禁止提现" name="withdraw_banned" />
        <el-tab-pane label="禁止登录" name="login_banned" />
      </el-tabs>
    </el-card>

    <el-card shadow="never" class="panel-card table-card">
      <el-table :data="tableData" border stripe @selection-change="(r) => (selected = r)">
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="用户编号" width="100" align="center" />
        <el-table-column prop="nickname" label="用户昵称" width="100" />
        <el-table-column prop="account" label="用户账号" width="120" />
        <el-table-column prop="level" label="用户等级" width="100" align="center" />
        <el-table-column prop="consumeAmount" label="消费金额" width="100" align="center">
          <template #default="{ row }">{{ Number(row.consumeAmount).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数量" width="90" align="center" />
        <el-table-column prop="points" label="可用积分" width="90" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.status !== 'normal' ? 'status-danger' : ''">{{ row.statusText }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="100" show-overflow-tooltip />
        <el-table-column prop="registerTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row)">查看</el-button>
            <el-button size="small" class="btn-green" @click="goEdit(row)">编辑</el-button>
            <el-button size="small" class="btn-red" @click="handlePermission(row)">权限</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <span class="page-info">第{{ pagination.page }}页 共{{ pagination.totalPages }}页 {{ pagination.total }}条</span>
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          layout="prev, pager, next, sizes"
          background
          @current-change="fetchUserList"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { exportUserList, fetchUserList as getUserListApi } from '@/api/user'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const selected = ref([])

const searchForm = reactive({
  account: '',
  nickname: '',
  timeType: 'register',
  dateRange: null,
})

const pagination = reactive({ page: 1, pageSize: 10, total: 0, totalPages: 0 })

const fetchUserList = async () => {
  loading.value = true
  try {
    const [startDate, endDate] = searchForm.dateRange || []
    const data = await getUserListApi({
      account: searchForm.account || undefined,
      nickname: searchForm.nickname || undefined,
      tab: activeTab.value,
      startDate,
      endDate,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = data.list
    pagination.total = data.total
    pagination.totalPages = data.totalPages
  } finally {
    loading.value = false
  }
}

const onTabChange = () => {
  pagination.page = 1
  fetchUserList()
}

const onSizeChange = () => {
  pagination.page = 1
  fetchUserList()
}

const handleSearch = () => {
  pagination.page = 1
  fetchUserList()
  ElMessage.success('查询成功')
}

const handleReset = () => {
  Object.assign(searchForm, { account: '', nickname: '', dateRange: null })
  activeTab.value = 'all'
  pagination.page = 1
  fetchUserList()
}

const handleExport = async () => {
  try {
    await exportUserList({
      account: searchForm.account || '',
      nickname: searchForm.nickname || '',
      tab: activeTab.value,
    })
    ElMessage.success('导出成功')
  } catch {
    /* handled */
  }
}

const goDetail = (row) => router.push(`/user/detail/${row.id}`)
const goEdit = (row) => router.push(`/user/edit/${row.id}`)
const handlePermission = (row) => router.push(`/user/edit/${row.id}`)

onMounted(() => {
  const q = route.query.search
  if (q && typeof q === 'string') searchForm.account = q
  fetchUserList()
})
</script>

<style scoped>
.user-list-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.search-form { display: flex; flex-wrap: wrap; }
.search-actions { margin-left: auto; }
.action-bar { display: flex; flex-wrap: wrap; gap: 8px; justify-content: flex-end; }
.status-danger { color: #f56c6c; font-weight: 500; }
.btn-green { color: #67c23a; border-color: #c2e7b0; background: #f0f9eb; }
.btn-red { color: #f56c6c; border-color: #fbc4c4; background: #fef0f0; }
.pagination-bar { display: flex; justify-content: space-between; margin-top: 16px; }
.page-info { font-size: 13px; color: #606266; }
</style>
