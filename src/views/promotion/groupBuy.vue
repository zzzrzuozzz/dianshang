<template>
  <div v-loading="loading" class="promo-page">
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="活动">
          <el-input v-model="searchForm.keyword" placeholder="请输入名称或编号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" value-format="YYYY-MM-DD" style="width: 280px" />
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <div class="toolbar">
        <el-tabs v-model="activeTab" @tab-change="fetchGroupBuyList">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane label="待开始" name="pending" />
          <el-tab-pane label="进行中" name="active" />
          <el-tab-pane label="已下架" name="offline" />
          <el-tab-pane label="已结束" name="ended" />
        </el-tabs>
        <el-button type="primary">+ 添加活动</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table :data="tableData" border stripe>
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="团购编号" width="100" align="center" />
        <el-table-column prop="title" label="活动标题" min-width="180" />
        <el-table-column label="活动状态" width="90" align="center">
          <template #default="{ row }">
            <span :class="row.status === 'offline' || row.status === 'ended' ? 'text-danger' : row.status === 'pending' ? 'text-primary' : ''">
              {{ seckillStatusMap[row.status]?.label }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="150" />
        <el-table-column prop="endTime" label="结束时间" width="150" />
        <el-table-column label="预警通知" width="160">
          <template #default="{ row }">
            <span v-if="row.warning" class="warning-text">{{ row.warning }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="上线/下架" width="100" align="center">
          <template #default="{ row }">
            <el-button v-if="row.online" type="danger" link @click="row.online = false">下架</el-button>
            <el-button v-else type="success" link @click="row.online = true">上线</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link>编辑</el-button>
            <el-button type="success" link>设置商品</el-button>
            <el-button type="danger" link>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination :total="265" layout="prev, pager, next, sizes" background />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { mockGroupBuyList, seckillStatusMap } from '@/mock/promotion'

const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const searchForm = reactive({ keyword: '', dateRange: [] })

/**
 * GET /api/promotion/group-buy/list
 */
const fetchGroupBuyList = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 400))
    let list = [...mockGroupBuyList]
    if (activeTab.value !== 'all') list = list.filter((i) => i.status === activeTab.value)
    tableData.value = list
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { fetchGroupBuyList(); ElMessage.success('查询成功') }
const handleReset = () => { searchForm.keyword = ''; searchForm.dateRange = []; fetchGroupBuyList() }

onMounted(fetchGroupBuyList)
</script>

<style scoped>
.promo-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.search-form { display: flex; flex-wrap: wrap; }
.search-actions { margin-left: auto; }
.toolbar { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
.toolbar :deep(.el-tabs__header) { margin-bottom: 0; }
.warning-text, .text-danger { color: #f56c6c; }
.text-primary { color: #409eff; }
.pagination-bar { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
