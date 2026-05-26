<template>
  <div v-loading="pageLoading" class="add-product">
    <div class="page-head">
      <h2 class="page-title">{{ pageTitle }}</h2>
      <p v-if="isEdit" class="page-sub">商品编号：{{ productId }}</p>
    </div>

    <el-card shadow="never" class="steps-card">
      <el-steps :active="currentStep" align-center finish-status="success">
        <el-step title="商品信息" />
        <el-step title="商品属性" />
        <el-step title="提交保存" />
      </el-steps>
    </el-card>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="product-form"
    >
      <!-- Step 1：基础信息 -->
      <div v-show="currentStep === 0" class="form-layout">
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

          <el-form-item label="配送区域">
            <AreaCascader
              v-model="form.deliveryRegions"
              multiple
              placeholder="不选则全国可配送；可多选省/市/区"
            />
            <p class="form-hint">
              限制商品可配送范围。使用国标区划懒加载，与订单地址、运营圈选共用同一数据源。
            </p>
          </el-form-item>

          <el-form-item label="商品货号" prop="sku">
            <el-input v-model="form.sku" placeholder="请输入 (限30字)" maxlength="30" />
            <p class="form-hint">留空时系统将按商品编号自动生成货号。</p>
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

        <el-card shadow="never" class="form-card media-card">
          <div class="upload-section">
            <h4 class="section-title">商品主图</h4>
            <div class="upload-grid">
              <div v-for="(img, idx) in form.mainImages" :key="idx" class="upload-item has-image">
                <el-image :src="img" fit="cover" class="upload-preview" />
                <span class="upload-remove" @click="removeMainImage(idx)">×</span>
              </div>
              <div
                v-if="form.mainImages.length < 5"
                class="upload-item upload-trigger"
                @click="mockUpload('mainImages')"
              >
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
          </div>

          <div class="upload-section editor-section">
            <h4 class="section-title">详情描述</h4>
            <el-input
              v-model="form.detail"
              type="textarea"
              :rows="10"
              placeholder="请输入商品详情描述..."
            />
          </div>
        </el-card>
      </div>

      <!-- Step 2：属性（占位，后续扩展多规格） -->
      <el-card v-show="currentStep === 1" shadow="never" class="form-card step-card">
        <el-empty description="多规格 SKU 将在后续版本支持，当前使用单规格库存（上一步已填写）" />
      </el-card>

      <!-- Step 3：确认 -->
      <el-card v-show="currentStep === 2" shadow="never" class="form-card step-card">
        <el-descriptions title="请确认商品信息" :column="2" border>
          <el-descriptions-item label="商品名称">{{ form.name }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ categoryLabel }}</el-descriptions-item>
          <el-descriptions-item label="品牌">{{ brandLabel }}</el-descriptions-item>
          <el-descriptions-item label="售价">¥{{ form.price }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ form.stock }}</el-descriptions-item>
          <el-descriptions-item label="上架">{{ form.onSale ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="货号">{{ form.sku || '自动生成' }}</el-descriptions-item>
          <el-descriptions-item label="配送区域">
            {{ form.deliveryRegions?.length ? `已选 ${form.deliveryRegions.length} 个地区` : '全国' }}
          </el-descriptions-item>
          <el-descriptions-item label="主图数量">{{ form.mainImages.length }} 张</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <div class="form-footer">
        <el-button v-if="currentStep > 0" size="large" @click="prevStep">上一步</el-button>
        <el-button v-if="currentStep < 2" type="primary" size="large" @click="nextStep">下一步</el-button>
        <el-button
          v-else
          type="primary"
          size="large"
          :loading="submitting"
          @click="submit"
        >
          {{ isEdit ? '保存修改' : '提交保存' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { Plus, VideoPlay } from '@element-plus/icons-vue'
import AreaCascader from '@/components/AreaCascader/index.vue'
import { useProductEditor } from '@/composables/useProductEditor'

const {
  formRef,
  form,
  rules,
  currentStep,
  pageLoading,
  submitting,
  categoryOptions,
  brandOptions,
  isEdit,
  pageTitle,
  productId,
  init,
  nextStep,
  prevStep,
  submit,
  mockUpload,
  removeMainImage,
} = useProductEditor()

const categoryLabel = computed(
  () => categoryOptions.value.find((o) => o.value === form.category)?.label || form.category,
)

const brandLabel = computed(
  () => brandOptions.value.find((o) => o.value === form.brand)?.label || form.brand,
)

onMounted(init)
</script>

<style scoped>
.add-product {
  min-height: calc(100vh - 120px);
}

.page-head {
  margin-bottom: 12px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.page-sub {
  margin: 4px 0 0;
  font-size: 13px;
  color: #909399;
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

.step-card {
  margin-bottom: 16px;
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

.form-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
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
