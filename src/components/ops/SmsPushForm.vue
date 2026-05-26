<template>
  <el-form
    ref="formRef"
    v-loading="loading"
    :model="pushForm"
    :rules="rules"
    label-width="120px"
    class="push-form"
  >
    <el-form-item label="消息内容" prop="smsContent">
      <el-input v-model="pushForm.smsContent" type="textarea" :rows="5" placeholder="请输入（限100字）" maxlength="100" show-word-limit style="width: 400px" />
    </el-form-item>

    <AudienceTargetFields :form="pushForm" @estimate-change="calcEstimate" />

    <el-form-item label="推送时间" prop="sendType">
      <el-radio-group v-model="pushForm.sendType">
        <el-radio :value="1">立即推送</el-radio>
        <el-radio :value="2">定时推送</el-radio>
      </el-radio-group>
      <el-date-picker
        v-if="pushForm.sendType === 2"
        v-model="pushForm.publishTime"
        type="datetime"
        placeholder="请选择开始时间"
        value-format="YYYY-MM-DD HH:mm:ss"
        style="margin-left: 12px"
      />
    </el-form-item>

    <el-form-item>
      <el-button type="primary" class="submit-btn" :loading="submitting" @click="submitNotification">
        {{ isEdit ? '保存' : '提交' }}
      </el-button>
      <el-button @click="router.back()">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AudienceTargetFields from './AudienceTargetFields.vue'
import { saveNotification, estimateAudience } from '@/api/ops'
import { useNotificationEditor } from '@/composables/useNotificationEditor'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)

const notifyCode = computed(() => {
  const id = route.params.id
  return id ? String(id) : undefined
})
const isEdit = computed(() => Boolean(notifyCode.value))

const { form: pushForm, loading, loadForEdit } = useNotificationEditor(notifyCode)

const rules = {
  smsContent: [{ required: true, message: '请输入短信内容', trigger: 'blur' }],
  sendType: [{ required: true, message: '请选择推送时间', trigger: 'change' }],
}

const calcEstimate = async () => {
  try {
    const data = await estimateAudience({
      memberLevels: pushForm.memberLevels,
      regions: pushForm.regions,
      tags: pushForm.tags,
    })
    pushForm.estimatedUsers = data.estimatedUsers
  } catch {
    pushForm.estimatedUsers = 0
  }
}

watch(
  () => [pushForm.memberLevels, pushForm.regions],
  () => calcEstimate(),
  { deep: true },
)

const submitNotification = async () => {
  await formRef.value?.validate().catch(() => {
    ElMessage.warning('请完善必填项')
    return Promise.reject()
  })
  submitting.value = true
  try {
    await saveNotification({
      ...pushForm,
      notifyCode: pushForm.notifyCode || notifyCode.value,
      msgType: 'SMS',
      memberLevels: pushForm.memberLevels?.length ? pushForm.memberLevels : ['all'],
    })
    ElMessage.success(isEdit.value ? '保存成功' : '提交成功')
    router.back()
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (isEdit.value) {
    await loadForEdit()
  }
  calcEstimate()
})
</script>

<style scoped>
.push-form { max-width: 900px; }
.submit-btn { width: 100%; max-width: 400px; height: 44px; margin-right: 12px; }
</style>
