<template>
  <div class="topic-edit-page">
    <el-card shadow="never" class="panel-card">
      <template #header><span class="section-title">{{ isEdit ? '编辑专题' : '新增专题' }}</span></template>
      <el-form ref="formRef" :model="topicForm" :rules="rules" label-width="120px" class="topic-form">
        <el-form-item label="专题类型" prop="typeId">
          <el-select v-model="topicForm.typeId" placeholder="请选择类型" style="width: 400px">
            <el-option v-for="t in topicTypes" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专题标题" prop="title">
          <el-input v-model="topicForm.title" placeholder="请输入" maxlength="60" show-word-limit style="width: 400px" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="topicForm.intro" type="textarea" :rows="3" placeholder="请输入（限100字）" maxlength="100" show-word-limit style="width: 400px" />
        </el-form-item>
        <el-form-item label="详情">
          <div class="rich-editor-wrap">
            <div class="rich-toolbar">
              <el-button size="small">B</el-button>
              <el-button size="small">对齐</el-button>
              <el-button size="small">列表</el-button>
              <el-button size="small">图片</el-button>
              <el-button size="small">链接</el-button>
            </div>
            <el-input v-model="topicForm.content" type="textarea" :rows="8" placeholder="富文本详情（联调时替换编辑器）" />
          </div>
        </el-form-item>
        <el-form-item label="封面图">
          <div class="cover-upload">
            <div v-if="topicForm.coverImage" class="cover-preview">
              <img :src="topicForm.coverImage" alt="cover" />
              <span class="cover-remove" @click="topicForm.coverImage = ''">×</span>
            </div>
            <div v-else class="cover-placeholder" @click="uploadCover">
              <span>上传图片</span>
            </div>
          </div>
          <p class="hint">仅支持 .jpg .png，建议单张封面</p>
        </el-form-item>

        <el-form-item label="关联商品">
          <el-checkbox v-model="topicForm.specifyProducts">指定商品</el-checkbox>
          <template v-if="topicForm.specifyProducts">
            <span class="product-summary">已选择 {{ topicForm.productIds.length }} 款商品</span>
            <el-link type="primary" @click="pickVisible = true">选择商品 &gt;</el-link>
          </template>
        </el-form-item>

        <AudienceTargetFields :form="topicForm" @estimate-change="calcEstimate" />

        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="submitting" @click="submitTopic">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <ProductPickDialog v-model="pickVisible" :selected="topicForm.productIds" @confirm="onProductsPicked" />
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AudienceTargetFields from '@/components/ops/AudienceTargetFields.vue'
import ProductPickDialog from '@/components/content/ProductPickDialog.vue'
import { createTopicForm, mockTopicTypes } from '@/mock/content'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const pickVisible = ref(false)
const topicTypes = ref([...mockTopicTypes])
const topicForm = reactive(createTopicForm())

const isEdit = computed(() => Boolean(route.params.id))

const rules = {
  typeId: [{ required: true, message: '请选择专题类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入专题标题', trigger: 'blur' }],
}

const uploadCover = () => {
  topicForm.coverImage = 'https://picsum.photos/200/200?random=cover'
}

const onProductsPicked = ({ ids }) => {
  topicForm.productIds = ids
}

const calcEstimate = async () => {
  await new Promise((r) => setTimeout(r, 200))
  topicForm.estimatedUsers = 5000 + Math.floor(Math.random() * 3000)
}

/**
 * POST /api/content/topic/save
 * 选中的商品 ID 数组需转为逗号分隔字符串或 JSON 传给后端（product_ids 字段）
 */
const submitTopic = async () => {
  await formRef.value?.validate().catch(() => {
    ElMessage.warning('请完善必填项')
    return Promise.reject()
  })
  submitting.value = true
  try {
    await new Promise((r) => setTimeout(r, 600))
    const payload = {
      ...topicForm,
      product_ids: topicForm.productIds.join(','),
    }
    console.log('[mock] save topic', payload)
    ElMessage.success('提交成功')
    router.push('/content/topic')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  calcEstimate()
  if (isEdit.value) {
    topicForm.title = '春季家电家具疯狂秒杀'
    topicForm.typeId = 2
    topicForm.productIds = ['025342', '025343']
  }
})
</script>

<style scoped>
.topic-edit-page { min-height: calc(100vh - 120px); }
.panel-card { border-radius: 8px; border: none; }
.section-title { font-weight: 600; }
.rich-editor-wrap { width: 100%; max-width: 600px; border: 1px solid #dcdfe6; border-radius: 4px; }
.rich-toolbar { padding: 8px; border-bottom: 1px solid #ebeef5; display: flex; gap: 4px; }
.cover-preview, .cover-placeholder { width: 100px; height: 100px; position: relative; border-radius: 4px; }
.cover-preview img { width: 100%; height: 100%; object-fit: cover; }
.cover-remove { position: absolute; top: 4px; right: 4px; width: 20px; height: 20px; background: #f56c6c; color: #fff; border-radius: 50%; text-align: center; line-height: 18px; cursor: pointer; }
.cover-placeholder { border: 1px dashed #dcdfe6; display: flex; align-items: center; justify-content: center; color: #909399; cursor: pointer; font-size: 12px; }
.hint { font-size: 12px; color: #909399; margin-top: 4px; }
.product-summary { margin: 0 12px; color: #606266; font-size: 13px; }
.submit-btn { width: 100%; max-width: 400px; height: 44px; }
</style>
