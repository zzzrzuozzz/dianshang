<template>
  <div v-loading="loading" class="promo-page">
    <el-card shadow="never" class="panel-card">
      <div class="section-header">
        <span class="section-title">设置商品 - 活动 {{ activityId }}</span>
        <el-button @click="router.back()">返回</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-empty v-if="!loading && tableData.length === 0" description="该活动尚未配置团购时段">
        <el-button type="primary" @click="router.push(`/promotion/group-buy/${activityId}/time`)">去添加时间段</el-button>
      </el-empty>
      <el-table v-else :data="tableData" border stripe>
        <el-table-column prop="id" label="编号" width="100" align="center" />
        <el-table-column prop="name" label="团购时段名称" min-width="140" />
        <el-table-column prop="start" label="每日开始时间" width="130" align="center" />
        <el-table-column prop="end" label="每日结束时间" width="130" align="center" />
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="goSku(row)">商品列表</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchGroupBuyTimeList } from '@/api/promotion'

const route = useRoute()
const router = useRouter()
const activityId = route.params.activityId
const loading = ref(false)
const tableData = ref([])

const fetchSlots = async () => {
  loading.value = true
  try {
    tableData.value = await fetchGroupBuyTimeList(activityId)
  } finally {
    loading.value = false
  }
}

const goSku = (row) => {
  router.push(`/promotion/group-buy/${activityId}/sku/${row.id}?timeName=${encodeURIComponent(row.name)}`)
}

onMounted(fetchSlots)
</script>

<style scoped>
.promo-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.section-header { display: flex; justify-content: space-between; align-items: center; }
.section-title::before { content: ''; display: inline-block; width: 4px; height: 14px; margin-right: 8px; background: #409eff; border-radius: 2px; vertical-align: middle; }
</style>
