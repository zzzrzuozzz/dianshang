<template>
  <div v-loading="loading" class="type-edit-page">
    <el-card shadow="never" class="panel-card">
      <template #header><span class="section-title">{{ isEdit ? '编辑分类' : '添加分类' }}</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="分类名称" prop="name">
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
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="saveHelpType">保存</el-button>
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
import { createHelpTypeForm, fetchHelpTypeDetail, saveHelpType as saveHelpTypeApi } from '@/api/content'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const form = reactive(createHelpTypeForm())

const isEdit = computed(() => Boolean(route.params.id))

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  icon: [{
    validator: (_r, _v, cb) => (form.icon ? cb() : cb(new Error('请上传图标'))),
    trigger: 'change',
  }],
}

const onIconChange = (urls) => {
  form.icon = urls[0] || ''
  formRef.value?.validateField('icon')
}

const saveHelpType = async () => {
  await formRef.value?.validate().catch(() => Promise.reject())
  submitting.value = true
  try {
    await saveHelpTypeApi({
      ...form,
      id: isEdit.value ? Number(route.params.id) : undefined,
    })
    ElMessage.success('保存成功')
    router.push('/content/help/type')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const data = await fetchHelpTypeDetail(Number(route.params.id))
    form.name = data.name
    form.sort = data.sort ?? 0
    form.visible = data.visible !== false
    form.icon = data.icon || ''
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.panel-card { border-radius: 8px; border: none; }
.section-title { font-weight: 600; }
</style>
