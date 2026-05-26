<template>
  <div v-loading="loading" class="promo-page">
    <el-card shadow="never" class="panel-card">
      <div class="section-header">
        <span class="section-title">设置商品 - {{ timeName }}</span>
        <div>
          <el-button @click="router.back()">返回</el-button>
          <el-button type="primary" @click="picker.visible = true">+ 添加商品</el-button>
          <el-button type="success" :loading="saving" @click="submitSeckillSku">保存配货</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table :data="seckillSkuForm.items" border stripe>
        <el-table-column prop="id" label="编号" width="90" align="center" />
        <el-table-column prop="name" label="商品名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="productCode" label="商品编号" width="110" />
        <el-table-column label="商品价格" width="100" align="center">
          <template #default="{ row }">{{ row.price.toFixed(2) }}元</template>
        </el-table-column>
        <el-table-column label="秒杀价格" width="120" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.seckillPrice" :min="0" :precision="2" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="秒杀数量" width="120" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.seckillQty" :min="0" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="剩余活动库存" width="120" align="center">
          <template #default="{ row }">
            <div :class="{ 'stock-warning': isLowStock(row) }">
              <span>{{ row.remainStock }}</span>
              <p v-if="isLowStock(row)" class="warning-tip">低于预警值</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalStock" label="商品库存" width="100" align="center" />
        <el-table-column label="活动库存预警值" width="130" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.warningStock" :min="0" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="限购数量" width="110" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.limitQty" :min="1" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="排序" width="90" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.sort" :min="0" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template #default="{ $index }">
            <el-button type="danger" link @click="removeItem($index)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <span>第1页 共10页 {{ seckillSkuForm.items.length }}条</span>
        <el-pagination :total="265" layout="prev, pager, next, sizes" background />
      </div>
    </el-card>

    <!-- 添加商品弹窗 -->
    <el-dialog v-model="picker.visible" title="添加商品" width="800px" destroy-on-close>
      <div class="picker-search">
        <el-form :inline="true">
          <el-form-item label="商品">
            <el-input v-model="picker.keyword" placeholder="请输入名称或编号" clearable style="width: 200px" />
          </el-form-item>
          <el-button type="primary" @click="filterProducts">查询</el-button>
          <el-button @click="picker.keyword = ''; filterProducts()">重置</el-button>
          <span class="picker-count">当前已选择 {{ picker.selectedIds.length }} 件商品</span>
        </el-form>
        <el-tabs v-model="picker.tab">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane :label="`已选择 (${picker.selectedIds.length})`" name="selected" />
        </el-tabs>
      </div>
      <el-table :data="pickerList" border :row-class-name="pickerRowClass" max-height="360">
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="id" label="商品编号" width="110" />
        <el-table-column label="商品价格" width="100" align="center">
          <template #default="{ row }">{{ row.price.toFixed(2) }}元</template>
        </el-table-column>
        <el-table-column prop="stock" label="剩余库存" width="100" align="center" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button
              v-if="!picker.selectedIds.includes(row.id)"
              type="primary"
              link
              @click="selectProduct(row)"
            >
              选择
            </el-button>
            <el-button v-else type="success" link disabled>已选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="picker-page" :total="mockStoreProducts.length" layout="prev, pager, next" small />
      <template #footer>
        <el-button @click="picker.visible = false">关闭</el-button>
        <el-button type="primary" @click="confirmPicker">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { mockSeckillSkus, mockStoreProducts } from '@/mock/promotion'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)

const activityId = route.params.activityId
const timeId = route.params.timeId
const timeName = computed(() => route.query.timeName || '秒杀时段')

const seckillSkuForm = reactive({
  activityId,
  timeId,
  items: [],
})

const picker = reactive({
  visible: false,
  keyword: '',
  tab: 'all',
  selectedIds: [],
})

const pickerList = computed(() => {
  let list = [...mockStoreProducts]
  if (picker.keyword) {
    list = list.filter((p) => p.name.includes(picker.keyword) || p.id.includes(picker.keyword))
  }
  if (picker.tab === 'selected') {
    list = list.filter((p) => picker.selectedIds.includes(p.id))
  }
  return list
})

const isLowStock = (row) => row.remainStock <= row.warningStock

const pickerRowClass = ({ row }) => (picker.selectedIds.includes(row.id) ? 'row-selected' : '')

const fetchSkuList = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 400))
    seckillSkuForm.items = mockSeckillSkus.map((i) => ({ ...i }))
  } finally {
    loading.value = false
  }
}

const filterProducts = () => {}

const selectProduct = (row) => {
  if (!picker.selectedIds.includes(row.id)) picker.selectedIds.push(row.id)
}

const confirmPicker = () => {
  picker.selectedIds.forEach((pid) => {
    if (seckillSkuForm.items.some((i) => i.productCode === pid)) return
    const p = mockStoreProducts.find((x) => x.id === pid)
    if (!p) return
    seckillSkuForm.items.push({
      id: `SKU${Date.now()}`,
      name: p.name,
      productCode: p.id,
      price: p.price,
      seckillPrice: p.price * 0.8,
      seckillQty: 500,
      remainStock: 500,
      totalStock: p.stock,
      warningStock: 100,
      limitQty: 2,
      sort: seckillSkuForm.items.length + 1,
    })
  })
  picker.visible = false
  ElMessage.success(`已添加 ${picker.selectedIds.length} 件商品`)
  picker.selectedIds = []
}

const removeItem = (index) => seckillSkuForm.items.splice(index, 1)

/**
 * POST /api/promotion/seckill/sku/save
 * 联调时后端需在分布式锁或 Redis 扣减库存层面与此处字段对齐
 */
const submitSeckillSku = async () => {
  saving.value = true
  try {
    // await axios.post('/api/promotion/seckill/sku/save', seckillSkuForm)
    await new Promise((r) => setTimeout(r, 500))
    ElMessage.success('配货保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(fetchSkuList)
</script>

<style scoped>
.promo-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.section-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.section-title::before { content: ''; display: inline-block; width: 4px; height: 14px; margin-right: 8px; background: #409eff; border-radius: 2px; vertical-align: middle; }
.stock-warning { color: #f56c6c; font-weight: 600; }
.warning-tip { margin: 2px 0 0; font-size: 11px; color: #f56c6c; }
.pagination-bar { display: flex; justify-content: space-between; margin-top: 16px; font-size: 13px; color: #606266; }
.picker-search { margin-bottom: 12px; }
.picker-count { margin-left: 16px; font-size: 13px; color: #606266; }
.picker-page { margin-top: 12px; justify-content: flex-end; }
:deep(.row-selected) { background-color: #f0f9eb !important; }
</style>
