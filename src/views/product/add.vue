<template>
  <div class="add-product">
    <!-- 步骤条 -->
    <el-card shadow="never" class="steps-card">
      <el-steps :active="currentStep" align-center finish-status="success">
        <el-step title="商品信息" />
        <el-step title="商品属性" />
        <el-step title="提交" />
      </el-steps>
    </el-card>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="product-form"
    >
      <div class="form-layout">
        <!-- 左侧基础表单 -->
        <el-card shadow="never" class="form-card">
          <el-form-item label="商品分类" prop="category">
            <el-select v-model="form.category" placeholder="请选择商品分类" style="width: 100%">
              <el-option
                v-for="opt in categoryOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="商品名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入 (限60字)" maxlength="60" show-word-limit />
          </el-form-item>

          <el-form-item label="副标题" prop="subtitle">
            <el-input v-model="form.subtitle" placeholder="请输入 (限10字)" maxlength="10" show-word-limit />
          </el-form-item>

          <el-form-item label="商品品牌" prop="brand">
            <el-select v-model="form.brand" placeholder="请选择品牌" style="width: 100%">
              <el-option
                v-for="opt in brandOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="商品介绍" prop="intro">
            <el-input
              v-model="form.intro"
              type="textarea"
              :rows="4"
              placeholder="请输入 (限300字)"
              maxlength="300"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="运费模板" prop="shipping">
            <el-select v-model="form.shipping" placeholder="请选择运费模板" style="width: 100%">
              <el-option label="包邮模板" value="free" />
              <el-option label="按重量计费" value="weight" />
            </el-select>
          </el-form-item>

          <el-form-item label="商品货号" prop="sku">
            <el-input v-model="form.sku" placeholder="请输入 (限30字)" maxlength="30" />
            <p class="form-hint">如果您不输入商品货号，系统将自动生成一个唯一的货号。</p>
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="售价" prop="price">
                <el-input v-model="form.price" placeholder="请输入售价" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="市场价" prop="marketPrice">
                <el-input v-model="form.marketPrice" placeholder="请输入市场价" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="库存" prop="stock">
                <el-input v-model="form.stock" placeholder="请输入库存" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="库存预警值" prop="stockWarning">
                <el-input v-model="form.stockWarning" placeholder="请输入预警值" />
              </el-form-item>
            </el-col>
          </el-row>
          <p class="form-hint block-hint">
            单规格直接填写；多规格请在下一步「商品属性」中分别设置各规格库存。
          </p>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="计量单位" prop="unit">
                <el-input v-model="form.unit" placeholder="如：件、盒" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="商品重量" prop="weight">
                <el-input v-model="form.weight" placeholder="请输入">
                  <template #append>克/g</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="预售商品">
            <el-switch v-model="form.preSale" />
            <span class="switch-hint">开启后商品在正式开售时间前不可购买</span>
          </el-form-item>

          <el-form-item label="商品上架">
            <el-switch v-model="form.onSale" />
          </el-form-item>

          <el-form-item label="商品推荐">
            <el-checkbox-group v-model="form.recommend">
              <el-checkbox value="new">新品</el-checkbox>
              <el-checkbox value="hot">推荐</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="服务保证">
            <el-checkbox-group v-model="form.services">
              <el-checkbox value="return">无忧退货</el-checkbox>
              <el-checkbox value="refund">快速退款</el-checkbox>
              <el-checkbox value="freeShip">免费包邮</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="商品标签">
            <el-input v-model="form.tags" placeholder="逗号分隔，最多5个" />
          </el-form-item>
        </el-card>

        <!-- 右侧媒体与富文本 -->
        <el-card shadow="never" class="form-card media-card">
          <div class="upload-section">
            <h4 class="section-title">商品主图</h4>
            <div class="upload-grid">
              <div v-for="(img, idx) in form.mainImages" :key="idx" class="upload-item has-image">
                <el-image :src="img" fit="cover" class="upload-preview" />
                <span class="upload-remove" @click="removeImage('mainImages', idx)">×</span>
              </div>
              <div v-if="form.mainImages.length < 5" class="upload-item upload-trigger" @click="mockUpload('mainImages')">
                <el-icon :size="28"><Plus /></el-icon>
                <span>上传</span>
              </div>
            </div>
            <p class="form-hint">只能上传 jpg, png 格式，最多 5 张</p>
          </div>

          <div class="upload-section">
            <h4 class="section-title">白底图</h4>
            <div class="upload-grid">
              <div v-if="form.whiteImage" class="upload-item has-image">
                <el-image :src="form.whiteImage" fit="cover" class="upload-preview" />
                <span class="upload-remove" @click="form.whiteImage = ''">×</span>
              </div>
              <div v-else class="upload-item upload-trigger" @click="mockUpload('whiteImage')">
                <el-icon :size="28"><Plus /></el-icon>
                <span>上传</span>
              </div>
            </div>
            <p class="form-hint">只能上传 jpg, png 格式，最多 1 张</p>
          </div>

          <div class="upload-section">
            <h4 class="section-title">视频</h4>
            <div class="upload-grid">
              <div v-if="form.video" class="upload-item has-image">
                <div class="video-placeholder">
                  <el-icon :size="32"><VideoPlay /></el-icon>
                </div>
                <span class="upload-remove" @click="form.video = ''">×</span>
              </div>
              <div v-else class="upload-item upload-trigger" @click="mockUpload('video')">
                <el-icon :size="28"><Plus /></el-icon>
                <span>上传</span>
              </div>
            </div>
            <p class="form-hint">只能上传 mp4 格式，最多 1 个</p>
          </div>

          <div class="upload-section editor-section">
            <h4 class="section-title">详情图</h4>
            <div class="rich-editor">
              <div class="editor-toolbar">
                <span v-for="tool in editorTools" :key="tool" class="toolbar-btn">{{ tool }}</span>
              </div>
              <el-input
                v-model="form.detail"
                type="textarea"
                :rows="12"
                placeholder="请输入商品详情描述..."
                class="editor-body"
              />
            </div>
          </div>
        </el-card>
      </div>

      <div class="form-footer">
        <el-button type="primary" size="large" @click="handleNext">下一步</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, VideoPlay } from '@element-plus/icons-vue'
