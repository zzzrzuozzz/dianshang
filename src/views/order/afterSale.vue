<template>
  <div v-loading="loading" class="after-sale-page">
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品">
          <el-input v-model="searchForm.product" placeholder="请输入名称或编号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderId" placeholder="请输入订单编号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="searchForm.logisticsNo" placeholder="请输入物流单号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="售后编号">
          <el-input v-model="searchForm.afterSaleId" placeholder="请输入售后编号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="时间">
          <el-select v-model="searchForm.timeType" style="width: 120px">
            <el-option label="下单时间" value="create" />
            <el-option label="申请时间" value="apply" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane
          v-for="tab in statusTabs"
          :key="tab.key"
          :name="tab.key"
          :label="`${tab.label}(${tab.count})`"
        />
      </el-tabs>
    </el-card>

    <el-card shadow="never" class="panel-card table-card">
      <el-table :data="tableData" border stripe>
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="售后编号" width="100" align="center" />

        <el-table-column label="商品信息" min-width="200">
          <template #default="{ row }">
            <div class="product-info">
              <el-image :src="row.thumb" fit="cover" class="product-thumb" />
              <span class="product-name">{{ row.productName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="orderId" label="订单编号" width="100" align="center" />

        <el-table-column label="订单状态" width="90" align="center">
          <template #default="{ row }">
            {{ orderStatusLabel[row.orderStatus] || row.orderStatus }}
          </template>
        </el-table-column>

        <el-table-column label="发货状态" width="90" align="center">
          <template #default="{ row }">
            {{ shipStatusLabel[row.shipStatus] || row.shipStatus }}
          </template>
        </el-table-column>

        <el-table-column label="售后状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="warning">{{ afterSaleStatusLabelMap[row.afterSaleStatus] }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="售后类型" width="100" align="center">
          <template #default="{ row }">
            {{ afterSaleTypeLabel[row.afterSaleType] || row.afterSaleType }}
          </template>
        </el-table-column>

        <el-table-column prop="refundAmount" label="退款金额" width="90" align="center">
          <template #default="{ row }">
            {{ Number(row.refundAmount).toFixed(2) }}
          </template>
        </el-table-column>

        <el-table-column prop="applyTime" label="申请时间" width="140" />
        <el-table-column prop="processTime" label="处理时间" width="140">
          <template #default="{ row }">
            {{ row.processTime || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <template v-if="row.afterSaleStatus === 'platform_pending'">
              <el-button v-perm="'order:refund'" type="success" link @click="handleApprove(row)">同意</el-button>
              <el-button v-perm="'order:refund'" type="danger" link @click="handleReject(row)">拒绝</el-button>
            </template>
            <el-button
              v-if="row.afterSaleStatus === 'platform_confirm'"
              v-perm="'order:refund'"
              type="warning"
              link
              @click="handleConfirmReturn(row)"
            >
              确认收货
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <span class="page-info">
          第{{ pagination.page }}页 共{{ pagination.totalPages }}页 {{ pagination.total }}条
        </span>
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="prev, pager, next, sizes"
          background
          @current-change="fetchAfterSaleList"
          @size-change="onPageSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  afterSaleStatusLabel,
  afterSaleTypeLabel,
  orderStatusLabel,
  shipStatusLabel,
} from '@/constants/order'
import {
  approveAfterSale,
  confirmAfterSaleReturn,
  fetchAfterSaleList as loadAfterSaleList,
  rejectAfterSale,
} from '@/api/order'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('platform_pending')
const tableData = ref([])
const statusTabs = ref([])

const afterSaleStatusLabelMap = afterSaleStatusLabel

const searchForm = reactive({
  product: '',
  orderId: '',
  logisticsNo: '',
  afterSaleId: '',
  timeType: 'apply',
  dateRange: null,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
  totalPages: 0,
})

const fetchAfterSaleList = async () => {
  loading.value = true
  try {
    const data = await loadAfterSaleList({
      product: searchForm.product || undefined,
      orderId: searchForm.orderId || undefined,
      logisticsNo: searchForm.logisticsNo || undefined,
      afterSaleId: searchForm.afterSaleId || undefined,
      timeType: searchForm.timeType,
      startDate: searchForm.dateRange?.[0],
      endDate: searchForm.dateRange?.[1],
      status: activeTab.value,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = data.list
    pagination.total = data.total
    pagination.totalPages = data.totalPages
    statusTabs.value = data.tabs || []
  } finally {
    loading.value = false
  }
}

const onPageSizeChange = () => {
  pagination.page = 1
  fetchAfterSaleList()
}

const onTabChange = () => {
  pagination.page = 1
  fetchAfterSaleList()
}

const handleSearch = () => {
  pagination.page = 1
  fetchAfterSaleList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    product: '',
    orderId: '',
    logisticsNo: '',
    afterSaleId: '',
    timeType: 'apply',
    dateRange: null,
  })
  pagination.page = 1
  fetchAfterSaleList()
}

const handleView = (row) => {
  router.push({ path: `/order/detail/${row.orderId}`, query: { status: 'after_sale' } })
}

const handleApprove = (row) => {
  ElMessageBox.confirm(`确定同意售后单 ${row.id} 吗？仅退款类型将直接完成退款。`, '同意售后', { type: 'warning' })
    .then(async () => {
      await approveAfterSale(row.id)
      ElMessage.success('已同意售后申请')
      fetchAfterSaleList()
    })
    .catch(() => {})
}

const handleReject = (row) => {
  ElMessageBox.prompt('请输入拒绝原因（可选）', '拒绝售后', {
    confirmButtonText: '确定拒绝',
    cancelButtonText: '取消',
    inputPlaceholder: '拒绝原因',
  })
    .then(async ({ value }) => {
      await rejectAfterSale(row.id, value || undefined)
      ElMessage.success('已拒绝售后申请')
      fetchAfterSaleList()
    })
    .catch(() => {})
}

const handleConfirmReturn = (row) => {
  ElMessageBox.confirm(`确认已收到退货并完成退款吗？`, '确认收货', { type: 'warning' })
    .then(async () => {
      await confirmAfterSaleReturn(row.id)
      ElMessage.success('已确认收货并完成退款')
      fetchAfterSaleList()
    })
    .catch(() => {})
}

onMounted(fetchAfterSaleList)
</script>

<style scoped>
.after-sale-page {
  min-height: calc(100vh - 120px);
}

.panel-card {
  margin-bottom: 12px;
  border-radius: 8px;
  border: none;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.search-actions {
  margin-left: auto;
}

.product-info {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.product-thumb {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  flex-shrink: 0;
}

.product-name {
  font-size: 13px;
  color: #303133;
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
</style>
