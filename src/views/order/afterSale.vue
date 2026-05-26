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
      <el-tabs v-model="activeTab" @tab-change="fetchAfterSaleList">
        <el-tab-pane
          v-for="tab in afterSaleStatusTabs"
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
            <el-tag size="small" type="warning">{{ afterSaleStatusLabel[row.afterSaleStatus] }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="售后类型" width="100" align="center">
          <template #default="{ row }">
            {{ afterSaleTypeLabel[row.afterSaleType] || row.afterSaleType }}
          </template>
        </el-table-column>

        <el-table-column prop="refundAmount" label="退款金额" width="90" align="center">
          <template #default="{ row }">
            {{ row.refundAmount.toFixed(2) }}
          </template>
        </el-table-column>

        <el-table-column prop="applyTime" label="申请时间" width="140" />
        <el-table-column prop="processTime" label="处理时间" width="140">
          <template #default="{ row }">
            {{ row.processTime || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
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
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  afterSaleStatusTabs,
  orderStatusLabel,
  shipStatusLabel,
  afterSaleTypeLabel,
  mockAfterSaleList,
} from '@/mock/order'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('platform_pending')
const tableData = ref([])

const afterSaleStatusLabel = {
  platform_pending: '待平台处理',
  user_pending: '待用户处理',
  platform_confirm: '待平台确认收货',
  completed: '已完成',
  rejected: '已拒绝',
  closed: '已关闭',
}

const searchForm = reactive({
  product: '',
  orderId: '',
  logisticsNo: '',
  afterSaleId: '',
  timeType: 'create',
  dateRange: ['2024-08-02', '2024-08-23'],
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 265,
  totalPages: 10,
})

/**
 * 获取售后列表
 * 此处后续对接 Spring Boot 后端 /api/order/after-sale/list 接口
 */
const fetchAfterSaleList = async () => {
  loading.value = true
  try {
    await new Promise((resolve) => setTimeout(resolve, 400))
    tableData.value = [...mockAfterSaleList]
    pagination.total = 265
    pagination.totalPages = Math.ceil(265 / pagination.pageSize)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchAfterSaleList()
  ElMessage.success('查询成功')
}

const handleReset = () => {
  Object.assign(searchForm, {
    product: '',
    orderId: '',
    logisticsNo: '',
    afterSaleId: '',
    timeType: 'create',
    dateRange: ['2024-08-02', '2024-08-23'],
  })
  fetchAfterSaleList()
}

const handleView = (row) => {
  router.push({ path: `/order/detail/${row.orderId}`, query: { status: 'after_sale' } })
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
