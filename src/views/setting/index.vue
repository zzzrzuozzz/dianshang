<template>
  <div v-loading="loading" class="setting-page">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="linkage-alert"
      title="全局联动说明"
    >
      <template #default>
        <ul class="linkage-list">
          <li>商城名称 → 登录页标题、侧栏品牌、首页平台横幅</li>
          <li>客服热线 → 订单详情页平台信息条</li>
          <li>包邮门槛 → 订单详情运费说明、首页横幅</li>
          <li>未付款关单时限 → 后台定时任务每分钟扫描关闭超时订单</li>
          <li>库存策略 → 订单发货时按「下单减库存 / 付款减库存」规则出库</li>
        </ul>
      </template>
    </el-alert>

    <el-row :gutter="16" class="quick-links">
      <el-col :span="8">
        <el-card shadow="hover" class="link-card" @click="router.push('/setting/region')">
          <el-icon><Location /></el-icon>
          <span>行政区划</span>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="link-card" @click="router.push('/setting/maintenance')">
          <el-icon><Tools /></el-icon>
          <span>对账与维护</span>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="link-card" @click="router.push('/order/setting')">
          <el-icon><List /></el-icon>
          <span>订单设置</span>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="card-head">
          <span class="card-title">平台基础参数配置</span>
          <span v-if="formMeta.lastUpdateTime" class="card-sub">
            最近更新：{{ formMeta.lastUpdateTime }}
          </span>
          <span v-else class="card-sub">修改后将同步至全站展示与订单/库存业务规则</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="160px"
        class="config-form"
      >
        <el-form-item label="商城品牌名称" prop="shopName">
          <el-input v-model="form.shopName" placeholder="如: 暴走电商" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="官方客服热线" prop="servicePhone">
          <el-input v-model="form.servicePhone" placeholder="400-888-8888" maxlength="20" />
        </el-form-item>
        <el-form-item label="统一运费收费标准" prop="freeShipThreshold">
          <div class="inline-field">
            <span class="prefix">满</span>
            <el-input-number v-model="form.freeShipThreshold" :min="0" :max="99999" :precision="2" />
            <span class="suffix">元包邮</span>
          </div>
        </el-form-item>
        <el-form-item label="自动关闭未付款订单" prop="unpaidCloseMinutes">
          <el-input-number v-model="form.unpaidCloseMinutes" :min="5" :max="1440" />
          <span class="unit">分钟（定时任务自动执行）</span>
        </el-form-item>
        <el-form-item label="库存扣减策略" prop="stockDeductStrategy">
          <el-radio-group v-model="form.stockDeductStrategy">
            <el-radio value="order">下单减库存（发货时扣减）</el-radio>
            <el-radio value="pay">付款减库存（已付款发货时扣减）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            保存平台配置
          </el-button>
          <el-button @click="loadConfig">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Tools, List } from '@element-plus/icons-vue'
import { fetchPlatformConfig, savePlatformConfig } from '@/api/system'
import { refreshPlatformConfig } from '@/composables/usePlatformConfig'

const router = useRouter()
const phonePattern = /^(1[3-9]\d{9}|0\d{2,3}-?\d{7,8}|400-?\d{3}-?\d{4})$/

const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  shopName: '',
  servicePhone: '',
  freeShipThreshold: 99,
  unpaidCloseMinutes: 30,
  stockDeductStrategy: 'order',
})

const formMeta = reactive({ lastUpdateTime: '' })

const rules = {
  shopName: [{ required: true, message: '请输入商城品牌名称', trigger: 'blur' }],
  servicePhone: [
    { required: true, message: '请输入客服热线', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (!value || phonePattern.test(String(value).trim())) callback()
        else callback(new Error('请输入正确的手机或固话/400号码'))
      },
      trigger: 'blur',
    },
  ],
  freeShipThreshold: [{ required: true, message: '请设置包邮门槛', trigger: 'change' }],
  unpaidCloseMinutes: [{ required: true, message: '请设置关闭时限', trigger: 'change' }],
  stockDeductStrategy: [{ required: true, message: '请选择扣减策略', trigger: 'change' }],
}

const loadConfig = async () => {
  loading.value = true
  try {
    const data = await fetchPlatformConfig()
    if (data) {
      form.shopName = data.shopName || ''
      form.servicePhone = data.servicePhone || ''
      form.freeShipThreshold = Number(data.freeShipThreshold ?? 99)
      form.unpaidCloseMinutes = Number(data.unpaidCloseMinutes ?? 30)
      form.stockDeductStrategy = data.stockDeductStrategy || 'order'
      formMeta.lastUpdateTime = data.lastUpdateTime || ''
    }
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await savePlatformConfig({
      shopName: form.shopName.trim(),
      servicePhone: form.servicePhone.trim(),
      freeShipThreshold: form.freeShipThreshold,
      unpaidCloseMinutes: form.unpaidCloseMinutes,
      stockDeductStrategy: form.stockDeductStrategy,
    })
    ElMessage.success('平台配置已保存，全站联动已刷新')
    await refreshPlatformConfig()
    await loadConfig()
  } finally {
    submitting.value = false
  }
}

onMounted(loadConfig)
onActivated(loadConfig)
</script>

<style scoped>
.setting-page {
  max-width: 900px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.linkage-alert {
  border-radius: 8px;
}
.linkage-list {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  line-height: 1.8;
  color: #606266;
}
.quick-links {
  margin-bottom: 0;
}
.link-card {
  cursor: pointer;
  text-align: center;
  padding: 16px 0;
  border-radius: 8px;
  transition: transform 0.15s;
}
.link-card:hover {
  transform: translateY(-2px);
}
.link-card .el-icon {
  font-size: 28px;
  color: #409eff;
  display: block;
  margin: 0 auto 8px;
}
.panel-card {
  border-radius: 8px;
}
.card-head {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
}
.card-sub {
  font-size: 12px;
  color: #909399;
}
.inline-field {
  display: flex;
  align-items: center;
  gap: 8px;
}
.prefix,
.suffix,
.unit {
  font-size: 14px;
  color: #606266;
}
</style>
