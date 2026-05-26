<template>
  <div v-loading="loading" class="growth-page">
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="账号">
          <el-input v-model="searchForm.account" placeholder="请输入手机号或ID" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="searchForm.nickname" placeholder="请输入昵称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <div class="action-bar">
        <el-button type="primary" size="small" @click="openRules">规则设置</el-button>
        <el-button type="primary" size="small" @click="openRewards">奖励设置</el-button>
        <el-button type="primary" size="small" @click="openBatch">批量设置</el-button>
        <el-button type="primary" size="small" @click="openLedger('growth')">成长值明细</el-button>
        <el-button type="primary" size="small" @click="openLedger('points')">积分明细</el-button>
        <el-button type="primary" size="small" @click="handleExport">导出数据</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="普通会员" name="normal" />
        <el-tab-pane label="黄金会员" name="gold" />
        <el-tab-pane label="铂金会员" name="platinum" />
        <el-tab-pane label="钻石会员" name="diamond" />
      </el-tabs>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table :data="tableData" border stripe @selection-change="(r) => (selected = r)">
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="用户编号" width="100" align="center" />
        <el-table-column prop="nickname" label="用户昵称" width="100" />
        <el-table-column prop="account" label="用户账号" width="120" />
        <el-table-column prop="level" label="用户等级" width="100" align="center" />
        <el-table-column prop="points" label="可用积分" width="100" align="center" />
        <el-table-column prop="growth" label="成长值" width="100" align="center" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openAdjust(row, 'growth')">成长值</el-button>
            <el-button type="success" link @click="openAdjust(row, 'points')">积分</el-button>
            <el-button type="danger" link @click="openAdjust(row, 'settings')">设置</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          layout="prev, pager, next, sizes"
          background
          @current-change="fetchGrowthList"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="adjustDialog.visible" :title="adjustDialog.title" width="420px">
      <el-form v-if="adjustDialog.type !== 'settings'" label-width="80px">
        <el-form-item label="调整值">
          <el-input-number v-model="adjustDialog.value" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="adjustDialog.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <el-form v-else label-width="90px">
        <el-form-item label="会员等级">
          <el-select v-model="adjustDialog.level" style="width: 100%">
            <el-option v-for="opt in levelOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分">
          <el-input-number v-model="adjustDialog.points" :min="0" />
        </el-form-item>
        <el-form-item label="成长值">
          <el-input-number v-model="adjustDialog.growth" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="adjustDialog.saving" @click="updateUserGrowthOrScore">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rulesDialog.visible" title="规则设置（任务 + 消费规则）" width="720px">
      <el-divider content-position="left">任务设置</el-divider>
      <el-table :data="rulesDialog.tasks" border size="small">
        <el-table-column prop="taskName" label="任务" />
        <el-table-column label="成长值" width="100">
          <template #default="{ row }"><el-input-number v-model="row.growthReward" :min="0" size="small" /></template>
        </el-table-column>
        <el-table-column label="积分" width="100">
          <template #default="{ row }"><el-input-number v-model="row.pointsReward" :min="0" size="small" /></template>
        </el-table-column>
        <el-table-column label="启用" width="80">
          <template #default="{ row }"><el-switch v-model="row.enabled" /></template>
        </el-table-column>
      </el-table>
      <el-divider content-position="left">消费/行为规则</el-divider>
      <el-form :model="rulesDialog.rules" label-width="140px" class="rules-form">
        <el-form-item label="每元订单成长值"><el-input-number v-model="rulesDialog.rules.orderGrowthPerYuan" :min="0" /></el-form-item>
        <el-form-item label="每元订单积分"><el-input-number v-model="rulesDialog.rules.orderPointsPerYuan" :min="0" /></el-form-item>
        <el-form-item label="签到成长值"><el-input-number v-model="rulesDialog.rules.signInGrowth" :min="0" /></el-form-item>
        <el-form-item label="签到积分"><el-input-number v-model="rulesDialog.rules.signInPoints" :min="0" /></el-form-item>
        <el-form-item label="评价成长值"><el-input-number v-model="rulesDialog.rules.reviewGrowth" :min="0" /></el-form-item>
        <el-form-item label="评价积分"><el-input-number v-model="rulesDialog.rules.reviewPoints" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rulesDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="rulesDialog.saving" @click="saveRules">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rewardsDialog.visible" title="奖励设置" width="520px">
      <el-form :model="rewardsDialog.data" label-width="120px">
        <el-form-item label="注册奖励">
          <el-switch v-model="rewardsDialog.data.registerEnabled" />
          <el-input-number v-model="rewardsDialog.data.registerGrowth" :min="0" style="margin-left: 8px" /> 成长值
          <el-input-number v-model="rewardsDialog.data.registerPoints" :min="0" style="margin-left: 8px" /> 积分
        </el-form-item>
        <el-form-item label="生日奖励">
          <el-switch v-model="rewardsDialog.data.birthdayEnabled" />
          <el-input-number v-model="rewardsDialog.data.birthdayGrowth" :min="0" style="margin-left: 8px" /> 成长值
          <el-input-number v-model="rewardsDialog.data.birthdayPoints" :min="0" style="margin-left: 8px" /> 积分
        </el-form-item>
        <el-form-item label="邀请奖励">
          <el-switch v-model="rewardsDialog.data.inviteEnabled" />
          <el-input-number v-model="rewardsDialog.data.inviteGrowth" :min="0" style="margin-left: 8px" /> 成长值
          <el-input-number v-model="rewardsDialog.data.invitePoints" :min="0" style="margin-left: 8px" /> 积分
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rewardsDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="rewardsDialog.saving" @click="saveRewards">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchDialog.visible" title="批量设置" width="400px">
      <el-form label-width="90px">
        <el-form-item label="调整类型">
          <el-radio-group v-model="batchDialog.adjustType">
            <el-radio value="growth">成长值</el-radio>
            <el-radio value="points">积分</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="方式">
          <el-radio-group v-model="batchDialog.mode">
            <el-radio value="add">增加</el-radio>
            <el-radio value="set">设为</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数值"><el-input-number v-model="batchDialog.value" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="batchDialog.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="batchDialog.saving" @click="submitBatch">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="ledgerDrawer.visible" :title="ledgerDrawer.title" size="60%">
      <el-table v-loading="ledgerDrawer.loading" :data="ledgerDrawer.list" border stripe>
        <el-table-column prop="userNo" label="用户编号" width="100" />
        <el-table-column prop="nickname" label="昵称" width="90" />
        <el-table-column prop="changeTypeText" label="类型" width="100" />
        <el-table-column prop="beforeQty" label="变更前" width="80" align="center" />
        <el-table-column prop="changeQty" label="变更" width="80" align="center" />
        <el-table-column prop="afterQty" label="变更后" width="80" align="center" />
        <el-table-column prop="remark" label="备注" min-width="120" />
        <el-table-column prop="createdAt" label="时间" width="160" />
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="ledgerDrawer.page"
          :total="ledgerDrawer.total"
          layout="prev, pager, next"
          @current-change="loadLedger"
        />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  adjustGrowth,
  batchAdjustGrowth,
  exportGrowthList,
  fetchGrowthList as getGrowthListApi,
  fetchGrowthRewards,
  fetchGrowthRules,
  fetchGrowthTasks,
  fetchLedger,
  fetchLevelOptions,
  saveGrowthRewards,
  saveGrowthRules,
  saveGrowthTasks,
} from '@/api/user'

