<template>
  <div class="help-edit-page">
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
          <div class="rich-editor-wrap">
            <div class="rich-toolbar">
              <el-button size="small">左对齐</el-button>
              <el-button size="small">居中</el-button>
              <el-button size="small">列表</el-button>
              <el-button size="small">图片</el-button>
              <el-button size="small">链接</el-button>
            </div>
            <el-input v-model="form.content" type="textarea" :rows="8" placeholder="富文本详情" />
          </div>
        </el-form-item>
        <el-form-item label="封面图" prop="coverImages">
          <div class="cover-list">
            <div v-for="(img, idx) in form.coverImages" :key="idx" class="cover-item">
              <img :src="img" alt="cover" />
              <span class="cover-remove" @click="form.coverImages.splice(idx, 1)">×</span>
            </div>
            <div v-if="form.coverImages.length < 5" class="cover-upload" @click="addCover">
              <span>上传照片</span>
            </div>
          </div>
          <p class="hint">只支持jpg、png格式，最多5张</p>
        </el-form-item>
        <el-form-item label="是否上线">
          <el-switch v-model="form.online" active-text="上线" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="submitting" @click="submitHelpArticle">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createHelpForm, mockHelpTypes } from '@/mock/content'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const helpTypes = ref([...mockHelpTypes])
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

const addCover = () => {
  form.coverImages.push('https://picsum.photos/120/120?random=' + Date.now())
}

/**
 * POST /api/content/help/save
 * 后端建议对帮助列表使用 @Cacheable + Redis 缓存以支撑 C 端高频读取
 */
const submitHelpArticle = async () => {
  await formRef.value?.validate().catch(() => {
    ElMessage.warning('请完善必填项')
    return Promise.reject()
  })
  submitting.value = true
  try {
    await new Promise((r) => setTimeout(r, 600))
    console.log('[mock] save help article', form)
    ElMessage.success('提交成功')
    router.push('/content/help')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (isEdit.value) {
    form.title = '常见问题'
    form.typeId = 2
    form.coverImages = ['https://picsum.photos/120/120?random=1']
  }
})
</script>

<style scoped>
.help-edit-page { min-height: calc(100vh - 120px); }
.panel-card { border-radius: 8px; border: none; }
.rich-editor-wrap { max-width: 600px; border: 1px solid #dcdfe6; border-radius: 4px; }
.rich-toolbar { padding: 8px; border-bottom: 1px solid #ebeef5; display: flex; gap: 4px; }
.cover-list { display: flex; flex-wrap: wrap; gap: 12px; }
.cover-item { position: relative; width: 100px; height: 100px; }
.cover-item img { width: 100%; height: 100%; object-fit: cover; border-radius: 4px; }
.cover-remove { position: absolute; top: 4px; right: 4px; width: 20px; height: 20px; background: #f56c6c; color: #fff; border-radius: 50%; text-align: center; line-height: 18px; cursor: pointer; }
.cover-upload { width: 100px; height: 100px; border: 1px dashed #dcdfe6; display: flex; align-items: center; justify-content: center; color: #909399; cursor: pointer; font-size: 12px; }
.hint { font-size: 12px; color: #909399; margin-top: 4px; }
.submit-btn { width: 100%; max-width: 400px; height: 44px; }
</style>
