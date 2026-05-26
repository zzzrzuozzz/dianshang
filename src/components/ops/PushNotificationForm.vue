<template>
  <el-form
    ref="formRef"
    v-loading="loading"
    :model="pushForm"
    :rules="rules"
    label-width="120px"
    class="push-form"
  >
    <template v-if="mode === 'system'">
      <el-form-item label="消息分类" prop="category">
        <el-select v-model="pushForm.category" placeholder="请选择分类" style="width: 400px">
          <el-option v-for="c in messageCategories" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="消息标题" prop="title">
        <el-input v-model="pushForm.title" placeholder="请输入（限60字）" maxlength="60" show-word-limit style="width: 400px" />
      </el-form-item>
      <el-form-item label="简介">
        <el-input v-model="pushForm.intro" type="textarea" :rows="3" placeholder="请输入（限100字）" maxlength="100" show-word-limit style="width: 400px" />
      </el-form-item>
    </template>

    <template v-else>
      <el-form-item label="消息标题" prop="title">
        <el-input v-model="pushForm.title" placeholder="请输入（限60字）" maxlength="60" show-word-limit style="width: 400px" />
      </el-form-item>
      <el-form-item label="消息内容" prop="stationContent">
        <el-input v-model="pushForm.stationContent" type="textarea" :rows="4" placeholder="请输入（限100字）" maxlength="100" show-word-limit style="width: 400px" />
      </el-form-item>
      <el-form-item label="推送方式">
        <el-checkbox-group v-model="pushForm.pushMethod">
          <el-checkbox value="assign">指定推送</el-checkbox>
          <el-checkbox value="tag">标签推送</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
    </template>

    <template v-if="mode === 'system'">
      <el-form-item label="跳转类型" prop="jumpType">
        <el-radio-group v-model="pushForm.jumpType">
          <el-radio value="TEXT">富文本</el-radio>
          <el-radio value="INNER">内链</el-radio>
          <el-radio value="OUTER">外链</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="pushForm.jumpType === 'TEXT'" label="详情" prop="detail">
        <div class="rich-editor-wrap">
          <div class="rich-toolbar">
            <el-button size="small">B</el-button>
            <el-button size="small">对齐</el-button>
            <el-button size="small">列表</el-button>
            <el-button size="small">图片</el-button>
            <el-button size="small">视频</el-button>
            <el-button size="small">链接</el-button>
          </div>
          <el-input v-model="pushForm.detail" type="textarea" :rows="8" placeholder="富文本内容" />
        </div>
      </el-form-item>

      <template v-if="pushForm.jumpType === 'INNER'">
        <el-form-item label="跳转页面">
          <el-radio-group v-model="pushForm.innerLinkType">
            <el-radio v-for="opt in innerLinkOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="">
          <el-input v-model="pushForm.jumpUrl" placeholder="请输入（限60字）" maxlength="60" show-word-limit style="width: 400px" />
          <p class="field-hint">内链、外链需填写；富文本无需填写</p>
        </el-form-item>
      </template>

      <el-form-item v-if="pushForm.jumpType === 'OUTER'" label="外链地址" prop="jumpUrl">
        <el-input v-model="pushForm.jumpUrl" placeholder="请输入外链地址（限60字）" maxlength="60" style="width: 400px" />
      </el-form-item>

      <el-form-item v-if="pushForm.jumpType === 'TEXT'" label="封面图">
        <ImageUploadGrid v-model="pushForm.coverImages" :max="5" hint="仅支持 .jpg .png 格式，最多5张" />
      </el-form-item>
    </template>

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

    <el-form-item v-if="mode === 'system'" label="同时生成">
      <el-checkbox-group v-model="pushForm.generateTypes">
        <el-checkbox value="ad">广告位</el-checkbox>
        <el-checkbox value="article">文章</el-checkbox>
        <el-checkbox value="topic">主题</el-checkbox>
      </el-checkbox-group>
      <p class="field-hint">生成后，请到对应模板，进行发布</p>
    </el-form-item>

    <el-form-item label="APP推送">
      <el-radio-group v-model="pushForm.appPush">
        <el-radio :value="false">不推送</el-radio>
        <el-radio :value="true">推送</el-radio>
      </el-radio-group>
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
import ImageUploadGrid from '@/components/common/ImageUploadGrid.vue'
import AudienceTargetFields from './AudienceTargetFields.vue'
import {
  messageCategories,
  innerLinkOptions,
  saveNotification,
  estimateAudience,
} from '@/api/ops'
import { applyRoutePreset, useNotificationEditor } from '@/composables/useNotificationEditor'

const props = defineProps({
  mode: { type: String, default: 'system' },
  msgType: { type: String, default: 'SYSTEM' },
})

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
  category: [{ required: true, message: '请选择消息分类', trigger: 'change' }],
  title: [{ required: true, message: '请输入消息标题', trigger: 'blur' }],
  jumpType: [{ required: true, message: '请选择跳转类型', trigger: 'change' }],
  sendType: [{ required: true, message: '请选择推送时间', trigger: 'change' }],
  stationContent: [{ required: true, message: '请输入消息内容', trigger: 'blur' }],
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
      msgType: props.msgType,
      memberLevels: pushForm.memberLevels?.length ? pushForm.memberLevels : ['all'],
      pushMethod: pushForm.pushMethod?.length ? pushForm.pushMethod : ['tag'],
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
  } else {
    applyRoutePreset(pushForm, route)
  }
  calcEstimate()
})
</script>

<style scoped>
.push-form { max-width: 900px; }
.rich-editor-wrap { width: 100%; border: 1px solid #dcdfe6; border-radius: 4px; }
.rich-toolbar { padding: 8px; border-bottom: 1px solid #ebeef5; display: flex; gap: 4px; }
.field-hint { font-size: 12px; color: #909399; margin-top: 4px; }
.submit-btn { width: 100%; max-width: 400px; height: 44px; margin-right: 12px; }
</style>
