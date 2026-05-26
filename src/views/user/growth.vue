<template>
  <div v-loading="loading" class="growth-page">
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
      <div class="action-bar">
        <el-button type="primary" size="small">规则设置</el-button>
        <el-button type="primary" size="small">奖励设置</el-button>
        <el-button type="primary" size="small">批量设置</el-button>
        <el-button type="primary" size="small">导出数据</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-tabs v-model="activeTab" @tab-change="fetchGrowthList">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="普通会员" name="normal" />
        <el-tab-pane label="黄金会员" name="gold" />
        <el-tab-pane label="铂金会员" name="platinum" />
        <el-tab-pane label="钻石会员" name="diamond" />
      </el-tabs>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table :data="tableData" border stripe>
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="用户编号" width="100" align="center" />
        <el-table-column prop="nickname" label="用户昵称" width="100" />
        <el-table-column prop="account" label="用户账号" width="120" />
        <el-table-column prop="level" label="用户等级" width="100" align="center" />
        <el-table-column prop="points" label="可用积分" width="100" align="center" />
        <el-table-column prop="growth" label="成长值" width="100" align="center" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openAdjust(row, 'growth')">成长值</el-button>
            <el-button type="success" link @click="openAdjust(row, 'points')">积分</el-button>
            <el-button type="danger" link @click="openAdjust(row, 'settings')">设置</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination v-model:current-page="pagination.page" :total="pagination.total" layout="prev, pager, next, sizes" background />
      </div>
    </el-card>

    <el-dialog v-model="adjustDialog.visible" :title="adjustDialog.title" width="400px">
      <el-form label-width="80px">
        <el-form-item label="调整值">
          <el-input-number v-model="adjustDialog.value" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="adjustDialog.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="adjustDialog.saving" @click="updateUserGrowthOrScore">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { mockUserList } from '@/mock/user'

const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const searchForm = reactive({ account: '', nickname: '' })
const pagination = reactive({ page: 1, total: 265 })

const adjustDialog = reactive({
  visible: false,
  saving: false,
  title: '',
  type: '',
  row: null,
  value: 0,
  remark: '',
})

const fetchGrowthList = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 400))
    let list = [...mockUserList]
    if (activeTab.value !== 'all') list = list.filter((u) => u.levelKey === activeTab.value)
    tableData.value = list
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { fetchGrowthList(); ElMessage.success('查询成功') }
const handleReset = () => { searchForm.account = ''; searchForm.nickname = ''; fetchGrowthList() }

const openAdjust = (row, type) => {
  adjustDialog.row = row
  adjustDialog.type = type
  adjustDialog.title = type === 'growth' ? '调整成长值' : type === 'points' ? '调整积分' : '用户设置'
  adjustDialog.value = type === 'growth' ? row.growth : type === 'points' ? row.points : 0
  adjustDialog.remark = ''
  adjustDialog.visible = true
}

/**
 * POST /api/user/growth/adjust
 */
const updateUserGrowthOrScore = async () => {
  adjustDialog.saving = true
  try {
    // await axios.post('/api/user/growth/adjust', { userId: adjustDialog.row.id, type: adjustDialog.type, value: adjustDialog.value })
    await new Promise((r) => setTimeout(r, 400))
    if (adjustDialog.type === 'growth') adjustDialog.row.growth = adjustDialog.value
    if (adjustDialog.type === 'points') adjustDialog.row.points = adjustDialog.value
    ElMessage.success('调整成功')
    adjustDialog.visible = false
  } finally {
    adjustDialog.saving = false
  }
}

onMounted(fetchGrowthList)
</script>

<style scoped>
.growth-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.search-actions { margin-left: auto; }
.action-bar { display: flex; flex-wrap: wrap; gap: 8px; justify-content: flex-end; }
.pagination-bar { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
