<template>
  <div class="level-edit-page">
    <el-card shadow="never" class="panel-card">
      <template #header><span class="section-title">{{ isEdit ? '编辑等级' : '新增等级' }}</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="会员等级名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入" style="width: 400px" />
        </el-form-item>
        <el-form-item label="成长值满足点" prop="growthPoint">
          <el-input-number v-model="form.growthPoint" :min="0" style="width: 200px" />
        </el-form-item>
        <el-form-item label="默认会员">
          <el-checkbox-group v-model="form.isDefault">
            <el-checkbox :value="true">是</el-checkbox>
            <el-checkbox :value="false">否</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="免运费标准">
          <span>满</span>
          <el-input-number v-model="form.freeShipAmount" :min="0" size="small" style="margin: 0 8px" />
          <span>元/单可用 每月</span>
          <el-input-number v-model="form.freeShipTimes" :min="0" size="small" style="margin: 0 8px" />
          <span>次</span>
        </el-form-item>
        <el-form-item label="评价奖励">
          <span>每条奖励</span>
          <el-input-number v-model="form.reviewGrowth" :min="0" size="small" style="margin: 0 8px" />
          <span>成长值 每月</span>
          <el-input-number v-model="form.reviewTimes" :min="0" size="small" style="margin: 0 8px" />
          <span>次</span>
        </el-form-item>

        <el-divider content-position="left">权益开关</el-divider>
        <el-form-item v-for="priv in privileges" :key="priv.key" :label="priv.label">
          <el-switch v-model="form.privileges[priv.key]" active-text="已开启" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="saving" @click="submitLevel">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const formRef = ref(null)
const isEdit = computed(() => route.params.levelId && route.params.levelId !== 'new')

const privileges = [
  { key: 'freeShip', label: '满免运费特权' },
  { key: 'checkIn', label: '签到奖励特权' },
  { key: 'review', label: '评价奖励特权' },
  { key: 'exclusive', label: '专属活动特权' },
  { key: 'specialPrice', label: '会员特价特权' },
  { key: 'birthday', label: '生日礼包特权' },
]

const form = reactive({
  name: '',
  growthPoint: 1,
  isDefault: [false],
  freeShipAmount: 40,
  freeShipTimes: 2,
  reviewGrowth: 5,
  reviewTimes: 10,
  privileges: {
    freeShip: true,
    checkIn: true,
    review: true,
    exclusive: true,
    specialPrice: true,
    birthday: true,
  },
})

const rules = {
  name: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  growthPoint: [{ required: true, message: '请输入成长值', trigger: 'blur' }],
}

const submitLevel = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await new Promise((r) => setTimeout(r, 400))
    ElMessage.success('保存成功')
    router.push('/user/level')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.level-edit-page { min-height: calc(100vh - 120px); max-width: 720px; margin: 0 auto; }
.panel-card { border-radius: 8px; border: none; }
.section-title::before { content: ''; display: inline-block; width: 4px; height: 14px; margin-right: 8px; background: #409eff; border-radius: 2px; vertical-align: middle; }
.submit-btn { width: 100%; max-width: 400px; }
</style>
