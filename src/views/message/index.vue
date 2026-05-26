<template>
  <div v-loading="loading" class="message-page">
    <el-card shadow="never" class="type-card">
      <el-radio-group v-model="activeType" @change="handleTypeChange">
        <el-radio-button value="">
          全部消息<span v-if="summary.total" class="count">({{ summary.total }})</span>
        </el-radio-button>
        <el-radio-button value="SYSTEM">
          系统公告<span v-if="summary.systemCount" class="count">({{ summary.systemCount }})</span>
        </el-radio-button>
        <el-radio-button value="AUDIT">
          审批待办<span v-if="summary.auditCount" class="count">({{ summary.auditCount }})</span>
        </el-radio-button>
        <el-radio-button value="ALARM">
          风险预警<span v-if="summary.alarmCount" class="count">({{ summary.alarmCount }})</span>
        </el-radio-button>
      </el-radio-group>
      <div class="toolbar">
        <el-button type="primary" @click="handleMarkAllRead">一键全部标记已读</el-button>
        <el-button type="danger" plain :disabled="!selectedIds.length" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" border stripe @selection-change="onSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column label="紧急级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.level)" size="small">{{ row.levelLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="消息标题" min-width="280">
          <template #default="{ row }">
            <div
              class="title-cell"
              :class="{ unread: row.status === 0, read: row.status === 1 }"
              @click="openDetail(row)"
            >
              <span v-if="row.status === 0" class="unread-dot" />
              <span class="title-text">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="noticeTypeLabel" label="类型" width="110" align="center" />
        <el-table-column prop="createTime" label="接收时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">查看正文</el-button>
            <el-button type="danger" link @click="handleDeleteOne(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, prev, pager, next, sizes"
          background
          @current-change="fetchData"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" :title="detailRow?.title || '消息详情'" width="640px" destroy-on-close>
      <div v-if="detailRow" class="detail-wrap">
        <div class="detail-meta">
          <span>发布时间：{{ detailRow.createTime }}</span>
          <span>发送人：{{ detailRow.sender || '系统' }}</span>
          <el-tag size="small" effect="plain">{{ detailRow.noticeTypeLabel }}</el-tag>
        </div>
        <div class="detail-content" v-html="detailRow.content" />
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchNoticePage,
  markNoticesRead,
  markAllNoticesRead,
  deleteNotices,
  fetchNoticeDetail,
  fetchUnreadNoticeSummary,
} from '@/api/notice'
import { triggerNoticeRefresh } from '@/utils/noticeHelpers'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const selectedIds = ref([])
const activeType = ref('')
const detailVisible = ref(false)
const detailRow = ref(null)

const summary = reactive({
  total: 0,
  systemCount: 0,
  auditCount: 0,
  alarmCount: 0,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const levelTagType = (level) => {
  if (level === 'DANGER') return 'danger'
  if (level === 'WARNING') return 'warning'
  return 'info'
}

const loadSummary = async () => {
  try {
    const data = await fetchUnreadNoticeSummary()
    Object.assign(summary, {
      total: data?.total ?? 0,
      systemCount: data?.systemCount ?? 0,
      auditCount: data?.auditCount ?? 0,
      alarmCount: data?.alarmCount ?? 0,
    })
  } catch {
    /* ignore */
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const data = await fetchNoticePage({
      noticeType: activeType.value || undefined,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = data.list || []
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

const refreshAll = async () => {
  await Promise.all([fetchData(), loadSummary()])
  triggerNoticeRefresh()
}

const handleTypeChange = () => {
  pagination.page = 1
  fetchData()
}

const onSizeChange = () => {
  pagination.page = 1
  fetchData()
}

const onSelectionChange = (rows) => {
  selectedIds.value = rows.map((r) => r.id)
}

const openDetail = async (row) => {
  detailRow.value = { ...row }
  detailVisible.value = true
  if (row.status === 0) {
    await markNoticesRead([row.id])
    row.status = 1
    await loadSummary()
    triggerNoticeRefresh()
  }
}

const handleMarkAllRead = async () => {
  await markAllNoticesRead(activeType.value || undefined)
  ElMessage.success('已全部标记为已读')
  await refreshAll()
}

const handleBatchDelete = async () => {
  await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 条消息？`, '提示', { type: 'warning' })
  await deleteNotices(selectedIds.value)
  ElMessage.success('删除成功')
  selectedIds.value = []
  await refreshAll()
}

const handleDeleteOne = async (row) => {
  await ElMessageBox.confirm('确定删除该消息？', '提示', { type: 'warning' })
  await deleteNotices([row.id])
  ElMessage.success('已删除')
  await refreshAll()
}

const openFromQuery = async () => {
  const id = Number(route.query.id)
  if (!id) return
  try {
    const detail = await fetchNoticeDetail(id)
    if (detail) {
      await openDetail(detail)
    }
  } finally {
    router.replace({ path: route.path, query: {} })
  }
}

watch(detailVisible, (visible) => {
  if (!visible) {
    fetchData()
  }
})

onMounted(async () => {
  await refreshAll()
  await openFromQuery()
})
</script>

<style scoped>
.message-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.type-card :deep(.el-card__body) {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.count {
  margin-left: 2px;
  font-size: 12px;
  opacity: 0.85;
}

.toolbar {
  display: flex;
  gap: 8px;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.title-cell.unread .title-text {
  font-weight: 600;
  color: #303133;
}

.title-cell.read .title-text {
  color: #909399;
}

.unread-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #409eff;
  flex-shrink: 0;
}

.pagination-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.detail-content {
  line-height: 1.7;
  color: #303133;
  font-size: 14px;
}

.detail-content :deep(p) {
  margin: 0 0 10px;
}

.detail-content :deep(strong) {
  color: #409eff;
}
</style>
