<template>
  <div v-loading="loading" class="statement-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="流水/订单号">
          <el-input v-model="query.keyword" placeholder="财务流水号或订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="交易类型">
          <el-select v-model="query.tradeType" placeholder="全部" clearable style="width: 140px">
            <el-option label="订单收入" value="ORDER_IN" />
            <el-option label="退款支出" value="REFUND_OUT" />
            <el-option label="提现" value="WITHDRAW" />
            <el-option label="平台手续费" value="COMMISSION" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-select v-model="query.paymentChannel" placeholder="全部" clearable style="width: 120px">
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="余额" value="BALANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="记账时间">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="primary" :icon="Download" :loading="exporting" @click="handleExportExcel">
            导出对账单
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <!-- 后端须走 idx_create_time 范围索引；导出为流式 CSV，避免 SELECT * OOM -->
      <el-table :data="tableData" border stripe>
        <el-table-column prop="recordNo" label="流水号" width="200" />
        <el-table-column prop="orderNo" label="关联订单号" width="140" />
        <el-table-column label="交易类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="tradeTagType(row.tradeType)" effect="plain" size="small">
              {{ row.tradeTypeLabel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动金额" width="120" align="right">
          <template #default="{ row }">
            <span :class="amountClass(row)">{{ row.amountDisplay }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentChannelLabel" label="支付渠道" width="100" align="center" />
        <el-table-column label="记账状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'info'" size="small">
              {{ row.statusLabel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, prev, pager, next, sizes"
          background
          @current-change="fetchData"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onActivated } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { exportFinanceStatement, fetchStatementPage } from '@/api/finance'

const route = useRoute()

const loading = ref(false)
const exporting = ref(false)
const tableData = ref([])
const timeRange = ref(null)

const query = reactive({
  keyword: '',
  tradeType: '',
  paymentChannel: '',
})

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const buildParams = () => ({
  keyword: query.keyword || undefined,
  tradeType: query.tradeType || undefined,
  paymentChannel: query.paymentChannel || undefined,
  startTime: timeRange.value?.[0],
  endTime: timeRange.value?.[1],
  page: pagination.page,
  pageSize: pagination.pageSize,
})

const fetchData = async () => {
  loading.value = true
  try {
    const data = await fetchStatementPage(buildParams())
    tableData.value = data.list || []
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  query.keyword = ''
  query.tradeType = ''
  query.paymentChannel = ''
  timeRange.value = null
  pagination.page = 1
  fetchData()
}

const onSizeChange = () => {
  pagination.page = 1
  fetchData()
}

const handleExportExcel = async () => {
  exporting.value = true
  try {
    await exportFinanceStatement({
      keyword: query.keyword || undefined,
      tradeType: query.tradeType || undefined,
      paymentChannel: query.paymentChannel || undefined,
      startTime: timeRange.value?.[0],
      endTime: timeRange.value?.[1],
    })
    ElMessage.success('对账单导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const tradeTagType = (type) => {
  if (type === 'ORDER_IN') return 'success'
  if (type === 'REFUND_OUT') return 'danger'
  return 'info'
}

const amountClass = (row) => (row.amount && Number(row.amount) < 0 ? 'amount-out' : 'amount-in')

const applyRouteKeyword = () => {
  const kw = route.query.keyword
  if (typeof kw === 'string' && kw) {
    query.keyword = kw
  }
}

onMounted(() => {
  applyRouteKeyword()
  fetchData()
})

onActivated(() => {
  applyRouteKeyword()
  fetchData()
})
</script>

<style scoped>
.statement-page { display: flex; flex-direction: column; gap: 12px; }
.search-card, .table-card { border: none; border-radius: 8px; }
.search-form { margin-bottom: 0; }
.pagination-bar { display: flex; justify-content: flex-end; margin-top: 16px; }
.amount-in { color: #67c23a; font-weight: 600; }
.amount-out { color: #f56c6c; font-weight: 600; }
</style>
