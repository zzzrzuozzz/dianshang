<template>
  <div class="adv-edit-page">
    <el-card shadow="never" class="panel-card">
      <template #header><span class="section-title">{{ isEdit ? '编辑广告' : '新增广告' }}</span></template>
      <el-form ref="formRef" :model="advForm" :rules="rules" label-width="120px" class="adv-form">
        <el-form-item label="广告分类" prop="category">
          <el-select v-model="advForm.category" placeholder="请选择分类" style="width: 400px">
            <el-option v-for="t in advTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="广告标题" prop="title">
          <el-input v-model="advForm.title" placeholder="请输入（限60字）" maxlength="60" show-word-limit style="width: 400px" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="advForm.intro" type="textarea" :rows="3" placeholder="请输入（限100字）" maxlength="100" show-word-limit style="width: 400px" />
        </el-form-item>

        <el-form-item label="跳转类型" prop="jumpType">
          <el-radio-group v-model="advForm.jumpType">
            <el-radio value="TEXT">富文本</el-radio>
            <el-radio value="INNER">内链</el-radio>
            <el-radio value="OUTER">外链</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="advForm.jumpType === 'TEXT'" label="详情">
          <SimpleRichEditor v-model="advForm.detail" min-height="180px" />
        </el-form-item>
        <el-form-item v-else label="跳转链接" prop="jumpUrl">
          <el-input v-model="advForm.jumpUrl" placeholder="请输入跳转地址" style="width: 400px" />
        </el-form-item>

        <el-form-item label="封面图">
          <ImageUploadGrid v-model="advForm.coverImages" :max="5" hint="仅支持 .jpg .png 格式，最多5张" />
        </el-form-item>

        <AudienceTargetFields :form="advForm" @estimate-change="calcEstimate" />

        <el-form-item label="展示时间" required>
          <el-date-picker v-model="advForm.startTime" type="datetime" placeholder="开始时间" value-format="YYYY-MM-DD HH:mm:ss" />
          <span class="range-sep">至</span>
          <el-date-picker v-model="advForm.endTime" type="datetime" placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>

        <el-form-item label="是否上线">
          <el-switch v-model="advForm.online" />
        </el-form-item>

        <el-form-item label="APP推送">
          <el-radio-group v-model="advForm.appPush">
            <el-radio :value="false">不推送</el-radio>
            <el-radio :value="true">推送</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="submitting" @click="submitAdvertisement">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ImageUploadGrid from '@/components/common/ImageUploadGrid.vue'
import SimpleRichEditor from '@/components/common/SimpleRichEditor.vue'
import AudienceTargetFields from '@/components/ops/AudienceTargetFields.vue'
import {
  advTypeOptions,
  createEmptyPushForm,
  fetchAdvertisementDetail,
  saveAdvertisement,
  estimateAudience,
} from '@/api/ops'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const isEdit = computed(() => Boolean(route.params.id))

const base = createEmptyPushForm()
const advForm = reactive({
  ...base,
  advCode: '',
  category: '',
  title: '',
  intro: '',
  startTime: '',
  endTime: '',
  online: true,
})

const rules = {
  category: [{ required: true, message: '请选择广告分类', trigger: 'change' }],
  title: [{ required: true, message: '请输入广告标题', trigger: 'blur' }],
  jumpType: [{ required: true, message: '请选择跳转类型', trigger: 'change' }],
}

const calcEstimate = async () => {
  try {
    const data = await estimateAudience({
      memberLevels: advForm.memberLevels,
      regions: advForm.regions,
      tags: advForm.tags,
    })
    advForm.estimatedUsers = data.estimatedUsers
  } catch {
    advForm.estimatedUsers = 0
  }
}

watch(
  () => [advForm.memberLevels, advForm.regions],
  () => calcEstimate(),
  { deep: true },
)

const loadDetail = async () => {
  if (!isEdit.value) return
  const detail = await fetchAdvertisementDetail(String(route.params.id))
  advForm.advCode = detail.advCode
  advForm.category = detail.category
  advForm.title = detail.title
  advForm.intro = detail.intro || ''
  advForm.jumpType = detail.jumpType || 'INNER'
  advForm.jumpUrl = detail.jumpUrl || ''
  advForm.detail = detail.detail || ''
  advForm.coverImages = detail.coverImages || []
  advForm.memberLevels = detail.memberLevels || ['all']
  advForm.regions = detail.regions || []
  advForm.tags = detail.tags || advForm.tags
  advForm.startTime = detail.startTime || ''
  advForm.endTime = detail.endTime || ''
  advForm.online = detail.online !== false
  advForm.appPush = Boolean(detail.appPush)
  advForm.estimatedUsers = detail.estimatedUsers || 0
}

const submitAdvertisement = async () => {
  await formRef.value?.validate().catch(() => {
    ElMessage.warning('请完善必填项')
    return Promise.reject()
  })
  submitting.value = true
  try {
    await saveAdvertisement({
      advCode: advForm.advCode || undefined,
      category: advForm.category,
      title: advForm.title,
      intro: advForm.intro,
      jumpType: advForm.jumpType,
      jumpUrl: advForm.jumpUrl,
      detail: advForm.detail,
      coverImages: advForm.coverImages,
      memberLevels: advForm.memberLevels,
      regions: advForm.regions,
      tags: advForm.tags,
      startTime: advForm.startTime,
      endTime: advForm.endTime,
      online: advForm.online,
      appPush: advForm.appPush,
      estimatedUsers: advForm.estimatedUsers,
    })
    ElMessage.success('保存成功')
    router.push('/ops/advertisement')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (isEdit.value) {
    await loadDetail()
  }
  calcEstimate()
})
</script>

<style scoped>
.adv-edit-page { min-height: calc(100vh - 120px); }
.panel-card { border-radius: 8px; border: none; }
.section-title { font-weight: 600; }
.adv-form { max-width: 900px; }
.field-hint { font-size: 12px; color: #909399; margin-top: 4px; }
.range-sep { margin: 0 8px; color: #909399; }
.submit-btn { width: 100%; max-width: 400px; height: 44px; }
</style>
