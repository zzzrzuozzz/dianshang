<template>
  <div v-loading="loading" class="topic-edit-page">
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
          <SimpleRichEditor v-model="topicForm.content" />
        </el-form-item>
        <el-form-item label="封面图">
          <ImageUploadGrid
            :model-value="topicForm.coverImage ? [topicForm.coverImage] : []"
            :max="1"
            biz="content"
            @update:model-value="(urls) => { topicForm.coverImage = urls[0] || '' }"
          />
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
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <ProductPickDialog v-model="pickVisible" :selected="topicForm.productIds" @confirm="onProductsPicked" />
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ImageUploadGrid from '@/components/common/ImageUploadGrid.vue'
import SimpleRichEditor from '@/components/common/SimpleRichEditor.vue'
import AudienceTargetFields from '@/components/ops/AudienceTargetFields.vue'
import ProductPickDialog from '@/components/content/ProductPickDialog.vue'
import { estimateAudience } from '@/api/ops'
import {
  createTopicForm,
  fetchTopicForEdit,
  fetchTopicTypeOptions,
  saveTopic,
} from '@/api/content'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const pickVisible = ref(false)
const topicTypes = ref([])
const topicForm = reactive(createTopicForm())

const isEdit = computed(() => Boolean(route.params.id))

const rules = {
  typeId: [{ required: true, message: '请选择专题类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入专题标题', trigger: 'blur' }],
}

const onProductsPicked = ({ ids }) => {
  topicForm.productIds = ids
}

const calcEstimate = async () => {
  try {
    const data = await estimateAudience({
      memberLevels: topicForm.memberLevels,
      regions: topicForm.regions,
      tags: topicForm.tags,
    })
    topicForm.estimatedUsers = data.estimatedUsers
  } catch {
    topicForm.estimatedUsers = 0
  }
}

watch(
  () => [topicForm.memberLevels, topicForm.regions],
  () => calcEstimate(),
  { deep: true },
)

const applyDetail = (data) => {
  topicForm.topicCode = data.id
  topicForm.typeId = data.typeId
  topicForm.title = data.title || ''
  topicForm.intro = data.intro || ''
  topicForm.content = data.content || ''
  topicForm.coverImage = data.coverImage || data.images?.[0] || ''
  topicForm.specifyProducts = data.specifyProducts !== false
  topicForm.productIds = data.productIds ? [...data.productIds] : []
  topicForm.memberLevels = data.memberLevels?.length ? [...data.memberLevels] : ['all']
  topicForm.regions = data.regions ? [...data.regions] : []
  topicForm.tags = data.tags ? { ...data.tags } : topicForm.tags
  topicForm.sort = data.sort ?? 0
  topicForm.status = data.status ?? 1
}

const loadMeta = async () => {
  topicTypes.value = await fetchTopicTypeOptions()
}

const loadDetail = async () => {
  if (!isEdit.value) return
  const data = await fetchTopicForEdit(String(route.params.id))
  applyDetail(data)
}

const submitTopic = async () => {
  await formRef.value?.validate().catch(() => {
    ElMessage.warning('请完善必填项')
    return Promise.reject()
  })
  submitting.value = true
  try {
    await saveTopic({
      ...topicForm,
      typeId: Number(topicForm.typeId),
      topicCode: topicForm.topicCode || (isEdit.value ? String(route.params.id) : undefined),
      status: topicForm.status ?? 1,
    })
    ElMessage.success(isEdit.value ? '保存成功' : '提交成功')
    router.push('/content/topic')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await loadMeta()
    await loadDetail()
    calcEstimate()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.topic-edit-page { min-height: calc(100vh - 120px); }
.panel-card { border-radius: 8px; border: none; }
.section-title { font-weight: 600; }
.rich-editor-wrap { width: 100%; max-width: 600px; border: 1px solid #dcdfe6; border-radius: 4px; padding: 8px; }
.product-summary { margin: 0 12px; color: #606266; font-size: 13px; }
.submit-btn { width: 100%; max-width: 400px; height: 44px; margin-right: 12px; }
</style>
