<template>
  <div v-loading="loading" class="order-setting-page">
    <el-card shadow="never" class="panel-card">
      <div class="setting-header">
        <el-tabs v-model="activeSettingTab" class="setting-tabs">
          <el-tab-pane label="退货原因设置" name="returnReason" />
          <el-tab-pane label="快递单模板" name="expressTemplate" />
          <el-tab-pane label="发货地址" name="shipAddress" />
          <el-tab-pane label="退货地址" name="returnAddress" />
        </el-tabs>
        <el-button v-if="activeSettingTab === 'returnReason'" type="primary" :icon="Plus">
          添加原因
        </el-button>
      </div>
    </el-card>

    <el-card v-if="activeSettingTab === 'returnReason'" shadow="never" class="panel-card">
      <el-table :data="settingData.list" border stripe>
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="编号" width="100" align="center" />
        <el-table-column prop="reason" label="原因类型" min-width="200" />
        <el-table-column prop="addTime" label="添加时间" width="160" />
        <el-table-column label="是否显示" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.visible ? 'visible-yes' : 'visible-no'">
              {{ row.visible ? '显示' : '隐藏' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" class="btn-blue" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" class="btn-red" @click="handleDelete(row)">删除</el-button>
            <el-button
              size="small"
              :class="row.visible ? 'btn-red' : 'btn-green-solid'"
              @click="toggleVisible(row)"
            >
              {{ row.visible ? '隐藏' : '显示' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <span class="page-info">
          第{{ settingData.pagination.page }}页 共{{ settingData.pagination.totalPages }}页
          {{ settingData.pagination.total }}条
        </span>
        <el-pagination
          v-model:current-page="settingData.pagination.page"
          v-model:page-size="settingData.pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="settingData.pagination.total"
          layout="prev, pager, next, sizes"
          background
        />
      </div>
    </el-card>

    <el-card v-else shadow="never" class="panel-card placeholder-card">
      <el-empty :description="`${tabLabelMap[activeSettingTab]} 功能开发中`" />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { mockReturnReasons } from '@/mock/order'

const loading = ref(false)
const activeSettingTab = ref('returnReason')

const tabLabelMap = {
  returnReason: '退货原因设置',
  expressTemplate: '快递单模板',
  shipAddress: '发货地址',
  returnAddress: '退货地址',
}

const settingData = reactive({
  list: [],
  pagination: {
    page: 1,
    pageSize: 10,
    total: 265,
    totalPages: 10,
  },
})

/**
 * 获取订单设置列表
 * 此处后续对接 Spring Boot 后端 /api/order/setting/list 接口
 */
const fetchSettingList = async () => {
  loading.value = true
  try {
    await new Promise((resolve) => setTimeout(resolve, 300))
    settingData.list = mockReturnReasons.map((item) => ({ ...item }))
    settingData.pagination.total = 265
    settingData.pagination.totalPages = Math.ceil(265 / settingData.pagination.pageSize)
  } finally {
    loading.value = false
  }
}

/**
 * 实时同步「是否显示」或「隐藏」状态至 Spring Boot 后端
 * PUT /api/order/setting/{id}/visible
 */
const updateSettingStatus = async (row) => {
  // await axios.put(`/api/order/setting/${row.id}/visible`, { visible: row.visible })
  await new Promise((resolve) => setTimeout(resolve, 200))
  ElMessage.success(row.visible ? '已设为显示' : '已设为隐藏')
}

const toggleVisible = async (row) => {
  row.visible = !row.visible
  await updateSettingStatus(row)
}

const handleEdit = (row) => ElMessage.info(`编辑原因：${row.reason}`)
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除「${row.reason}」吗？`, '提示', { type: 'warning' })
    .then(() => ElMessage.success('删除成功'))
    .catch(() => {})
}

onMounted(fetchSettingList)
</script>

<style scoped>
.order-setting-page {
  min-height: calc(100vh - 120px);
}

.panel-card {
  margin-bottom: 12px;
  border-radius: 8px;
  border: none;
}

.setting-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.setting-tabs {
  flex: 1;
}

.setting-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.visible-yes {
  color: #303133;
}

.visible-no {
  color: #f56c6c;
  font-weight: 500;
}

.btn-blue {
  color: #409eff;
  border-color: #b3d8ff;
  background: #ecf5ff;
}

.btn-red {
  color: #f56c6c;
  border-color: #fbc4c4;
  background: #fef0f0;
}

.btn-green-solid {
  color: #fff;
  border-color: #67c23a;
  background: #67c23a;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}

.page-info {
  font-size: 13px;
  color: #606266;
}

.placeholder-card {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
