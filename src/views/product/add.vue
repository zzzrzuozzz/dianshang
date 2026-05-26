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
            <el-select
              v-model="form.shipping"
              placeholder="请选择运费模板"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="opt in shippingOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
            <p v-if="!shippingOptions.length" class="form-hint">
              暂无模板，请先在「订单 → 订单设置 → 快递单模板」中添加
            </p>
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
            <ImageUploadGrid
              v-model="form.mainImages"
              :max="5"
              biz="product"
              hint="只能上传 jpg、png 格式，最多 5 张"
            />
          </div>

          <div class="upload-section">
            <h4 class="section-title">白底图</h4>
            <SingleImageUpload v-model="form.whiteImage" biz="product" />
          </div>

          <div class="upload-section">
            <h4 class="section-title">视频</h4>
            <VideoUpload v-model="form.video" biz="product" />
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

      <!-- Step 2：多规格 SKU -->
      <el-card v-show="currentStep === 1" shadow="never" class="form-card step-card">
        <div class="sku-toolbar">
          <p class="form-hint">主规格与第一步信息同步；可添加扩展规格（如颜色、尺码），保存后同步至库存。</p>
          <el-button type="primary" plain @click="addSkuRow">添加规格</el-button>
        </div>
        <el-table :data="form.skuRows" border stripe class="sku-table">
          <el-table-column label="类型" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isPrimary ? 'primary' : 'info'" size="small">
                {{ row.isPrimary ? '主规格' : '扩展' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="规格名称" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.skuName" placeholder="如：红色 / XL" maxlength="30" />
            </template>
          </el-table-column>
          <el-table-column label="货号" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.skuCode" placeholder="留空自动生成" maxlength="30" />
            </template>
          </el-table-column>
          <el-table-column label="库存" width="120">
            <template #default="{ row }">
              <el-input v-model="row.stock" placeholder="0" />
            </template>
          </el-table-column>
          <el-table-column label="预警值" width="120">
            <template #default="{ row }">
              <el-input v-model="row.stockWarning" placeholder="0" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ $index, row }">
              <el-button
                v-if="!row.isPrimary"
                type="danger"
                link
                @click="removeSkuRow($index)"
              >
                删除
              </el-button>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
        </el-table>
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
          <el-descriptions-item label="规格数量">{{ form.skuRows.length }} 个</el-descriptions-item>
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
import AreaCascader from '@/components/AreaCascader/index.vue'
import ImageUploadGrid from '@/components/common/ImageUploadGrid.vue'
import SingleImageUpload from '@/components/common/SingleImageUpload.vue'
import VideoUpload from '@/components/common/VideoUpload.vue'
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
  shippingOptions,
  isEdit,
  pageTitle,
  productId,
  init,
  nextStep,
  prevStep,
  submit,
  addSkuRow,
  removeSkuRow,
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

.sku-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.sku-table {
  width: 100%;
}

.text-muted {
  color: #c0c4cc;
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
