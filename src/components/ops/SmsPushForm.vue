<template>
  <el-form ref="formRef" :model="pushForm" :rules="rules" label-width="120px" class="push-form">
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
      <el-button type="primary" class="submit-btn" :loading="submitting" @click="submitNotification">提交</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AudienceTargetFields from './AudienceTargetFields.vue'
import { createEmptyPushForm } from '@/mock/ops'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const pushForm = reactive(createEmptyPushForm())

const rules = {
  smsContent: [{ required: true, message: '请输入短信内容', trigger: 'blur' }],
  sendType: [{ required: true, message: '请选择推送时间', trigger: 'change' }],
}

const calcEstimate = async () => {
  await new Promise((r) => setTimeout(r, 200))
  pushForm.estimatedUsers = 4000 + Math.floor(Math.random() * 4000)
}

/** POST /api/ops/notification/save — msg_type=SMS，大批量触达建议异步队列 */
const submitNotification = async () => {
  await formRef.value?.validate().catch(() => {
    ElMessage.warning('请完善必填项')
    return Promise.reject()
  })
  submitting.value = true
  try {
    await new Promise((r) => setTimeout(r, 600))
    console.log('[mock] save sms', { ...pushForm, msg_type: 'SMS' })
    ElMessage.success('提交成功')
    router.back()
  } finally {
    submitting.value = false
  }
}

onMounted(calcEstimate)
</script>

<style scoped>
.push-form { max-width: 900px; }
.submit-btn { width: 100%; max-width: 400px; height: 44px; }
</style>
