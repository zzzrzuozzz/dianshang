<template>
  <div class="type-edit-page">
    <el-card shadow="never" class="panel-card">
      <template #header><span class="section-title">{{ isEdit ? '编辑分类' : '添加分类' }}</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="分类名称" prop="name">
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
            <div v-else class="icon-placeholder" @click="form.icon = 'https://picsum.photos/100/100?random=help'">
              <span>上传照片</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="saveHelpType">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createTypeForm, mockHelpTypes } from '@/mock/content'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const form = reactive(createTypeForm())
const isEdit = computed(() => Boolean(route.params.id && route.params.id !== 'new'))

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  icon: [{ required: true, message: '请上传图标', trigger: 'change' }],
}

/** POST /api/content/help/type/save */
const saveHelpType = async () => {
  await formRef.value?.validate().catch(() => Promise.reject())
  submitting.value = true
  try {
    await new Promise((r) => setTimeout(r, 500))
    ElMessage.success('保存成功')
    router.push('/content/help/type')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (isEdit.value) {
    const row = mockHelpTypes.find((t) => String(t.id) === route.params.id)
    if (row) Object.assign(form, { name: row.name, sort: row.sort, visible: row.visible, icon: row.icon })
  }
})
</script>

<style scoped>
.panel-card { border-radius: 8px; border: none; }
.icon-preview, .icon-placeholder { width: 100px; height: 100px; position: relative; border-radius: 4px; }
.icon-preview img { width: 100%; height: 100%; object-fit: cover; }
.icon-remove { position: absolute; top: 4px; right: 4px; width: 20px; height: 20px; background: #f56c6c; color: #fff; border-radius: 50%; text-align: center; line-height: 18px; cursor: pointer; }
.icon-placeholder { border: 1px dashed #dcdfe6; display: flex; align-items: center; justify-content: center; color: #909399; cursor: pointer; font-size: 12px; }
</style>
