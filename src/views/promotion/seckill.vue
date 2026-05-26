<template>
  <div v-loading="loading" class="promo-page">
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="活动">
          <el-input v-model="searchForm.keyword" placeholder="请输入名称或编号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD"
            style="width: 280px"
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
        <el-tabs v-model="activeTab" @tab-change="fetchSeckillList">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane label="待开始" name="pending" />
          <el-tab-pane label="进行中" name="active" />
          <el-tab-pane label="已下架" name="offline" />
          <el-tab-pane label="已结束" name="ended" />
        </el-tabs>
        <div class="toolbar-actions">
          <el-button type="primary" @click="openDialog()">+ 添加活动</el-button>
          <el-button type="success" @click="router.push('/promotion/seckill/time')">时间段设置</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table :data="tableData" border stripe>
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="活动编号" width="100" align="center" />
        <el-table-column prop="title" label="活动标题" min-width="180" />
        <el-table-column label="活动状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="statusClass(row.status)">{{ seckillStatusMap[row.status]?.label }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="150" />
        <el-table-column prop="endTime" label="结束时间" width="150" />
        <el-table-column label="预警通知" width="180">
          <template #default="{ row }">
            <span v-if="row.warning" class="warning-text">{{ row.warning }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="上线/下架" width="100" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.online"
              type="danger"
              link
              @click="toggleOnline(row, false)"
            >
              下架
            </el-button>
            <el-button v-else type="success" link @click="toggleOnline(row, true)">上线</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="success" link @click="goProducts(row)">设置商品</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <span>第{{ pagination.page }}页 共{{ pagination.totalPages }}页 {{ pagination.total }}条</span>
        <el-pagination v-model:current-page="pagination.page" :total="pagination.total" layout="prev, pager, next, sizes" background />
      </div>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑秒杀活动' : '添加秒杀活动'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="dialog.form" :rules="dialogRules" label-width="100px">
        <el-form-item label="活动名称" prop="title">
          <el-input v-model="dialog.form.title" placeholder="请输入（限60字）" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="dialog.form.startTime" type="datetime" placeholder="请选择开始时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="dialog.form.endTime" type="datetime" placeholder="请选择结束时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="上线/下架">
          <el-switch v-model="dialog.form.online" active-text="已上线" inactive-text="已下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">关闭</el-button>
        <el-button type="primary" :loading="dialog.saving" @click="saveActivity">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mockSeckillList, seckillStatusMap } from '@/mock/promotion'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const formRef = ref(null)

const searchForm = reactive({ keyword: '', dateRange: [] })
const pagination = reactive({ page: 1, total: 265, totalPages: 10 })

const dialog = reactive({
  visible: false,
  saving: false,
  isEdit: false,
  form: { id: '', title: '', startTime: '', endTime: '', online: true },
})

const dialogRules = {
  title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
}

const statusClass = (status) => {
  if (status === 'pending') return 'text-primary'
  if (status === 'offline' || status === 'ended') return 'text-danger'
  return ''
}

/**
 * GET /api/promotion/seckill/list
 */
const fetchSeckillList = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 400))
    let list = [...mockSeckillList]
    if (activeTab.value !== 'all') list = list.filter((i) => i.status === activeTab.value)
    tableData.value = list
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { fetchSeckillList(); ElMessage.success('查询成功') }
const handleReset = () => { searchForm.keyword = ''; searchForm.dateRange = []; fetchSeckillList() }

const openDialog = (row) => {
  dialog.isEdit = !!row
  if (row) {
    Object.assign(dialog.form, { id: row.id, title: row.title, startTime: row.startTime, endTime: row.endTime, online: row.online })
  } else {
    Object.assign(dialog.form, { id: '', title: '', startTime: '', endTime: '', online: true })
  }
  dialog.visible = true
}

const saveActivity = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  dialog.saving = true
  try {
    await new Promise((r) => setTimeout(r, 400))
    ElMessage.success('保存成功')
    dialog.visible = false
    fetchSeckillList()
  } finally {
    dialog.saving = false
  }
}

const toggleOnline = (row, online) => {
  row.online = online
  ElMessage.success(online ? '已上线' : '已下架')
}

const goProducts = (row) => router.push(`/promotion/seckill/${row.id}/products`)

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除活动「${row.title}」吗？`, '提示', { type: 'warning' })
    .then(() => ElMessage.success('删除成功'))
    .catch(() => {})
}

onMounted(fetchSeckillList)
</script>

<style scoped>
.promo-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.search-form { display: flex; flex-wrap: wrap; }
.search-actions { margin-left: auto; }
.toolbar { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
.toolbar :deep(.el-tabs__header) { margin-bottom: 0; }
.toolbar-actions { display: flex; gap: 8px; }
.warning-text { color: #f56c6c; font-size: 12px; }
.text-primary { color: #409eff; }
.text-danger { color: #f56c6c; }
.pagination-bar { display: flex; justify-content: space-between; margin-top: 16px; font-size: 13px; color: #606266; }
</style>
