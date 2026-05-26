<template>
  <div v-loading="loading" class="promo-page">
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="优惠券">
          <el-input v-model="searchForm.keyword" placeholder="请输入名称或编号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择优惠券分类" clearable style="width: 160px">
            <el-option v-for="opt in couponTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <div class="toolbar">
        <el-checkbox>全选</el-checkbox>
        <el-tabs v-model="activeTab" @tab-change="onTabChange">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane label="待开始" name="pending" />
          <el-tab-pane label="进行中" name="active" />
          <el-tab-pane label="已结束" name="ended" />
        </el-tabs>
        <el-button type="primary" @click="openDialog()">+ 添加优惠券</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card table-card">
      <el-table :data="tableData" border stripe>
        <el-table-column type="selection" width="48" align="center" fixed="left" />
        <el-table-column prop="id" label="活动编号" width="90" align="center" fixed="left" />
        <el-table-column prop="name" label="优惠券名称" width="130" fixed="left" />
        <el-table-column prop="typeLabel" label="类型" width="90" />
        <el-table-column prop="products" label="可使用商品" width="100" />
        <el-table-column prop="threshold" label="使用门槛" width="110" />
        <el-table-column label="面值" width="80" align="center">
          <template #default="{ row }">{{ Number(row.faceValue).toFixed(2) }}元</template>
        </el-table-column>
        <el-table-column prop="issueQty" label="发行量" width="80" align="center" />
        <el-table-column prop="claimed" label="领取量" width="80" align="center" />
        <el-table-column prop="used" label="使用量" width="80" align="center" />
        <el-table-column prop="platform" label="适用平台" width="90" />
        <el-table-column prop="validity" label="有效期" width="80" />
        <el-table-column prop="timeRange" label="开始结束时间" width="150" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <span :class="row.status === 'ended' ? 'text-danger' : ''">{{ row.statusLabel }}</span>
          </template>
        </el-table-column>
        <el-table-column label="上线/下架" width="90" align="center">
          <template #default="{ row }">
            <el-button v-if="row.online" type="danger" link size="small" @click="toggleOnline(row, false)">下架</el-button>
            <el-button v-else type="success" link size="small" @click="toggleOnline(row, true)">上线</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row)">明细</el-button>
            <el-button type="success" link @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <span>第{{ pagination.page }}页 共{{ pagination.totalPages }}页 {{ pagination.total }}条</span>
        <el-pagination v-model:current-page="pagination.page" :total="pagination.total" layout="prev, pager, next, sizes" background @current-change="fetchList" />
      </div>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑优惠券' : '添加优惠券'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="dialog.form" :rules="dialogRules" label-width="110px">
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="dialog.form.name" maxlength="60" />
        </el-form-item>
        <el-form-item label="优惠券类型" prop="type">
          <el-select v-model="dialog.form.type" style="width: 100%">
            <el-option v-for="opt in couponTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="使用范围" prop="scopeType">
          <el-radio-group v-model="dialog.form.scopeType">
            <el-radio v-for="opt in couponScopeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="dialog.form.scopeType === 'product'" label="选择商品">
          <el-button type="primary" link @click="productPicker.visible = true">选择商品（已选 {{ dialog.form.productIds.length }}）</el-button>
        </el-form-item>
        <el-form-item v-if="dialog.form.scopeType === 'category'" label="选择分类">
          <el-select v-model="dialog.form.categoryIds" multiple placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="满减门槛">
          <span>满</span>
          <el-input-number v-model="dialog.form.thresholdAmount" :min="0" :precision="2" style="margin: 0 8px" />
          <span>减</span>
          <el-input-number v-model="dialog.form.faceValue" :min="0" :precision="2" style="margin: 0 8px" />
          <span>元</span>
        </el-form-item>
        <el-form-item label="发行量">
          <el-input-number v-model="dialog.form.issueQty" :min="-1" />
          <span class="hint">-1 表示不限量</span>
        </el-form-item>
        <el-form-item label="适用平台">
          <el-select v-model="dialog.form.platform" style="width: 100%">
            <el-option label="APP" value="APP" />
            <el-option label="小程序" value="小程序" />
            <el-option label="全平台" value="全平台" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效天数">
          <el-input-number v-model="dialog.form.validityDays" :min="1" />
        </el-form-item>
        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker v-model="dialog.form.timeRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="上线">
          <el-switch v-model="dialog.form.online" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.saving" @click="submitCoupon">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="productPicker.visible" title="选择商品" width="720px">
      <el-input v-model="productPicker.keyword" placeholder="搜索商品" clearable style="margin-bottom: 12px" @keyup.enter="loadProducts" />
      <el-button type="primary" size="small" @click="loadProducts">查询</el-button>
      <el-table :data="productPicker.list" border max-height="320" style="margin-top: 12px">
        <el-table-column prop="name" label="商品" min-width="200" />
        <el-table-column prop="id" label="编号" width="110" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button type="primary" link :disabled="dialog.form.productIds.includes(row.id)" @click="dialog.form.productIds.push(row.id)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="productPicker.visible = false">完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchCategoryOptions } from '@/api/product'
