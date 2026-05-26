<template>
  <div v-loading="loading" class="promo-page">
    <el-card shadow="never" class="panel-card">
      <div class="section-header">
        <span class="section-title">设置商品 - {{ timeName }}</span>
        <div>
          <el-button @click="router.back()">返回</el-button>
          <el-button type="primary" @click="openPicker">+ 添加商品</el-button>
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
          <template #default="{ row }">{{ Number(row.price).toFixed(2) }}元</template>
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
    </el-card>

    <el-dialog v-model="picker.visible" title="添加商品" width="800px" destroy-on-close>
      <div class="picker-search">
        <el-form :inline="true">
          <el-form-item label="商品">
            <el-input v-model="picker.keyword" placeholder="请输入名称或编号" clearable style="width: 200px" @keyup.enter="loadPicker" />
          </el-form-item>
          <el-button type="primary" @click="loadPicker">查询</el-button>
          <span class="picker-count">当前已选择 {{ picker.selectedIds.length }} 件商品</span>
        </el-form>
      </div>
      <el-table :data="pickerList" border :row-class-name="pickerRowClass" max-height="360">
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="id" label="商品编号" width="110" />
        <el-table-column label="商品价格" width="100" align="center">
          <template #default="{ row }">{{ Number(row.price).toFixed(2) }}元</template>
        </el-table-column>
        <el-table-column prop="stock" label="剩余库存" width="100" align="center" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button v-if="!picker.selectedIds.includes(row.id)" type="primary" link @click="selectProduct(row)">选择</el-button>
            <el-button v-else type="success" link disabled>已选择</el-button>
          </template>
        </el-table-column>
      </el-table>
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
import { fetchProductPicker, fetchSeckillSkuList, saveSeckillSku } from '@/api/promotion'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)

const activityId = route.params.activityId
const timeId = route.params.timeId
const timeName = computed(() => route.query.timeName || '秒杀时段')

const seckillSkuForm = reactive({ activityId, timeId, items: [] })
const picker = reactive({ visible: false, keyword: '', selectedIds: [] })
const storeProducts = ref([])

const pickerList = computed(() => {
  let list = storeProducts.value
  if (picker.keyword) {
    list = list.filter((p) => p.name.includes(picker.keyword) || p.id.includes(picker.keyword))
  }
  return list
})

const isLowStock = (row) => row.remainStock <= row.warningStock
const pickerRowClass = ({ row }) => (picker.selectedIds.includes(row.id) ? 'row-selected' : '')

const fetchSkuList = async () => {
  loading.value = true
  try {
    seckillSkuForm.items = await fetchSeckillSkuList(activityId, timeId)
  } finally {
    loading.value = false
  }
}

const openPicker = async () => {
  picker.visible = true
  picker.selectedIds = []
  await loadPicker()
}

const loadPicker = async () => {
  storeProducts.value = await fetchProductPicker(picker.keyword || undefined)
}

const selectProduct = (row) => {
  if (!picker.selectedIds.includes(row.id)) picker.selectedIds.push(row.id)
}

const confirmPicker = () => {
  let added = 0
  picker.selectedIds.forEach((pid) => {
    if (seckillSkuForm.items.some((i) => i.productCode === pid)) return
    const p = storeProducts.value.find((x) => x.id === pid)
    if (!p) return
    seckillSkuForm.items.push({
      id: `SKU${Date.now()}${added}`,
      name: p.name,
      productCode: p.id,
      price: p.price,
      seckillPrice: Number(p.price) * 0.8,
      seckillQty: 500,
      remainStock: 500,
      totalStock: p.stock,
      warningStock: 100,
      limitQty: 2,
      sort: seckillSkuForm.items.length + 1,
    })
    added++
  })
  picker.visible = false
  ElMessage.success(`已添加 ${added} 件商品`)
  picker.selectedIds = []
}

const removeItem = (index) => seckillSkuForm.items.splice(index, 1)

const submitSeckillSku = async () => {
  saving.value = true
  try {
    await saveSeckillSku({
      activityId: seckillSkuForm.activityId,
      timeId: seckillSkuForm.timeId,
      items: seckillSkuForm.items,
    })
    ElMessage.success('配货保存成功')
    fetchSkuList()
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
.picker-search { margin-bottom: 12px; }
.picker-count { margin-left: 16px; font-size: 13px; color: #606266; }
:deep(.row-selected) { background-color: #f0f9eb !important; }
</style>