import { fetchBrandList, fetchCategoryOptions } from '@/api/product'

const formRef = ref(null)
const categoryOptions = ref([])
const brandOptions = ref([])
const currentStep = ref(0)

const mockThumb =
  'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const form = reactive({
  category: '',
  name: '',
  subtitle: '',
  brand: '',
  intro: '',
  shipping: '',
  sku: '',
  price: '',
  marketPrice: '',
  stock: '',
  stockWarning: '',
  unit: '',
  weight: '',
  preSale: true,
  onSale: false,
  recommend: [],
  services: [],
  tags: '',
  mainImages: [mockThumb],
  whiteImage: mockThumb,
  video: 'mock-video.mp4',
  detail: '',
})

const rules = {
  category: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  brand: [{ required: true, message: '请选择品牌', trigger: 'change' }],
  intro: [{ required: true, message: '请输入商品介绍', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
}

const editorTools = ['B', 'I', 'U', 'S', '左', '中', '右', '列表', '图片', '链接']

onMounted(async () => {
  const [categories, brands] = await Promise.all([
    fetchCategoryOptions(),
    fetchBrandList({ page: 1, pageSize: 100, status: 'active' }),
  ])
  categoryOptions.value = categories
  brandOptions.value = brands.list.map((b) => ({ label: b.name, value: b.id }))
})

const mockUpload = (field) => {
  if (field === 'mainImages') {
    if (form.mainImages.length < 5) form.mainImages.push(mockThumb)
  } else if (field === 'whiteImage') {
    form.whiteImage = mockThumb
  } else if (field === 'video') {
    form.video = 'mock-video.mp4'
  }
  ElMessage.success('上传成功（模拟）')
}

const removeImage = (field, idx) => {
  if (field === 'mainImages') form.mainImages.splice(idx, 1)
}

const handleNext = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  currentStep.value = 1
  ElMessage.success('商品信息校验通过，进入下一步')
}
</script>

<style scoped>
.add-product {
  min-height: calc(100vh - 120px);
}

.steps-card {
  margin-bottom: 16px;
  border-radius: 8px;
  border: none;
}

.steps-card :deep(.el-card__body) {
  padding: 24px 40px;
}

.form-layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.form-card {
  flex: 1;
  border-radius: 8px;
  border: none;
}

.form-card :deep(.el-card__body) {
  padding: 24px;
}

.media-card {
  flex: 1;
  max-width: 520px;
}

.form-hint {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.block-hint {
  margin: -8px 0 16px 100px;
}

.switch-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

.upload-section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.upload-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.upload-item {
  position: relative;
  width: 100px;
  height: 100px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #909399;
  font-size: 12px;
  cursor: pointer;
  background: #fafafa;
  transition: border-color 0.2s;
}

.upload-trigger:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-preview {
  width: 100%;
  height: 100%;
}

.upload-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f56c6c;
  color: #fff;
  border-radius: 50%;
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
}

.video-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #303133;
  color: #fff;
}

.rich-editor {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.toolbar-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  padding: 0 6px;
  font-size: 12px;
  color: #606266;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: default;
}

.editor-body :deep(.el-textarea__inner) {
  border: none;
  box-shadow: none;
  border-radius: 0;
}

.form-footer {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-bottom: 24px;
}

@media (max-width: 1200px) {
  .form-layout {
    flex-direction: column;
  }

  .media-card {
    max-width: none;
  }
}
</style>
