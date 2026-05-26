<template>
  <div class="invite-list-page">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true">
        <el-form-item label="使用状态">
          <el-select v-model="query.used" clearable placeholder="全部" style="width: 140px" @change="handleSearch">
            <el-option label="未使用" :value="0" />
            <el-option label="已使用" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="router.push('/permission/invite/generate')">去生成</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-loading="loading" shadow="never">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="code" label="邀请码" width="160">
          <template #default="{ row }">
            <span class="mono">{{ row.code }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="roleName" label="绑定角色" width="140" />
        <el-table-column label="使用状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.used === 1 ? 'info' : 'success'" size="small">{{ row.usedLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usedByUsername" label="注册账号" width="130">
          <template #default="{ row }">
            {{ row.usedByUsername || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="注册手机/邮箱" min-width="180">
          <template #default="{ row }">
            <span v-if="row.usedAccount">{{ row.usedAccount }} ({{ row.usedAccountType === 'PHONE' ? '手机' : '邮箱' }})</span>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" width="170" />
        <el-table-column prop="usedTime" label="使用时间" width="170">
          <template #default="{ row }">{{ row.usedTime || '—' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          layout="total, prev, pager, next"
          background
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { fetchInviteCodePage } from '@/api/invite'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])

const query = reactive({
  used: undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const fetchData = async () => {
  loading.value = true
  try {
    const data = await fetchInviteCodePage({
      used: query.used ?? undefined,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
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

onMounted(fetchData)
onActivated(fetchData)
</script>

<style scoped>
.invite-list-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card :deep(.el-card__body) {
  padding-bottom: 0;
}

.mono {
  font-family: ui-monospace, monospace;
  letter-spacing: 0.04em;
}

.muted {
  color: #909399;
}

.pagination-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
