<template>
  <div v-loading="loading" class="promo-page">
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="优惠券">
          <el-input v-model="searchForm.keyword" placeholder="请输入名称或编号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择优惠券分类" clearable style="width: 160px">
            <el-option v-for="opt in couponTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
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
        <el-tabs v-model="activeTab" @tab-change="fetchCouponList">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane label="待开始" name="pending" />
          <el-tab-pane label="进行中" name="active" />
          <el-tab-pane label="已结束" name="ended" />
        </el-tabs>
        <el-button type="primary">+ 添加优惠券</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card table-card">
      <el-table :data="tableData" border stripe>
        <el-table-column type="selection" width="48" align="center" fixed="left" />
        <el-table-column prop="id" label="活动编号" width="90" align="center" fixed="left" />
        <el-table-column prop="name" label="优惠券名称" width="130" fixed="left" />
        <el-table-column prop="typeLabel" label="类型" width="90" />
        <el-table-column prop="products" label="可使用商品" width="100" />
        <el-table-column prop="threshold" label="使用门槛" width="110" />
        <el-table-column label="面值" width="80" align="center">
          <template #default="{ row }">{{ row.faceValue.toFixed(2) }}元</template>
        </el-table-column>
        <el-table-column prop="issueQty" label="发行量" width="80" align="center" />
        <el-table-column prop="claimed" label="领取量" width="80" align="center" />
        <el-table-column prop="used" label="使用量" width="80" align="center" />
        <el-table-column prop="platform" label="适用平台" width="90" />
        <el-table-column prop="validity" label="有效期" width="80" />
        <el-table-column prop="timeRange" label="开始结束时间" width="150" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <span :class="row.status === 'ended' ? 'text-danger' : ''">{{ row.statusLabel }}</span>
          </template>
        </el-table-column>
        <el-table-column label="上线/下架" width="90" align="center">
          <template #default="{ row }">
            <el-button v-if="row.online" type="danger" link size="small" @click="row.online = false">下架</el-button>
            <el-button v-else type="success" link size="small" @click="row.online = true">上线</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row)">明细</el-button>
            <el-button type="success" link>编辑</el-button>
            <el-button type="danger" link>删除</el-button>
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
import { ElMessage } from 'element-plus'
import { mockCouponList, couponTypeOptions } from '@/mock/promotion'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const searchForm = reactive({ keyword: '', type: '' })
const pagination = reactive({ page: 1, total: 265, totalPages: 10 })

/** GET /api/promotion/coupon/list */
const fetchCouponList = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 400))
    tableData.value = [...mockCouponList]
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { fetchCouponList(); ElMessage.success('查询成功') }
const handleReset = () => { searchForm.keyword = ''; searchForm.type = ''; fetchCouponList() }
const goDetail = (row) => router.push(`/promotion/coupon/${row.id}/detail`)

onMounted(fetchCouponList)
</script>

<style scoped>
.promo-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.table-card { overflow-x: auto; }
.search-form { display: flex; flex-wrap: wrap; }
.search-actions { margin-left: auto; }
.toolbar { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
.toolbar :deep(.el-tabs__header) { margin-bottom: 0; flex: 1; }
.text-danger { color: #f56c6c; }
.pagination-bar { display: flex; justify-content: space-between; margin-top: 16px; font-size: 13px; color: #606266; }
</style>