const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const selected = ref([])
const levelOptions = ref([])
const searchForm = reactive({ account: '', nickname: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const adjustDialog = reactive({
  visible: false,
  saving: false,
  title: '',
  type: '',
  row: null,
  value: 0,
  remark: '',
  level: '',
  points: 0,
  growth: 0,
})

const rulesDialog = reactive({
  visible: false,
  saving: false,
  tasks: [],
  rules: {
    orderGrowthPerYuan: 1,
    orderPointsPerYuan: 1,
    signInGrowth: 5,
    signInPoints: 5,
    reviewGrowth: 5,
    reviewPoints: 5,
    growthExpireDays: '365',
    pointsExpireDays: '365',
  },
})

const rewardsDialog = reactive({
  visible: false,
  saving: false,
  data: {
    registerEnabled: true,
    registerGrowth: 100,
    registerPoints: 100,
    birthdayEnabled: true,
    birthdayGrowth: 50,
    birthdayPoints: 50,
    inviteEnabled: false,
    inviteGrowth: 20,
    invitePoints: 20,
  },
})

const batchDialog = reactive({
  visible: false,
  saving: false,
  adjustType: 'growth',
  mode: 'add',
  value: 0,
  remark: '',
})

const ledgerDrawer = reactive({
  visible: false,
  loading: false,
  title: '',
  ledgerType: 'growth',
  list: [],
  page: 1,
  total: 0,
})

const fetchGrowthList = async () => {
  loading.value = true
  try {
    const data = await getGrowthListApi({
      account: searchForm.account || undefined,
      nickname: searchForm.nickname || undefined,
      tab: activeTab.value,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = data.list
    pagination.total = data.total
  } finally {
    loading.value = false
  }
}

const onTabChange = () => {
  pagination.page = 1
  fetchGrowthList()
}

const onSizeChange = () => {
  pagination.page = 1
  fetchGrowthList()
}

const handleSearch = () => {
  pagination.page = 1
  fetchGrowthList()
  ElMessage.success('查询成功')
}

const handleReset = () => {
  searchForm.account = ''
  searchForm.nickname = ''
  pagination.page = 1
  fetchGrowthList()
}

const openAdjust = (row, type) => {
  adjustDialog.row = row
  adjustDialog.type = type
  adjustDialog.title = type === 'growth' ? '调整成长值' : type === 'points' ? '调整积分' : '用户设置'
  adjustDialog.value = type === 'growth' ? row.growth : type === 'points' ? row.points : 0
  adjustDialog.remark = ''
  if (type === 'settings') {
    adjustDialog.level = row.levelKey
    adjustDialog.points = row.points
    adjustDialog.growth = row.growth
  }
  adjustDialog.visible = true
}

const updateUserGrowthOrScore = async () => {
  adjustDialog.saving = true
  try {
    const payload = {
      userId: adjustDialog.row.id,
      type: adjustDialog.type,
    }
    if (adjustDialog.type === 'settings') {
      Object.assign(payload, {
        level: adjustDialog.level,
        points: adjustDialog.points,
        growth: adjustDialog.growth,
        value: 0,
      })
    } else {
      Object.assign(payload, { value: adjustDialog.value, remark: adjustDialog.remark })
    }
    await adjustGrowth(payload)
    ElMessage.success('调整成功')
    adjustDialog.visible = false
    fetchGrowthList()
  } finally {
    adjustDialog.saving = false
  }
}

const openRules = async () => {
  rulesDialog.tasks = await fetchGrowthTasks()
  rulesDialog.rules = await fetchGrowthRules()
  rulesDialog.visible = true
}

const saveRules = async () => {
  rulesDialog.saving = true
  try {
    await Promise.all([saveGrowthTasks(rulesDialog.tasks), saveGrowthRules(rulesDialog.rules)])
    ElMessage.success('规则已保存')
    rulesDialog.visible = false
  } finally {
    rulesDialog.saving = false
  }
}

const openRewards = async () => {
  rewardsDialog.data = await fetchGrowthRewards()
  rewardsDialog.visible = true
}

const saveRewards = async () => {
  rewardsDialog.saving = true
  try {
    await saveGrowthRewards(rewardsDialog.data)
    ElMessage.success('奖励设置已保存')
    rewardsDialog.visible = false
  } finally {
    rewardsDialog.saving = false
  }
}

const openBatch = () => {
  if (!selected.value.length) {
    ElMessage.warning('请先勾选用户')
    return
  }
  batchDialog.visible = true
}

const submitBatch = async () => {
  batchDialog.saving = true
  try {
    await batchAdjustGrowth({
      userIds: selected.value.map((r) => r.id),
      adjustType: batchDialog.adjustType,
      mode: batchDialog.mode,
      value: batchDialog.value,
      remark: batchDialog.remark,
    })
    ElMessage.success('批量调整成功')
    batchDialog.visible = false
    fetchGrowthList()
  } finally {
    batchDialog.saving = false
  }
}

const openLedger = (type) => {
  ledgerDrawer.ledgerType = type
  ledgerDrawer.title = type === 'growth' ? '成长值明细' : '积分明细'
  ledgerDrawer.page = 1
  ledgerDrawer.visible = true
  loadLedger()
}

const loadLedger = async () => {
  ledgerDrawer.loading = true
  try {
    const data = await fetchLedger({
      ledgerType: ledgerDrawer.ledgerType,
      page: ledgerDrawer.page,
      pageSize: 10,
    })
    ledgerDrawer.list = data.list
    ledgerDrawer.total = data.total
  } finally {
    ledgerDrawer.loading = false
  }
}

const handleExport = async () => {
  try {
    await exportGrowthList({
      account: searchForm.account || '',
      tab: activeTab.value,
    })
    ElMessage.success('导出成功')
  } catch {
    /* handled */
  }
}

onMounted(async () => {
  levelOptions.value = await fetchLevelOptions()
  fetchGrowthList()
})
</script>

<style scoped>
.growth-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.search-actions { margin-left: auto; }
.action-bar { display: flex; flex-wrap: wrap; gap: 8px; justify-content: flex-end; }
.pagination-bar { margin-top: 16px; display: flex; justify-content: flex-end; }
.rules-form { margin-top: 12px; }
</style>
