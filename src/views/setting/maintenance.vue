<template>
  <div class="maintenance-page">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card shadow="hover" class="ops-card">
          <div class="ops-card__icon reconcile">
            <el-icon :size="28"><Coin /></el-icon>
          </div>
          <h3>全库财务数据对账</h3>
          <p class="ops-desc">
            调用后端清洗服务，将 fin_transaction_record 与首页 dashboard_daily_metric、订单状态强力同步，修复数据不一致。
          </p>
          <el-button
            type="primary"
            :loading="reconcileLoading"
            @click="handleReconcileGlobal"
          >
            执行全库对账
          </el-button>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="8">
        <el-card shadow="hover" class="ops-card">
          <div class="ops-card__icon cache">
            <el-icon :size="28"><Delete /></el-icon>
          </div>
          <h3>清除全局监控缓存</h3>
          <p class="ops-desc">清除系统配置 JVM 内存缓存，强制下次读取数据库最新平台参数。</p>
          <el-button type="warning" :loading="cacheLoading" @click="handleClearCache">
            清除缓存
          </el-button>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="8">
        <el-card shadow="hover" class="ops-card">
          <div class="ops-card__icon log">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <h3>启动种子数据日志</h3>
          <p class="ops-desc">各 CommandLineRunner 初始化脚本在当前环境的健康检查结果。</p>
          <el-button :loading="logLoading" @click="loadInitializerLog">刷新状态</el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="log-card">
      <template #header>
        <span>初始化脚本健康状态</span>
        <span v-if="status?.cacheClearedAt" class="cache-time">最近清缓存：{{ status.cacheClearedAt }}</span>
      </template>
      <el-input
        v-model="logText"
        type="textarea"
        :rows="14"
        readonly
        placeholder="点击「刷新状态」加载 AdminUserInitializer、ProductDataInitializer 等执行结果…"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElNotification } from 'element-plus'
import { Coin, Delete, Document } from '@element-plus/icons-vue'
import { reconcileFinance } from '@/api/finance'
import { clearSystemCache, fetchInitializerStatus } from '@/api/system'
import { refreshPlatformConfig } from '@/composables/usePlatformConfig'

const reconcileLoading = ref(false)
const cacheLoading = ref(false)
const logLoading = ref(false)
const logText = ref('')
const status = ref(null)

/** 全库对账：POST /api/finance/reconcile */
const handleReconcileGlobal = async () => {
  reconcileLoading.value = true
  try {
    const data = await reconcileFinance()
    ElNotification({
      title: '对账成功',
      message: `全库对账清洗成功，首页指标已同步。流水 ${data?.transactionCount ?? '-'} 条，待审提现 ${data?.pendingWithdrawCount ?? '-'} 笔。`,
      type: 'success',
      duration: 6000,
    })
    await refreshPlatformConfig()
    await loadInitializerLog()
  } finally {
    reconcileLoading.value = false
  }
}

const handleClearCache = async () => {
  cacheLoading.value = true
  try {
    const res = await clearSystemCache()
    await refreshPlatformConfig()
    ElNotification({
      title: '缓存已清除',
      message: (res?.message || '全局业务缓存已清除') + '，平台配置已从数据库重新加载',
      type: 'success',
    })
    await loadInitializerLog()
  } finally {
    cacheLoading.value = false
  }
}

const loadInitializerLog = async () => {
  logLoading.value = true
  try {
    status.value = await fetchInitializerStatus()
    const lines = (status.value?.initializers || []).map(
      (item) => `[${item.status}] ${item.name} — ${item.detail}`,
    )
    logText.value = [
      `服务器时间戳: ${status.value?.serverTimeMs ?? '-'}`,
      `最近清缓存: ${status.value?.cacheClearedAt ?? '-'}`,
      '---',
      ...lines,
    ].join('\n')
  } finally {
    logLoading.value = false
  }
}

onMounted(loadInitializerLog)
</script>

<style scoped>
.maintenance-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.ops-card {
  text-align: center;
  min-height: 260px;
  margin-bottom: 16px;
}
.ops-card__icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.ops-card__icon.reconcile {
  background: linear-gradient(135deg, #409eff, #337ecc);
}
.ops-card__icon.cache {
  background: linear-gradient(135deg, #e6a23c, #cf9236);
}
.ops-card__icon.log {
  background: linear-gradient(135deg, #67c23a, #529b2e);
}
.ops-card h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #303133;
}
.ops-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  min-height: 64px;
  margin-bottom: 16px;
}
.log-card :deep(.el-card__header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cache-time {
  font-size: 12px;
  color: #909399;
}
</style>
