<template>
  <div v-loading="loading" class="ops-list-page">
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="请输入消息标题或编号" clearable style="width: 220px" />
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
          <el-tabs v-if="categoryTabs.length" v-model="activeTab" @tab-change="fetchList">
            <el-tab-pane v-for="tab in categoryTabs" :key="tab.name" :label="tab.label" :name="tab.name" />
          </el-tabs>
          <el-tabs v-else v-model="activeTab" @tab-change="fetchList">
            <el-tab-pane v-for="tab in statusTabs" :key="tab.name" :label="tab.label" :name="tab.name" />
          </el-tabs>
          <el-button type="primary" @click="goAdd">+ 添加推送</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table :data="tableData" border stripe>
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="编号" width="100" align="center" />
        <el-table-column :label="contentColumnLabel" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.title }}</template>
        </el-table-column>
        <el-table-column v-if="showPublishStatus" label="发布状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="row.publishStatus === 1 ? 'text-primary' : ''">{{ row.publishStatusText }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="showAppPush" label="APP推送" width="90" align="center">
          <template #default="{ row }">{{ row.appPush }}</template>
        </el-table-column>
        <el-table-column v-if="showPushStatus" label="推送状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="row.publishStatus === 1 ? 'text-primary' : ''">{{ row.publishStatusText }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="150" />
        <el-table-column v-if="showMsgType" label="消息类型" width="100" align="center">
          <template #default="{ row }">{{ row.msgCategoryText }}</template>
        </el-table-column>
        <el-table-column prop="pushCount" label="推送次数" width="90" align="center" />
        <el-table-column prop="pushVolume" label="推送量" width="90" align="center" />
        <el-table-column v-if="showClickCount" label="点击量" width="80" align="center">
          <template #default="{ row }">{{ row.clickCount ?? '-' }}</template>
        </el-table-column>
        <el-table-column v-if="showReceiveVolume" label="接收量" width="90" align="center">
          <template #default="{ row }">{{ row.receiveVolume }}</template>
        </el-table-column>
        <el-table-column v-if="showPushUser" label="推送用户" width="120" align="center">
          <template #default="{ row }">{{ row.pushUser }}</template>
        </el-table-column>
        <el-table-column label="操作" :width="actionWidth" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button size="small" class="btn-green" @click="handleResend(row)">
              {{ row.publishStatus === 1 ? resendPublishedLabel : resendDraftLabel }}
            </el-button>
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
import { mockSystemMessages, mockSmsMessages, mockStationMessages } from '@/mock/ops'

const props = defineProps({
  msgType: { type: String, required: true },
  addRoute: { type: String, required: true },
  contentColumnLabel: { type: String, default: '消息标题' },
  showPublishStatus: { type: Boolean, default: true },
  showPushStatus: { type: Boolean, default: false },
  showAppPush: { type: Boolean, default: false },
  showMsgType: { type: Boolean, default: false },
  showClickCount: { type: Boolean, default: false },
  showReceiveVolume: { type: Boolean, default: false },
  showPushUser: { type: Boolean, default: false },
  categoryTabs: { type: Array, default: () => [] },
  statusTabs: {
    type: Array,
    default: () => [
      { label: '全部', name: 'all' },
      { label: '已发布', name: 'published' },
      { label: '未发布', name: 'draft' },
    ],
  },
  actionWidth: { type: Number, default: 200 },
  resendPublishedLabel: { type: String, default: '再发' },
  resendDraftLabel: { type: String, default: '发送' },
})

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])

const searchForm = reactive({ title: '', timeType: 'publish', dateRange: ['2024-08-02', '2024-08-23'] })
const pagination = reactive({ page: 1, total: 265, totalPages: 10 })

const mockMap = { SYSTEM: mockSystemMessages, SMS: mockSmsMessages, STATION: mockStationMessages }

/**
 * POST /api/ops/notification/page
 * 参数: { msg_type: props.msgType, page, size, ...searchForm }
 */
const fetchList = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 400))
    let list = [...(mockMap[props.msgType] || [])]
    if (!props.categoryTabs.length && activeTab.value === 'published') {
      list = list.filter((i) => i.publishStatus === 1)
    } else if (!props.categoryTabs.length && activeTab.value === 'draft') {
      list = list.filter((i) => i.publishStatus === 0)
    } else if (props.categoryTabs.length && activeTab.value !== 'all') {
      list = list.filter((i) => i.msgCategory === activeTab.value)
    }
    tableData.value = list
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { fetchList(); ElMessage.success('查询成功') }
const handleReset = () => {
  searchForm.title = ''
  searchForm.dateRange = ['2024-08-02', '2024-08-23']
  activeTab.value = 'all'
  fetchList()
}
const goAdd = () => router.push(props.addRoute)
const handleView = (row) => ElMessage.info(`查看 ${row.id}`)
const handleResend = (row) => ElMessage.success(row.publishStatus === 1 ? '已触发再发' : '已发送')
const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该推送记录吗？', '提示', { type: 'warning' })
    .then(() => ElMessage.success('删除成功'))
    .catch(() => {})
}

onMounted(fetchList)
</script>

<style scoped>
.ops-list-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.search-form { display: flex; flex-wrap: wrap; }
.search-actions { margin-left: auto; }
.toolbar { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 12px; }
.toolbar-right { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; flex: 1; }
.toolbar-right :deep(.el-tabs__header) { margin-bottom: 0; }
.text-primary { color: #409eff; }
.btn-green { color: #67c23a; border-color: #c2e7b0; background: #f0f9eb; }
.btn-red { color: #f56c6c; border-color: #fbc4c4; background: #fef0f0; }
.pagination-bar { display: flex; justify-content: space-between; margin-top: 16px; font-size: 13px; color: #606266; }
</style>
