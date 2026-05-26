<template>
  <div v-loading="loading" class="help-edit-page">
    <el-card shadow="never" class="panel-card">
      <template #header><span class="section-title">{{ isEdit ? '编辑帮助' : '新增帮助' }}</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="help-form">
        <el-form-item label="帮助类型" prop="typeId">
          <el-select v-model="form.typeId" placeholder="请选择类型" style="width: 400px">
            <el-option v-for="t in helpTypes" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="帮助标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入（限60字）" maxlength="60" show-word-limit style="width: 400px" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="请输入（限100字）" maxlength="100" show-word-limit style="width: 400px" />
        </el-form-item>
        <el-form-item label="详情">
          <SimpleRichEditor v-model="form.content" />
        </el-form-item>
        <el-form-item label="封面图" prop="coverImages">
          <ImageUploadGrid v-model="form.coverImages" :max="5" biz="content" hint="只支持jpg、png格式，最多5张" />
        </el-form-item>
        <el-form-item label="是否上线">
          <el-switch v-model="form.online" active-text="上线" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="submitting" @click="submitHelpArticle">提交</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ImageUploadGrid from '@/components/common/ImageUploadGrid.vue'
import SimpleRichEditor from '@/components/common/SimpleRichEditor.vue'
import {
  createHelpForm,
  fetchHelpDetail,
  fetchHelpTypeOptions,
  saveHelpArticle,
} from '@/api/content'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const helpTypes = ref([])
const form = reactive(createHelpForm())

const isEdit = computed(() => Boolean(route.params.id))

const rules = {
  typeId: [{ required: true, message: '请选择帮助类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入帮助标题', trigger: 'blur' }],
  coverImages: [{
    validator: (_r, v, cb) => (v?.length ? cb() : cb(new Error('请上传封面图'))),
    trigger: 'change',
  }],
}

const submitHelpArticle = async () => {
  await formRef.value?.validate().catch(() => {
    ElMessage.warning('请完善必填项')
    return Promise.reject()
  })
  submitting.value = true
  try {
    await saveHelpArticle({
      ...form,
      typeId: Number(form.typeId),
      articleCode: form.articleCode || (isEdit.value ? String(route.params.id) : undefined),
    })
    ElMessage.success(isEdit.value ? '保存成功' : '提交成功')
    router.push('/content/help')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    helpTypes.value = await fetchHelpTypeOptions()
    if (isEdit.value) {
      const data = await fetchHelpDetail(String(route.params.id))
      form.articleCode = data.articleCode
      form.typeId = data.typeId
      form.title = data.title || ''
      form.intro = data.intro || ''
      form.content = data.content || ''
      form.coverImages = data.coverImages ? [...data.coverImages] : []
      form.online = data.online !== false
      form.sort = data.sort ?? 0
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.help-edit-page { min-height: calc(100vh - 120px); }
.panel-card { border-radius: 8px; border: none; }
.rich-editor-wrap { max-width: 600px; border: 1px solid #dcdfe6; border-radius: 4px; padding: 8px; }
.submit-btn { width: 100%; max-width: 400px; height: 44px; margin-right: 12px; }
</style>