import { toPickerDatetime } from '@/utils/promoDatetime'
import {
  couponScopeOptions,
  couponTypeOptions,
  deleteCoupon,
  fetchCouponDetail,
  fetchCouponList as getCouponListApi,
  fetchProductPicker,
  saveCoupon,
  toggleCouponOnline,
} from '@/api/promotion'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('all')
const tableData = ref([])
const formRef = ref(null)
const categoryOptions = ref([])

const searchForm = reactive({ keyword: '', type: '' })
const pagination = reactive({ page: 1, total: 0, totalPages: 0 })

const dialog = reactive({
  visible: false,
  saving: false,
  isEdit: false,
  form: {
    id: '',
    name: '',
    type: 'newcomer',
    scopeType: 'all',
    productIds: [],
    categoryIds: [],
    thresholdAmount: 20,
    faceValue: 10,
    issueQty: -1,
    platform: 'APP',
    validityDays: 15,
    timeRange: [],
    online: true,
  },
})

const productPicker = reactive({ visible: false, keyword: '', list: [] })

const dialogRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  scopeType: [{ required: true, message: '请选择范围', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change', type: 'array', min: 2 }],
}

const fetchList = async () => {
  loading.value = true
  try {
    const data = await getCouponListApi({
      keyword: searchForm.keyword || undefined,
      type: searchForm.type || undefined,
      tab: activeTab.value,
      page: pagination.page,
      pageSize: 10,
    })
    tableData.value = data.list
    pagination.total = data.total
    pagination.totalPages = data.totalPages
  } finally {
    loading.value = false
  }
}

const onTabChange = () => { pagination.page = 1; fetchList() }
const handleSearch = () => { pagination.page = 1; fetchList(); ElMessage.success('查询成功') }
const handleReset = () => { searchForm.keyword = ''; searchForm.type = ''; pagination.page = 1; fetchList() }
const goDetail = (row) => router.push(`/promotion/coupon/${row.id}/detail`)

const openDialog = async (row) => {
  dialog.isEdit = !!row
  if (row) {
    const d = await fetchCouponDetail(row.id)
    Object.assign(dialog.form, {
      id: d.id,
      name: d.name,
      type: d.couponType || row.type,
      scopeType: d.scopeType,
      productIds: d.productIds || [],
      categoryIds: d.categoryIds || [],
      thresholdAmount: d.thresholdAmount ?? 0,
      faceValue: d.faceValue,
      issueQty: d.issueQty ?? -1,
      platform: d.platform || row.platform,
      validityDays: parseInt(d.validity) || 15,
      timeRange: d.startTime && d.endTime ? [toPickerDatetime(d.startTime), toPickerDatetime(d.endTime)] : [],
      online: d.online,
    })
  } else {
    Object.assign(dialog.form, {
      id: '', name: '', type: 'newcomer', scopeType: 'all', productIds: [], categoryIds: [],
      thresholdAmount: 20, faceValue: 10, issueQty: -1, platform: 'APP', validityDays: 15, timeRange: [], online: true,
    })
  }
  dialog.visible = true
}

const loadProducts = async () => {
  productPicker.list = await fetchProductPicker(productPicker.keyword || undefined)
}

const submitCoupon = async () => {
  if (!(await formRef.value?.validate().catch(() => false))) return
  if (dialog.form.scopeType === 'product' && !dialog.form.productIds.length) {
    ElMessage.warning('请选择商品')
    return
  }
  if (dialog.form.scopeType === 'category' && !dialog.form.categoryIds.length) {
    ElMessage.warning('请选择分类')
    return
  }
  dialog.saving = true
  try {
    const wasNew = !dialog.isEdit
    const [startTime, endTime] = dialog.form.timeRange || []
    const code = await saveCoupon({
      id: dialog.form.id || undefined,
      name: dialog.form.name,
      type: dialog.form.type,
      scopeType: dialog.form.scopeType,
      productIds: dialog.form.productIds,
      categoryIds: dialog.form.categoryIds,
      thresholdAmount: dialog.form.thresholdAmount,
      faceValue: dialog.form.faceValue,
      issueQty: dialog.form.issueQty,
      platform: dialog.form.platform,
      validityDays: dialog.form.validityDays,
      startTime,
      endTime,
      online: dialog.form.online,
    })
    ElMessage.success(wasNew ? `优惠券已创建，编号 ${code}` : '保存成功')
    dialog.visible = false
    await fetchList()
    if (wasNew && code) {
      router.push(`/promotion/coupon/${code}/detail`)
    }
  } finally {
    dialog.saving = false
  }
}

const toggleOnline = async (row, online) => {
  await toggleCouponOnline(row.id, online)
  ElMessage.success(online ? '已上线' : '已下架')
  fetchList()
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => { await deleteCoupon(row.id); ElMessage.success('删除成功'); fetchList() })
    .catch(() => {})
}

onMounted(async () => {
  categoryOptions.value = await fetchCategoryOptions()
  fetchList()
})
</script>

<style scoped>
.promo-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.table-card { overflow-x: auto; }
.search-form { display: flex; flex-wrap: wrap; }
.search-actions { margin-left: auto; }
.toolbar { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
.toolbar :deep(.el-tabs__header) { margin-bottom: 0; flex: 1; }
.text-danger { color: #f56c6c; }
.pagination-bar { display: flex; justify-content: space-between; margin-top: 16px; font-size: 13px; color: #606266; }
.hint { margin-left: 8px; font-size: 12px; color: #909399; }
</style>
