<template>
  <div v-loading="loading" class="withdraw-page">
    <el-card shadow="never" class="toolbar-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部申请" name="all" />
        <el-tab-pane label="待审核" name="pending" />
        <el-tab-pane label="转账中" name="transferring" />
        <el-tab-pane label="已完结" name="done" />
      </el-tabs>
      <el-form :inline="true" class="search-inline">
        <el-form-item label="申请编号">
          <el-input v-model="keyword" placeholder="申请编号" clearable style="width: 200px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="applyNo" label="申请编号" width="190" />
        <el-table-column label="会员/商户" min-width="200">
          <template #default="{ row }">
            <div class="member-cell">
              <el-avatar :size="36" :src="row.avatar" />
              <div>
                <p class="member-name">{{ row.nickname || row.userNo }}</p>
                <p class="member-shop">{{ row.shopName }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="applyAmount" label="申请金额" width="110" align="right" />
        <el-table-column prop="feeAmount" label="手续费" width="90" align="right" />
        <el-table-column prop="actualAmount" label="实际到账" width="110" align="right" />
        <el-table-column label="提现账户" min-width="220">
          <template #default="{ row }">
            <div class="account-cell">
              <el-tag size="small" effect="plain">{{ row.accountType === 'ALIPAY' ? '支付宝' : '银行卡' }}</el-tag>
              <span>{{ row.holderName }} · {{ row.accountNo }}</span>
              <p v-if="row.bankName" class="bank-name">{{ row.bankName }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.verifyStatus)" size="small">{{ row.verifyStatusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.verifyStatus === 0"
              type="primary"
              link
              @click="openVerify(row)"
            >
              审核办理
            </el-button>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, prev, pager, next, sizes"
          background
          @current-change="fetchData"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

  <!-- 后端 @Transactional + 幂等：仅 verify_status=0 可审批，防止重复打款 -->
    <el-dialog v-model="verifyVisible" title="提现审核办理" width="520px" destroy-on-close>
      <div v-if="currentRow" class="verify-body">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="申请编号">{{ currentRow.applyNo }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentRow.nickname }}（{{ currentRow.shopName }}）</el-descriptions-item>
          <el-descriptions-item label="申请金额">¥{{ currentRow.applyAmount }}</el-descriptions-item>
          <el-descriptions-item label="实际到账">¥{{ currentRow.actualAmount }}</el-descriptions-item>
          <el-descriptions-item label="收款账户">
            {{ currentRow.holderName }} / {{ currentRow.accountNo }} / {{ currentRow.bankName }}
          </el-descriptions-item>
        </el-descriptions>
        <el-form class="verify-form">
          <el-form-item label="审批结果">
            <el-radio-group v-model="verifyForm.passed">
              <el-radio :value="true">通过</el-radio>
              <el-radio :value="false">拒绝</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="!verifyForm.passed" label="驳回原因" required>
            <el-input
              v-model="verifyForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请填写驳回原因/备注"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="verifyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitVerify">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onActivated } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchWithdrawPage, verifyWithdraw } from '@/api/finance'

const route = useRoute()

const loading = ref(false)
const submitting = ref(false)
const activeTab = ref('all')
const keyword = ref('')
const tableData = ref([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const verifyVisible = ref(false)
const currentRow = ref(null)
const verifyForm = reactive({ passed: true, remark: '' })

const fetchData = async () => {
  loading.value = true
  try {
    const data = await fetchWithdrawPage({
      tab: activeTab.value,
      keyword: keyword.value || undefined,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = data.list || []
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  pagination.page = 1
  fetchData()
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const onSizeChange = () => {
  pagination.page = 1
  fetchData()
}

const statusTagType = (status) => {
  if (status === 0) return 'warning'
  if (status === 1) return 'primary'
  if (status === 3) return 'success'
  if (status === 2 || status === 4) return 'danger'
  return 'info'
}

const openVerify = (row) => {
  currentRow.value = row
  verifyForm.passed = true
  verifyForm.remark = ''
  verifyVisible.value = true
}

const submitVerify = async () => {
  if (!verifyForm.passed && !verifyForm.remark.trim()) {
    ElMessage.warning('驳回时必须填写原因')
    return
  }
  submitting.value = true
  try {
    await verifyWithdraw({
      applyNo: currentRow.value.applyNo,
      passed: verifyForm.passed,
      remark: verifyForm.remark || undefined,
    })
    ElMessage.success(verifyForm.passed ? '审核通过，已记录提现流水' : '已驳回')
    verifyVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

const applyRouteApplyNo = () => {
  const no = route.query.applyNo
  if (typeof no === 'string' && no) {
    keyword.value = no
    activeTab.value = 'pending'
  }
}

onMounted(() => {
  applyRouteApplyNo()
  fetchData()
})

onActivated(() => {
  applyRouteApplyNo()
  fetchData()
})
</script>

<style scoped>
.withdraw-page { display: flex; flex-direction: column; gap: 12px; }
.toolbar-card, .table-card { border: none; border-radius: 8px; }
.search-inline { margin-top: 8px; }
.member-cell { display: flex; align-items: center; gap: 10px; }
.member-name { margin: 0; font-size: 14px; color: #303133; }
.member-shop { margin: 2px 0 0; font-size: 12px; color: #909399; }
.account-cell { font-size: 13px; line-height: 1.5; }
.account-cell .bank-name { margin: 4px 0 0; font-size: 12px; color: #909399; }
.pagination-bar { display: flex; justify-content: flex-end; margin-top: 16px; }
.verify-body { display: flex; flex-direction: column; gap: 16px; }
.verify-form { margin-top: 8px; }
.muted { color: #c0c4cc; }
</style>
