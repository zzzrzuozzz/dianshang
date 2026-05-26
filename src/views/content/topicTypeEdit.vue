<template>
  <div v-loading="loading" class="type-edit-page">
    <el-card shadow="never" class="panel-card">
      <template #header><span class="section-title">{{ isEdit ? '编辑类型' : '添加类型' }}</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="type-form">
        <el-form-item label="类型名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入（限60字）" maxlength="60" show-word-limit style="width: 400px" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" style="width: 200px" />
        </el-form-item>
        <el-form-item label="是否显示">
          <el-switch v-model="form.visible" active-text="已显示" />
        </el-form-item>
        <el-form-item label="类型图标" prop="icon">
          <ImageUploadGrid
            :model-value="form.icon ? [form.icon] : []"
            :max="1"
            biz="content"
            add-label="上传照片"
            hint="只支持 .jpg .png 格式，最多1张"
            @update:model-value="onIconChange"
          />
        </el-form-item>
        <el-form-item label="类型介绍">
          <el-input v-model="form.intro" type="textarea" :rows="4" placeholder="请输入（限100字）" maxlength="100" show-word-limit style="width: 400px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="save-btn" :loading="submitting" @click="saveTopicType">保存</el-button>
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
import { createTopicTypeForm, fetchTopicTypeDetail, saveTopicType as saveTopicTypeApi } from '@/api/content'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const form = reactive(createTopicTypeForm())

const isEdit = computed(() => Boolean(route.params.id))

const rules = {
  name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  icon: [{
    validator: (_r, _v, cb) => (form.icon ? cb() : cb(new Error('请上传类型图标'))),
    trigger: 'change',
  }],
}

const onIconChange = (urls) => {
  form.icon = urls[0] || ''
  formRef.value?.validateField('icon')
}

const saveTopicType = async () => {
  await formRef.value?.validate().catch(() => {
    ElMessage.warning('请完善必填项')
    return Promise.reject()
  })
  submitting.value = true
  try {
    await saveTopicTypeApi({
      ...form,
      id: isEdit.value ? Number(route.params.id) : undefined,
    })
    ElMessage.success('保存成功')
    router.push('/content/topic/type')
  } finally {
    submitting.value = false
  }
}

const loadDetail = async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const data = await fetchTopicTypeDetail(Number(route.params.id))
    form.name = data.name
    form.sort = data.sort ?? 0
    form.visible = data.visible !== false
    form.icon = data.icon || ''
    form.intro = data.intro || ''
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.type-edit-page { min-height: calc(100vh - 120px); }
.panel-card { border-radius: 8px; border: none; }
.section-title { font-weight: 600; }
.save-btn { width: 200px; height: 40px; margin-right: 12px; }
</style>
