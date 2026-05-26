<template>
  <div v-loading="loading" class="order-panel">
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
        <el-form-item label="收货人电话">
          <el-input v-model="searchForm.phone" placeholder="请输入电话" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="时间">
          <el-select v-model="searchForm.timeType" style="width: 120px">
            <el-option label="下单时间" value="create" />
            <el-option label="付款时间" value="pay" />
            <el-option label="发货时间" value="ship" />
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
      <div class="batch-actions">
        <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
        <div class="batch-btns">
          <template v-if="pageType === 'list'">
            <el-button type="primary" size="small">合并订单</el-button>
            <el-button type="primary" size="small">下载配货单</el-button>
            <el-button type="primary" size="small">打印发货单</el-button>
            <el-button
              type="primary"
              size="small"
              :disabled="!selectedRows.length"
              @click="openExpressPrint()"
            >
              打印快递单
            </el-button>
            <el-button type="primary" size="small">批量发货</el-button>
            <el-button type="primary" size="small">导出订单</el-button>
          </template>
          <template v-else>
            <el-button type="success" size="small" @click="handleBatchConfirm">批量确定收货</el-button>
            <el-button type="primary" size="small">导出订单</el-button>
          </template>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-tabs v-model="orderListData.activeTab" @tab-change="onTabChange">
        <el-tab-pane
          v-for="tab in statusTabs"
          :key="tab.key"
          :name="tab.key"
          :label="`${tab.label}(${tab.count})`"
        />
      </el-tabs>
    </el-card>

    <el-card shadow="never" class="panel-card table-card">
      <el-table
        ref="tableRef"
        :data="orderListData.list"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="订单编号" width="100" align="center" fixed="left" />

        <el-table-column label="商品信息" min-width="200">
          <template #default="{ row }">
            <div class="product-info">
              <el-image :src="row.thumb" fit="cover" class="product-thumb" />
              <span class="product-name">{{ row.productName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="spec" label="规格" width="130" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" width="60" align="center" />

        <el-table-column label="交易金额" width="120" align="center">
          <template #default="{ row }">
            <div class="amount-cell">
              <span>实付金额: {{ row.actualAmount }}元</span>
              <span class="discount">优惠折扣: {{ row.discount }}元</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="运费" width="70" align="center">
          <template #default="{ row }">
            {{ row.freightFree ? '包邮' : `${row.freight}元` }}
          </template>
        </el-table-column>

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

        <el-table-column label="物流单号" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.logistics || '-' }}
          </template>
        </el-table-column>

        <el-table-column :label="pageType === 'confirm' ? '自动确认收货时间' : '付款时间'" width="155">
          <template #default="{ row }">
            {{ pageType === 'confirm' ? row.autoConfirmTime || '-' : row.payTime || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="收货信息" min-width="180">
          <template #default="{ row }">
            <div class="receiver-cell">
              <span>{{ row.receiverName }} {{ row.receiverPhone }}</span>
              <span class="address">{{ row.receiverAddress }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="供应商" width="120">
          <template #default="{ row }">
            <div class="supplier-cell">
              <span>{{ row.supplier }}</span>
              <span class="sub">{{ row.supplierPhone }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <template v-if="pageType === 'list'">
              <el-button
                v-if="row.orderStatus === 'pending_ship'"
                type="success"
                size="small"
                plain
                @click="handleShip(row)"
              >
                发货
              </el-button>
              <el-button
                v-if="row.orderStatus === 'pending_ship' || row.orderStatus === 'shipped'"
                type="danger"
                size="small"
                plain
                @click="handleRefund(row)"
              >
                退款
              </el-button>
              <el-button
                v-if="row.orderStatus === 'pending_ship' || row.orderStatus === 'shipped'"
                type="primary"
                size="small"
                plain
                @click="openExpressPrint(row)"
              >
                快递单
              </el-button>
            </template>
            <template v-else>
              <el-button type="success" size="small" plain @click="handleConfirm(row)">确认收货</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <span class="page-info">
          第{{ orderListData.pagination.page }}页 共{{ orderListData.pagination.totalPages }}页
          {{ orderListData.pagination.total }}条
        </span>
        <el-pagination
          v-model:current-page="orderListData.pagination.page"
          v-model:page-size="orderListData.pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="orderListData.pagination.total"
          layout="prev, pager, next, sizes"
          background
          @current-change="fetchOrderList"
          @size-change="onPageSizeChange"
        />
      </div>
    </el-card>

    <ExpressWaybillDialog ref="expressDialogRef" />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import ExpressWaybillDialog from '@/components/express/ExpressWaybillDialog.vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderStatusLabel, shipStatusLabel } from '@/mock/order'
import {
  batchConfirmOrders,
  confirmOrder,
  fetchOrderList as loadOrderList,
  refundOrder,
  shipOrder,
} from '@/api/order'

const props = defineProps({
  pageType: {
    type: String,
    default: 'list',
    validator: (v) => ['list', 'confirm'].includes(v),
  },
})

const router = useRouter()
const loading = ref(false)
const selectAll = ref(false)
const tableRef = ref(null)
const expressDialogRef = ref(null)
const selectedRows = ref([])
const statusTabs = ref([])

const searchForm = reactive({
  product: '',
  orderId: '',
  logisticsNo: '',
  phone: '',
  timeType: 'create',
  dateRange: null,
})

const orderListData = reactive({
  activeTab: props.pageType === 'confirm' ? 'shipped' : 'pending_ship',
  list: [],
  pagination: {
    page: 1,
    pageSize: 10,
    total: 0,
    totalPages: 0,
  },
})

const buildQuery = () => ({
  product: searchForm.product || undefined,
  orderId: searchForm.orderId || undefined,
  logisticsNo: searchForm.logisticsNo || undefined,
  phone: searchForm.phone || undefined,
  timeType: searchForm.timeType,
  startDate: searchForm.dateRange?.[0],
  endDate: searchForm.dateRange?.[1],
  status: orderListData.activeTab,
  pageType: props.pageType,
  page: orderListData.pagination.page,
  pageSize: orderListData.pagination.pageSize,
})

const fetchOrderList = async () => {
  loading.value = true
  try {
    const data = await loadOrderList(buildQuery())
    orderListData.list = data.list
    orderListData.pagination.total = data.total
    orderListData.pagination.totalPages = data.totalPages
    statusTabs.value = data.tabs || []
    if (data.activeTab) {
      orderListData.activeTab = data.activeTab
    }
  } finally {
    loading.value = false
  }
}

const onPageSizeChange = () => {
  orderListData.pagination.page = 1
  fetchOrderList()
}

const onTabChange = () => {
  orderListData.pagination.page = 1
  fetchOrderList()
}

const handleSearch = () => {
  orderListData.pagination.page = 1
  fetchOrderList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    product: '',
    orderId: '',
    logisticsNo: '',
    phone: '',
    timeType: 'create',
    dateRange: null,
  })
  orderListData.pagination.page = 1
  fetchOrderList()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
  selectAll.value = rows.length === orderListData.list.length && rows.length > 0
}

const handleSelectAll = () => {
  tableRef.value?.toggleAllSelection()
}

const handleView = (row) => {
  router.push({ path: `/order/detail/${row.id}` })
}

const openExpressPrint = (row) => {
  const target = row || selectedRows.value[0]
  if (!target) {
    ElMessage.warning('请先选择订单')
    return
  }
  if (target.orderStatus === 'pending_payment') {
    ElMessage.warning('待付款订单无法打印快递单')
    return
  }
  expressDialogRef.value?.open(target.id)
}

const handleShip = (row) => {
  ElMessageBox.confirm(`确定对订单 ${row.id} 发货吗？`, '发货确认', { type: 'info' })
    .then(async () => {
      await shipOrder(row.id)
      ElMessage.success('发货成功')
      fetchOrderList()
      expressDialogRef.value?.open(row.id)
    })
    .catch(() => {})
}

const handleRefund = (row) => {
  ElMessageBox.confirm(`确定对订单 ${row.id} 发起退款吗？`, '退款确认', { type: 'warning' })
    .then(async () => {
      await refundOrder(row.id)
      ElMessage.success('退款申请已提交')
      fetchOrderList()
    })
    .catch(() => {})
}

const handleConfirm = (row) => {
  ElMessageBox.confirm(`确定对订单 ${row.id} 确认收货吗？`, '确认收货', { type: 'info' })
    .then(async () => {
      await confirmOrder(row.id)
      ElMessage.success('已确认收货')
      fetchOrderList()
    })
    .catch(() => {})
}

const handleBatchConfirm = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择订单')
    return
  }
  const ids = selectedRows.value.map((r) => r.id)
  ElMessageBox.confirm(`确定批量确认 ${ids.length} 个订单收货吗？`, '批量确认收货', { type: 'info' })
    .then(async () => {
      await batchConfirmOrders(ids)
      ElMessage.success('批量确认收货成功')
      fetchOrderList()
    })
    .catch(() => {})
}

onMounted(fetchOrderList)
</script>

<style scoped>
.order-panel {
  min-height: calc(100vh - 120px);
}

.panel-card {
  margin-bottom: 12px;
  border-radius: 8px;
  border: none;
}

.panel-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.search-actions {
  margin-left: auto;
}

.batch-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.batch-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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
  line-height: 1.4;
}

.amount-cell,
.receiver-cell,
.supplier-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: #606266;
}

.discount {
  color: #909399;
}

.address,
.sub {
  color: #909399;
  font-size: 11px;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.page-info {
  font-size: 13px;
  color: #606266;
}
</style>
