<template>
  <div v-loading="loading" class="tag-edit-page">
    <el-card shadow="never" class="panel-card">
      <template #header><span class="section-title">{{ isEdit ? '编辑标签' : '新增标签' }}</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="tag-form">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入" style="width: 400px" />
        </el-form-item>

        <el-divider content-position="left">自动打标签设置</el-divider>

        <el-form-item label="性别">
          <el-checkbox-group v-model="form.gender">
            <el-checkbox value="all">全部</el-checkbox>
            <el-checkbox value="male">男</el-checkbox>
            <el-checkbox value="female">女</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="会员">
          <el-checkbox-group v-model="form.memberLevels">
            <el-checkbox value="all">全部会员</el-checkbox>
            <el-checkbox v-for="lv in memberLevelOptions" :key="lv.value" :value="lv.value">{{ lv.label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="城市">
          <el-radio-group v-model="form.cityMode">
            <el-radio value="all">全部地区</el-radio>
            <el-radio value="specific">指定地区</el-radio>
          </el-radio-group>
          <AreaCascader
            v-if="form.cityMode === 'specific'"
            v-model="form.regionCodes"
            multiple
            placeholder="请选择省/市/区（可多选）"
            style="margin-top: 8px; max-width: 480px"
          />
        </el-form-item>

        <el-form-item label="注册时间">
          <el-checkbox v-model="form.registerEnabled">启用条件</el-checkbox>
          <div v-if="form.registerEnabled" class="condition-block">
            <el-radio-group v-model="form.registerType">
              <el-radio value="unlimited">无限制</el-radio>
              <el-radio value="range">日期范围</el-radio>
              <el-radio value="days">固定天数</el-radio>
            </el-radio-group>
            <el-date-picker
              v-if="form.registerType === 'range'"
              v-model="form.registerRange"
              type="daterange"
              style="margin-top: 8px"
            />
            <el-input-number v-if="form.registerType === 'days'" v-model="form.registerDays" :min="1" style="margin-top: 8px" />
            <span v-if="form.registerType === 'days'" class="unit">天</span>
          </div>
        </el-form-item>

        <el-form-item label="成交笔数">
          <el-checkbox v-model="form.orderCountEnabled">启用条件</el-checkbox>
          <div v-if="form.orderCountEnabled" class="condition-block">
            <el-radio-group v-model="form.orderCountType">
              <el-radio value="unlimited">无限制</el-radio>
              <el-radio value="range">日期范围</el-radio>
              <el-radio value="cumulative">累计成交笔数</el-radio>
            </el-radio-group>
            <el-input-number
              v-if="form.orderCountType === 'cumulative'"
              v-model="form.orderCount"
              :min="0"
              style="margin-top: 8px"
            />
            <span v-if="form.orderCountType === 'cumulative'" class="unit">笔</span>
          </div>
        </el-form-item>

        <el-form-item label="成交金额">
          <el-checkbox v-model="form.amountEnabled">启用条件</el-checkbox>
          <div v-if="form.amountEnabled" class="condition-block">
            <el-radio-group v-model="form.amountType">
              <el-radio value="unlimited">无限制</el-radio>
              <el-radio value="range">日期范围</el-radio>
              <el-radio value="cumulative">累计成交金额</el-radio>
            </el-radio-group>
            <el-input-number
              v-if="form.amountType === 'cumulative'"
              v-model="form.amount"
              :min="0"
              :precision="2"
              style="margin-top: 8px"
            />
            <span v-if="form.amountType === 'cumulative'" class="unit">元</span>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="saving" @click="saveTagRule">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AreaCascader from '@/components/AreaCascader/index.vue'
import { memberLevelOptions } from '@/mock/user'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const formRef = ref(null)

const isEdit = computed(() => route.params.tagId && route.params.tagId !== 'new')

const form = reactive({
  name: '',
  gender: ['all'],
  memberLevels: ['all'],
  cityMode: 'all',
  regionCodes: [],
  registerEnabled: false,
  registerType: 'unlimited',
  registerRange: [],
  registerDays: 30,
  orderCountEnabled: false,
  orderCountType: 'cumulative',
  orderCount: 10,
  amountEnabled: false,
  amountType: 'cumulative',
  amount: 2000,
})

const rules = { name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }] }

/**
 * POST /api/user/tag/save
 */
const saveTagRule = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    // await axios.post('/api/user/tag/save', form)
    await new Promise((r) => setTimeout(r, 400))
    ElMessage.success('保存成功')
    router.push('/user/tag')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.tag-edit-page { min-height: calc(100vh - 120px); max-width: 800px; }
.panel-card { border-radius: 8px; border: none; }
.section-title::before { content: ''; display: inline-block; width: 4px; height: 14px; margin-right: 8px; background: #409eff; border-radius: 2px; vertical-align: middle; }
.condition-block { margin-top: 8px; padding: 12px; background: #fafafa; border-radius: 6px; }
.unit { margin-left: 8px; color: #909399; }
.submit-btn { width: 100%; max-width: 400px; margin-top: 16px; }
</style>
