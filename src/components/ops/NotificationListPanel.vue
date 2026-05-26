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
        <div class="toolbar-left">
          <el-checkbox
            v-model="allChecked"
            :indeterminate="indeterminate"
            @change="onToggleAll"
          >
            全选
          </el-checkbox>
          <el-button
            size="small"
            class="btn-green"
            :disabled="!selectedRows.length"
            @click="handleBatchResend"
          >
            批量{{ batchSendLabel }}
          </el-button>
          <el-button
            size="small"
            class="btn-red"
            :disabled="!selectedRows.length"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
          <span v-if="selectedRows.length" class="selected-tip">已选 {{ selectedRows.length }} 条</span>
        </div>
        <div class="toolbar-right">
          <el-tabs v-if="categoryTabs.length" v-model="activeTab" @tab-change="onTabChange">
            <el-tab-pane v-for="tab in categoryTabs" :key="tab.name" :label="tab.label" :name="tab.name" />
          </el-tabs>
          <el-tabs v-else v-model="activeTab" @tab-change="onTabChange">
            <el-tab-pane v-for="tab in statusTabs" :key="tab.name" :label="tab.label" :name="tab.name" />
          </el-tabs>
          <div class="add-actions">
            <template v-if="showPresets">
              <el-button
                v-for="preset in systemPushPresets"
                :key="preset.label"
                @click="goAddWithQuery(preset.query)"
              >
                {{ preset.label }}
              </el-button>
            </template>
            <template v-if="stationPresets.length">
              <el-button
                v-for="preset in stationPresets"
                :key="preset.label"
                @click="goAddWithQuery(preset.query)"
              >
                {{ preset.label }}
              </el-button>
            </template>
            <el-button type="primary" @click="goAdd">+ 添加推送</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table
        ref="tableRef"
        :data="tableData"
        row-key="id"
        border
        stripe
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" reserve-selection />
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
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" class="btn-green" @click="handleResend(row)">
              {{ row.publishStatus === 1 ? resendPublishedLabel : resendDraftLabel }}
            </el-button>
            <el-button size="small" class="btn-red" @click="handleDelete(row)">删除</el-button>
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
          @current-change="fetchList"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  systemPushPresets,
  fetchNotificationList,
  fetchNotificationDetail,
  resendNotification,
  deleteNotification,
  batchDeleteNotifications,
  batchResendNotifications,
} from '@/api/ops'

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
  showPresets: { type: Boolean, default: false },
  stationPresets: { type: Array, default: () => [] },
  categoryTabs: { type: Array, default: () => [] },
  statusTabs: {
    type: Array,
    default: () => [
      { label: '全部', name: 'all' },
      { label: '已发布', name: 'published' },
      { label: '未发布', name: 'draft' },
    ],
  },
  actionWidth: { type: Number, default: 260 },
  resendPublishedLabel: { type: String, default: '再发' },
  resendDraftLabel: { type: String, default: '发送' },
  batchSendLabel: { type: String, default: '发送' },
})

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const tableRef = ref()
const selectedRows = ref([])

const editRouteBase = computed(() => props.addRoute.replace(/\/add$/, '/edit'))

const searchForm = reactive({ title: '', timeType: 'publish', dateRange: null })
const pagination = reactive({ page: 1, pageSize: 10, total: 0, totalPages: 1 })

const allChecked = computed({
  get: () => tableData.value.length > 0 && selectedRows.value.length === tableData.value.length,
  set: () => {},
})

const indeterminate = computed(() => {
  const n = selectedRows.value.length
  return n > 0 && n < tableData.value.length
})

const clearSelection = () => {
  selectedRows.value = []
  tableRef.value?.clearSelection()
}

const onSelectionChange = (rows) => {
  selectedRows.value = rows
}

const onToggleAll = (checked) => {
  if (!tableRef.value) return
  tableRef.value.clearSelection()
  if (checked) {
    tableData.value.forEach((row) => tableRef.value.toggleRowSelection(row, true))
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const [startDate, endDate] = searchForm.dateRange || []
    const data = await fetchNotificationList({
      msgType: props.msgType,
      tab: activeTab.value,
      title: searchForm.title || undefined,
      startDate,
      endDate,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = data.list
    pagination.total = data.total
    pagination.totalPages = data.totalPages
    clearSelection()
  } finally {
    loading.value = false
  }
}

const onTabChange = () => {
  pagination.page = 1
  fetchList()
}

const onSizeChange = () => {
  pagination.page = 1
  fetchList()
}

const handleSearch = () => {
  pagination.page = 1
  fetchList()
  ElMessage.success('查询成功')
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.dateRange = null
  activeTab.value = 'all'
  pagination.page = 1
  fetchList()
}

const goAdd = () => router.push(props.addRoute)

const goAddWithQuery = (query) => {
  router.push({ path: props.addRoute, query })
}

const handleEdit = (row) => {
  router.push(`${editRouteBase.value}/${row.id}`)
}

const handleView = async (row) => {
  try {
    const detail = await fetchNotificationDetail(row.id)
    const lines = [
      `编号：${detail.notifyCode || row.id}`,
      `标题：${detail.title}`,
    ]
    if (detail.smsContent) lines.push(`内容：${detail.smsContent}`)
    if (detail.stationContent) lines.push(`内容：${detail.stationContent}`)
    if (detail.intro) lines.push(`简介：${detail.intro}`)
    if (detail.jumpType) {
      lines.push(`跳转：${detail.jumpType}${detail.innerLinkType ? ' / ' + detail.innerLinkType : ''}`)
    }
    ElMessageBox.alert(lines.join('\n'), '推送详情', { confirmButtonText: '关闭' })
  } catch {
    /* handled by interceptor */
  }
}

const handleResend = async (row) => {
  try {
    await resendNotification(row.id)
    ElMessage.success(row.publishStatus === 1 ? '已触发再发' : '已发送')
    fetchList()
  } catch {
    /* handled */
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该推送记录吗？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteNotification(row.id)
      ElMessage.success('删除成功')
      fetchList()
    })
    .catch(() => {})
}

const handleBatchDelete = () => {
  const ids = selectedRows.value.map((r) => r.id)
  if (!ids.length) return
  ElMessageBox.confirm(`确定删除选中的 ${ids.length} 条记录吗？`, '批量删除', { type: 'warning' })
    .then(async () => {
      const { count } = await batchDeleteNotifications(ids)
      ElMessage.success(`已删除 ${count} 条`)
      fetchList()
    })
    .catch(() => {})
}

const handleBatchResend = () => {
  const ids = selectedRows.value.map((r) => r.id)
  if (!ids.length) return
  ElMessageBox.confirm(`确定为选中的 ${ids.length} 条记录执行推送吗？`, '批量推送', { type: 'info' })
    .then(async () => {
      const { count } = await batchResendNotifications(ids)
      ElMessage.success(`已成功处理 ${count} 条`)
      fetchList()
    })
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
.toolbar-left { display: flex; align-items: center; flex-wrap: wrap; gap: 10px; }
.toolbar-right { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; flex: 1; }
.toolbar-right :deep(.el-tabs__header) { margin-bottom: 0; }
.add-actions { display: flex; flex-wrap: wrap; gap: 8px; justify-content: flex-end; }
.selected-tip { font-size: 13px; color: #909399; }
.text-primary { color: #409eff; }
.btn-green { color: #67c23a; border-color: #c2e7b0; background: #f0f9eb; }
.btn-red { color: #f56c6c; border-color: #fbc4c4; background: #fef0f0; }
.pagination-bar { display: flex; justify-content: space-between; margin-top: 16px; font-size: 13px; color: #606266; }
</style>
