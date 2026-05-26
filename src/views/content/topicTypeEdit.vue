<template>
  <div class="type-edit-page">
    <el-card shadow="never" class="panel-card">
      <template #header><span class="section-title">{{ isEdit ? '编辑类型' : '添加类型' }}</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="type-form">
        <el-form-item label="类型名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入（限60字）" maxlength="60" show-word-limit style="width: 400px" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input v-model="form.sort" placeholder="请输入" style="width: 200px" />
        </el-form-item>
        <el-form-item label="是否显示">
          <el-switch v-model="form.visible" active-text="已显示" />
        </el-form-item>
        <el-form-item label="类型图标" prop="icon">
          <div class="icon-upload">
            <div v-if="form.icon" class="icon-preview">
              <img :src="form.icon" alt="icon" />
              <span class="icon-remove" @click="form.icon = ''">×</span>
            </div>
            <div v-else class="icon-placeholder" @click="uploadIcon">
              <el-icon><Plus /></el-icon>
              <span>上传照片</span>
            </div>
          </div>
          <p class="hint">只支持.jpg .png 格式，最多1张</p>
        </el-form-item>
        <el-form-item label="类型介绍">
          <el-input v-model="form.intro" type="textarea" :rows="4" placeholder="请输入（限100字）" maxlength="100" show-word-limit style="width: 400px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="save-btn" :loading="submitting" @click="saveTopicType">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createTypeForm, mockTopicTypes } from '@/mock/content'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const form = reactive(createTypeForm())

const isEdit = computed(() => Boolean(route.params.id && route.params.id !== 'new'))

const rules = {
  name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  icon: [{ required: true, message: '请上传类型图标', trigger: 'change' }],
}

const uploadIcon = () => {
  form.icon = 'https://picsum.photos/100/100?random=type'
}

/**
 * POST /api/content/topic/type/save
 */
const saveTopicType = async () => {
  await formRef.value?.validate().catch(() => {
    ElMessage.warning('请完善必填项')
    return Promise.reject()
  })
  submitting.value = true
  try {
    await new Promise((r) => setTimeout(r, 500))
    console.log('[mock] save topic type', form)
    ElMessage.success('保存成功')
    router.push('/content/topic/type')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (isEdit.value) {
    const row = mockTopicTypes.find((t) => String(t.id) === route.params.id)
    if (row) Object.assign(form, { name: row.name, sort: row.sort, visible: row.visible, icon: row.icon, intro: row.intro })
  }
})
</script>

<style scoped>
.type-edit-page { min-height: calc(100vh - 120px); }
.panel-card { border-radius: 8px; border: none; }
.section-title { font-weight: 600; }
.icon-upload { display: flex; gap: 12px; }
.icon-preview, .icon-placeholder { width: 100px; height: 100px; border-radius: 4px; position: relative; }
.icon-preview img { width: 100%; height: 100%; object-fit: cover; }
.icon-remove { position: absolute; top: 4px; right: 4px; width: 20px; height: 20px; background: #f56c6c; color: #fff; border-radius: 50%; text-align: center; line-height: 18px; cursor: pointer; }
.icon-placeholder { border: 1px dashed #dcdfe6; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #909399; cursor: pointer; font-size: 12px; }
.hint { font-size: 12px; color: #909399; margin-top: 4px; }
.save-btn { width: 200px; height: 40px; }
</style>
