<template>
  <div v-loading="loading" class="promo-page">
    <el-card shadow="never" class="panel-card">
      <div class="section-header">
        <span class="section-title">设置商品 - {{ timeName }}</span>
        <div>
          <el-button @click="router.back()">返回</el-button>
          <el-button type="primary" @click="openPicker">+ 添加商品</el-button>
          <el-button type="success" :loading="saving" @click="submitSku">保存配货</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table :data="form.items" border stripe>
        <el-table-column prop="name" label="商品名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="productCode" label="商品编号" width="110" />
        <el-table-column label="原价" width="90" align="center">
          <template #default="{ row }">{{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="团购价" width="110" align="center">
          <template #default="{ row }"><el-input-number v-model="row.groupPrice" :min="0" :precision="2" size="small" /></template>
        </el-table-column>
        <el-table-column label="成团人数" width="100" align="center">
          <template #default="{ row }"><el-input-number v-model="row.groupSize" :min="2" size="small" /></template>
        </el-table-column>
        <el-table-column label="活动库存" width="100" align="center">
          <template #default="{ row }"><el-input-number v-model="row.groupQty" :min="0" size="small" /></template>
        </el-table-column>
        <el-table-column label="剩余库存" width="90" align="center">
          <template #default="{ row }">
            <span :class="{ 'stock-warning': row.remainStock <= row.warningStock }">{{ row.remainStock }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row, $index }">
            <el-button type="primary" link @click="openAttrs(row)">设置属性</el-button>
            <el-button type="danger" link @click="form.items.splice($index, 1)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="picker.visible" title="添加商品" width="800px" destroy-on-close>
      <el-form :inline="true">
        <el-form-item label="商品"><el-input v-model="picker.keyword" clearable style="width: 200px" @keyup.enter="loadPicker" /></el-form-item>
        <el-button type="primary" @click="loadPicker">查询</el-button>
      </el-form>
      <el-table :data="pickerList" border max-height="360">
        <el-table-column prop="name" label="商品名称" min-width="200" />
        <el-table-column prop="id" label="编号" width="110" />
        <el-table-column label="价格" width="90"><template #default="{ row }">{{ Number(row.price).toFixed(2) }}</template></el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button type="primary" link :disabled="picker.selectedIds.includes(row.id)" @click="picker.selectedIds.push(row.id)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="picker.visible = false">关闭</el-button>
        <el-button type="primary" @click="confirmPicker">添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="attrsDialog.visible" title="设置属性" width="480px">
      <el-form label-width="100px">
        <el-form-item label="规格说明"><el-input v-model="attrsDialog.spec" placeholder="如：蓝色 / XL" /></el-form-item>
        <el-form-item label="团长价"><el-input-number v-model="attrsDialog.leaderPrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="团员价"><el-input-number v-model="attrsDialog.memberPrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="attrsDialog.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="attrsDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveAttrs">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchGroupBuySkuList, fetchProductPicker, saveGroupBuySku } from '@/api/promotion'

const route = useRoute()
const router = useRouter()
const activityId = route.params.activityId
const timeId = route.params.timeId
const timeName = computed(() => route.query.timeName || '团购时段')

const loading = ref(false)
const saving = ref(false)
const form = reactive({ items: [] })
const picker = reactive({ visible: false, keyword: '', selectedIds: [] })
const storeProducts = ref([])
const attrsDialog = reactive({ visible: false, row: null, spec: '', leaderPrice: 0, memberPrice: 0, remark: '' })

const pickerList = computed(() => storeProducts.value.filter((p) => !picker.keyword || p.name.includes(picker.keyword) || p.id.includes(picker.keyword)))

const fetchSkuList = async () => {
  loading.value = true
  try {
    form.items = await fetchGroupBuySkuList(activityId, timeId)
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

const confirmPicker = () => {
  let n = 0
  picker.selectedIds.forEach((pid) => {
    if (form.items.some((i) => i.productCode === pid)) return
    const p = storeProducts.value.find((x) => x.id === pid)
    if (!p) return
    form.items.push({
      name: p.name,
      productCode: p.id,
      price: p.price,
      groupPrice: Number(p.price) * 0.85,
      groupSize: 2,
      groupQty: 200,
      remainStock: 200,
      totalStock: p.stock,
      warningStock: 50,
      limitQty: 1,
      sort: form.items.length + 1,
      attrs: {},
    })
    n++
  })
  picker.visible = false
  picker.selectedIds = []
  ElMessage.success(`已添加 ${n} 件商品`)
}

const openAttrs = (row) => {
  attrsDialog.row = row
  const a = row.attrs || {}
  attrsDialog.spec = a.spec || ''
  attrsDialog.leaderPrice = a.leaderPrice ?? row.groupPrice
  attrsDialog.memberPrice = a.memberPrice ?? row.groupPrice
  attrsDialog.remark = a.remark || ''
  attrsDialog.visible = true
}

const saveAttrs = () => {
  if (attrsDialog.row) {
    attrsDialog.row.attrs = {
      spec: attrsDialog.spec,
      leaderPrice: attrsDialog.leaderPrice,
      memberPrice: attrsDialog.memberPrice,
      remark: attrsDialog.remark,
    }
  }
  attrsDialog.visible = false
  ElMessage.success('属性已保存')
}

const submitSku = async () => {
  saving.value = true
  try {
    await saveGroupBuySku({ activityId, timeId, items: form.items })
    ElMessage.success('保存成功')
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
</style>
