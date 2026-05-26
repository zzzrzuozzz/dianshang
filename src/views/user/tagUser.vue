<template>
  <div v-loading="loading" class="tag-user-page">
    <el-card shadow="never" class="panel-card">
      <template #header>
        <span class="section-title">标签人数 - {{ tagName }}</span>
      </template>
      <el-table :data="tableData" border stripe>
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="用户编号" width="100" align="center" />
        <el-table-column prop="nickname" label="用户昵称" width="100" />
        <el-table-column prop="account" label="用户账号" width="120" />
        <el-table-column prop="level" label="用户等级" width="100" align="center" />
        <el-table-column label="消费金额" width="100" align="center">
          <template #default="{ row }">{{ Number(row.consumeAmount).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数量" width="90" align="center" />
        <el-table-column prop="points" label="可用积分" width="90" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.status !== 'normal' ? 'status-danger' : ''">{{ row.statusText }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="100" />
        <el-table-column prop="registerTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row)">查看</el-button>
            <el-button size="small" class="btn-red" @click="removeFromTag(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <span>第{{ pagination.page }}页 共{{ pagination.totalPages }}页 {{ pagination.total }}条</span>
        <el-pagination
          v-model:current-page="pagination.page"
          :total="pagination.total"
          layout="prev, pager, next, sizes"
          background
          @current-change="fetchTagUsers"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchTagUsers as getTagUsersApi, removeTagUser } from '@/api/user'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0, totalPages: 0 })

const tagName = computed(() => route.query.tagName || '标签')
const tagId = computed(() => String(route.query.tagId || ''))

const fetchTagUsers = async () => {
  if (!tagId.value) return
  loading.value = true
  try {
    const data = await getTagUsersApi({
      tagId: tagId.value,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = data.list
    pagination.total = data.total
    pagination.totalPages = data.totalPages
  } finally {
    loading.value = false
  }
}

const goDetail = (row) => router.push(`/user/detail/${row.id}`)
const removeFromTag = (row) => {
  ElMessageBox.confirm(`确定将 ${row.nickname} 移出当前标签吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await removeTagUser(tagId.value, row.id)
      ElMessage.success('已移出标签')
      fetchTagUsers()
    })
    .catch(() => {})
}

onMounted(fetchTagUsers)
</script>

<style scoped>
.tag-user-page { min-height: calc(100vh - 120px); }
.panel-card { border-radius: 8px; border: none; }
.section-title::before { content: ''; display: inline-block; width: 4px; height: 14px; margin-right: 8px; background: #409eff; border-radius: 2px; vertical-align: middle; }
.status-danger { color: #f56c6c; }
.btn-red { color: #f56c6c; border-color: #fbc4c4; background: #fef0f0; }
.pagination-bar { display: flex; justify-content: space-between; margin-top: 16px; font-size: 13px; color: #606266; }
</style>
