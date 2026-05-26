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
          <el-input v-model="advForm.detail" type="textarea" :rows="6" placeholder="富文本详情" style="width: 100%; max-width: 600px" />
        </el-form-item>
        <el-form-item v-else label="跳转链接" prop="jumpUrl">
          <el-input v-model="advForm.jumpUrl" placeholder="请输入跳转地址" style="width: 400px" />
        </el-form-item>

        <el-form-item label="封面图">
          <div class="cover-list">
            <div v-for="(img, idx) in advForm.coverImages" :key="idx" class="cover-item">
              <img :src="img" alt="cover" />
              <span class="cover-remove" @click="advForm.coverImages.splice(idx, 1)">×</span>
            </div>
            <div v-if="advForm.coverImages.length < 5" class="cover-upload" @click="addCover">
              <span>上传图片</span>
            </div>
          </div>
          <p class="field-hint">仅支持 .jpg .png 格式，最多5张</p>
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
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AudienceTargetFields from '@/components/ops/AudienceTargetFields.vue'
import { advTypeOptions, createEmptyPushForm } from '@/mock/ops'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const isEdit = computed(() => Boolean(route.params.id && route.params.id !== 'new'))

const base = createEmptyPushForm()
const advForm = reactive({
  ...base,
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
  await new Promise((r) => setTimeout(r, 200))
  advForm.estimatedUsers = 5000 + Math.floor(Math.random() * 3000)
}

const addCover = () => {
  advForm.coverImages.push('https://picsum.photos/120/120?random=' + Date.now())
}

/**
 * POST /api/ops/advertisement/save
 * C 端高频读取，保存/上下架后务必同步刷新或清除 Redis 广告列表缓存。
 */
const submitAdvertisement = async () => {
  await formRef.value?.validate().catch(() => {
    ElMessage.warning('请完善必填项')
    return Promise.reject()
  })
  submitting.value = true
  try {
    await new Promise((r) => setTimeout(r, 600))
    console.log('[mock] save advertisement', advForm)
    ElMessage.success('保存成功')
    router.push('/ops/advertisement')
  } finally {
    submitting.value = false
  }
}

onMounted(calcEstimate)
</script>

<style scoped>
.adv-edit-page { min-height: calc(100vh - 120px); }
.panel-card { border-radius: 8px; border: none; }
.section-title { font-weight: 600; }
.adv-form { max-width: 900px; }
.field-hint { font-size: 12px; color: #909399; margin-top: 4px; }
.cover-list { display: flex; flex-wrap: wrap; gap: 12px; }
.cover-item { position: relative; width: 100px; height: 100px; border-radius: 4px; overflow: hidden; }
.cover-item img { width: 100%; height: 100%; object-fit: cover; }
.cover-remove { position: absolute; top: 4px; right: 4px; width: 20px; height: 20px; background: #f56c6c; color: #fff; border-radius: 50%; text-align: center; line-height: 18px; cursor: pointer; }
.cover-upload { width: 100px; height: 100px; border: 1px dashed #dcdfe6; display: flex; align-items: center; justify-content: center; color: #909399; cursor: pointer; font-size: 12px; }
.range-sep { margin: 0 8px; color: #909399; }
.submit-btn { width: 100%; max-width: 400px; height: 44px; }
</style>
