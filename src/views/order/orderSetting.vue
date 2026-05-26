<template>
  <div v-loading="loading" class="order-setting-page">
    <el-card shadow="never" class="panel-card">
      <div class="setting-header">
        <el-tabs v-model="activeSettingTab" class="setting-tabs" @tab-change="onTabChange">
          <el-tab-pane label="退货原因设置" name="returnReason" />
          <el-tab-pane label="快递单模板" name="expressTemplate" />
          <el-tab-pane label="发货地址" name="shipAddress" />
          <el-tab-pane label="退货地址" name="returnAddress" />
        </el-tabs>
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          {{ addButtonText }}
        </el-button>
      </div>
    </el-card>

    <!-- 退货原因 -->
    <el-card v-if="activeSettingTab === 'returnReason'" shadow="never" class="panel-card">
      <el-table :data="settingData.list" border stripe>
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="编号" width="110" align="center" />
        <el-table-column prop="reason" label="原因类型" min-width="200" />
        <el-table-column prop="addTime" label="添加时间" width="160" />
        <el-table-column label="是否显示" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.visible ? 'visible-yes' : 'visible-no'">
              {{ row.visible ? '显示' : '隐藏' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" class="btn-blue" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" class="btn-red" @click="handleDelete(row)">删除</el-button>
            <el-button
              size="small"
              :class="row.visible ? 'btn-red' : 'btn-green-solid'"
              @click="toggleVisible(row)"
            >
              {{ row.visible ? '隐藏' : '显示' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 快递单模板 -->
    <el-card v-else-if="activeSettingTab === 'expressTemplate'" shadow="never" class="panel-card">
      <el-table :data="settingData.list" border stripe>
        <el-table-column prop="id" label="编号" width="100" align="center" />
        <el-table-column prop="templateName" label="模板名称" min-width="140" />
        <el-table-column prop="expressCompany" label="快递公司" width="120" />
        <el-table-column prop="templateSpec" label="模板规格" width="120" />
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="是否默认" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
            <el-button v-else link type="primary" size="small" @click="setDefault(row)">设为默认</el-button>
          </template>
        </el-table-column>
        <el-table-column label="是否显示" width="90" align="center">
          <template #default="{ row }">
            <span :class="row.visible ? 'visible-yes' : 'visible-no'">
              {{ row.visible ? '显示' : '隐藏' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column prop="addTime" label="添加时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" class="btn-blue" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" class="btn-red" @click="handleDelete(row)">删除</el-button>
            <el-button
              size="small"
              :class="row.visible ? 'btn-red' : 'btn-green-solid'"
              @click="toggleVisible(row)"
            >
              {{ row.visible ? '隐藏' : '显示' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 发货/退货地址 -->
    <el-card
      v-else-if="activeSettingTab === 'shipAddress' || activeSettingTab === 'returnAddress'"
      shadow="never"
      class="panel-card"
    >
      <el-table :data="settingData.list" border stripe>
        <el-table-column prop="id" label="编号" width="100" align="center" />
        <el-table-column prop="contactName" label="联系人" width="110" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="fullAddress" label="地址" min-width="260" show-overflow-tooltip />
        <el-table-column prop="zipCode" label="邮编" width="80" align="center" />
        <el-table-column label="是否默认" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
            <el-button v-else link type="primary" size="small" @click="setDefault(row)">设为默认</el-button>
          </template>
        </el-table-column>
        <el-table-column label="是否显示" width="90" align="center">
          <template #default="{ row }">
            <span :class="row.visible ? 'visible-yes' : 'visible-no'">
              {{ row.visible ? '显示' : '隐藏' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column prop="addTime" label="添加时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" class="btn-blue" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" class="btn-red" @click="handleDelete(row)">删除</el-button>
            <el-button
              size="small"
              :class="row.visible ? 'btn-red' : 'btn-green-solid'"
              @click="toggleVisible(row)"
            >
              {{ row.visible ? '隐藏' : '显示' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-if="activeSettingTab" shadow="never" class="panel-card">
      <div class="pagination-bar">
        <span class="page-info">
          第{{ settingData.pagination.page }}页 共{{ totalPages }}页
          {{ settingData.pagination.total }}条
        </span>
        <el-pagination
          v-model:current-page="settingData.pagination.page"
          v-model:page-size="settingData.pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="settingData.pagination.total"
          layout="prev, pager, next, sizes"
          background
          @current-change="fetchSettingList"
          @size-change="onPageSizeChange"
        />
      </div>
    </el-card>

    <!-- 快递单模板弹窗 -->
    <el-dialog
      v-model="expressDialog.visible"
      :title="expressDialog.isEdit ? '编辑模板' : '添加快递单模板'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="expressFormRef" :model="expressDialog.form" :rules="expressRules" label-width="100px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="expressDialog.form.templateName" placeholder="如：申通标准面单" />
        </el-form-item>
        <el-form-item label="快递公司" prop="expressCompany">
          <el-select v-model="expressDialog.form.expressCompany" placeholder="请选择" style="width: 100%">
            <el-option v-for="c in expressCompanies" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板规格" prop="templateSpec">
          <el-input v-model="expressDialog.form.templateSpec" placeholder="如：100*180mm" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="expressDialog.form.remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="expressDialog.form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="expressDialog.form.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="expressDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="expressDialog.saving" @click="submitExpress">确定</el-button>
      </template>
    </el-dialog>

    <!-- 地址弹窗 -->
    <el-dialog
      v-model="addressDialog.visible"
      :title="addressDialog.isEdit ? '编辑地址' : addressDialogTitle"
      width="560px"
      destroy-on-close
    >
      <el-form ref="addressFormRef" :model="addressDialog.form" :rules="addressRules" label-width="100px">
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="addressDialog.form.contactName" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="addressDialog.form.phone" />
        </el-form-item>
        <el-form-item label="所在地区">
          <AreaCascader v-model="addressDialog.regionCodes" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="addressDialog.form.detailAddress" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="邮编">
          <el-input v-model="addressDialog.form.zipCode" maxlength="10" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="addressDialog.form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressDialog.form.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="addressDialog.saving" @click="submitAddress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AreaCascader from '@/components/AreaCascader/index.vue'
import {
  createExpressTemplate,
  createOrderAddress,
  createReturnReason,
  deleteExpressTemplate,
  deleteOrderAddress,
  deleteReturnReason,
  fetchExpressTemplateList,
  fetchOrderAddressList,
  fetchReturnReasonList,
  updateExpressTemplate,
  updateExpressTemplateDefault,
  updateExpressTemplateVisible,
  updateOrderAddress,
  updateOrderAddressDefault,
  updateOrderAddressVisible,
  updateReturnReason,
  updateReturnReasonVisible,
} from '@/api/order'

const loading = ref(false)
const activeSettingTab = ref('returnReason')
const expressFormRef = ref()
const addressFormRef = ref()

const expressCompanies = ['申通快递', '顺丰速运', '圆通速递', '韵达快递', '中通快递', '京东物流', 'EMS', '极兔速递']

const settingData = reactive({
  list: [],
  pagination: { page: 1, pageSize: 10, total: 0 },
})

const expressDialog = reactive({
  visible: false,
  isEdit: false,
  editId: '',
  saving: false,
  form: emptyExpressForm(),
})

const addressDialog = reactive({
  visible: false,
  isEdit: false,
  editId: '',
  saving: false,
  regionCodes: [],
  form: emptyAddressForm(),
})

const expressRules = {
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  expressCompany: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
}

const addressRules = {
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
}

const addButtonText = computed(() => {
  const map = {
    returnReason: '添加原因',
    expressTemplate: '添加模板',
    shipAddress: '添加发货地址',
    returnAddress: '添加退货地址',
  }
  return map[activeSettingTab.value] || '添加'
})

const addressDialogTitle = computed(() =>
  activeSettingTab.value === 'shipAddress' ? '添加发货地址' : '添加退货地址',
)

const addressType = computed(() => (activeSettingTab.value === 'returnAddress' ? 'return' : 'ship'))

const totalPages = computed(() =>
  Math.max(1, Math.ceil(settingData.pagination.total / settingData.pagination.pageSize)),
)

function emptyExpressForm() {
  return {
    templateName: '',
    expressCompany: '',
    templateSpec: '',
    remark: '',
    sort: 0,
    isDefault: false,
  }
}

function emptyAddressForm() {
  return {
    contactName: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    zipCode: '',
    sort: 0,
    isDefault: false,
  }
}

const onTabChange = () => {
  settingData.pagination.page = 1
  fetchSettingList()
}

const fetchSettingList = async () => {
  loading.value = true
  try {
    const { page, pageSize } = settingData.pagination
    const tab = activeSettingTab.value
    let data
    if (tab === 'returnReason') {
      data = await fetchReturnReasonList({ page, pageSize })
    } else if (tab === 'expressTemplate') {
      data = await fetchExpressTemplateList({ page, pageSize })
    } else {
      data = await fetchOrderAddressList({ type: addressType.value, page, pageSize })
    }
    settingData.list = data.list
    settingData.pagination.total = data.total
  } finally {
    loading.value = false
  }
}

const onPageSizeChange = () => {
  settingData.pagination.page = 1
  fetchSettingList()
}

const toggleVisible = async (row) => {
  const next = !row.visible
  const tab = activeSettingTab.value
  if (tab === 'returnReason') {
    await updateReturnReasonVisible(row.id, next)
  } else if (tab === 'expressTemplate') {
    await updateExpressTemplateVisible(row.id, next)
  } else {
    await updateOrderAddressVisible(row.id, next)
  }
  row.visible = next
  ElMessage.success(next ? '已设为显示' : '已设为隐藏')
}

const setDefault = async (row) => {
  const tab = activeSettingTab.value
  if (tab === 'expressTemplate') {
    await updateExpressTemplateDefault(row.id, true)
  } else {
    await updateOrderAddressDefault(row.id, true)
  }
  ElMessage.success('已设为默认')
  fetchSettingList()
}

const promptReason = (title, defaultValue = '') =>
  ElMessageBox.prompt('请输入退货原因', title, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: defaultValue,
    inputPattern: /\S+/,
    inputErrorMessage: '原因不能为空',
  })

const handleAdd = () => {
  const tab = activeSettingTab.value
  if (tab === 'returnReason') {
    promptReason('添加原因')
      .then(async ({ value }) => {
        await createReturnReason({ reason: value, visible: true })
        ElMessage.success('添加成功')
        fetchSettingList()
      })
      .catch(() => {})
    return
  }
  if (tab === 'expressTemplate') {
    expressDialog.isEdit = false
    expressDialog.editId = ''
    Object.assign(expressDialog.form, emptyExpressForm())
    expressDialog.visible = true
    return
  }
  addressDialog.isEdit = false
  addressDialog.editId = ''
  addressDialog.regionCodes = []
  Object.assign(addressDialog.form, emptyAddressForm())
  addressDialog.visible = true
}

const handleEdit = (row) => {
  const tab = activeSettingTab.value
  if (tab === 'returnReason') {
    promptReason('编辑原因', row.reason)
      .then(async ({ value }) => {
        await updateReturnReason(row.id, { reason: value })
        ElMessage.success('保存成功')
        fetchSettingList()
      })
      .catch(() => {})
    return
  }
  if (tab === 'expressTemplate') {
    expressDialog.isEdit = true
    expressDialog.editId = row.id
    Object.assign(expressDialog.form, {
      templateName: row.templateName,
      expressCompany: row.expressCompany,
      templateSpec: row.templateSpec || '',
      remark: row.remark || '',
      sort: row.sort ?? 0,
      isDefault: row.isDefault,
    })
    expressDialog.visible = true
    return
  }
  addressDialog.isEdit = true
  addressDialog.editId = row.id
  addressDialog.regionCodes = [row.provinceCode, row.cityCode, row.districtCode].filter(Boolean)
  Object.assign(addressDialog.form, {
    contactName: row.contactName,
    phone: row.phone,
    detailAddress: row.detailAddress,
    zipCode: row.zipCode || '',
    sort: row.sort ?? 0,
    isDefault: row.isDefault,
  })
  addressDialog.visible = true
}

const handleDelete = (row) => {
  const label =
    activeSettingTab.value === 'returnReason'
      ? row.reason
      : activeSettingTab.value === 'expressTemplate'
        ? row.templateName
        : row.contactName
  ElMessageBox.confirm(`确定删除「${label}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      const tab = activeSettingTab.value
      if (tab === 'returnReason') await deleteReturnReason(row.id)
      else if (tab === 'expressTemplate') await deleteExpressTemplate(row.id)
      else await deleteOrderAddress(row.id)
      ElMessage.success('删除成功')
      fetchSettingList()
    })
    .catch(() => {})
}

const submitExpress = async () => {
  const valid = await expressFormRef.value?.validate().catch(() => false)
  if (!valid) return
  expressDialog.saving = true
  try {
    const payload = {
      ...expressDialog.form,
      visible: true,
    }
    if (expressDialog.isEdit) {
      await updateExpressTemplate(expressDialog.editId, payload)
    } else {
      await createExpressTemplate(payload)
    }
    ElMessage.success('保存成功')
    expressDialog.visible = false
    fetchSettingList()
  } finally {
    expressDialog.saving = false
  }
}

const submitAddress = async () => {
  const valid = await addressFormRef.value?.validate().catch(() => false)
  if (!valid) return
  addressDialog.saving = true
  try {
    const [provinceCode, cityCode, districtCode] = addressDialog.regionCodes || []
    const payload = {
      type: addressType.value,
      ...addressDialog.form,
      provinceCode: provinceCode || undefined,
      cityCode: cityCode || undefined,
      districtCode: districtCode || undefined,
      visible: true,
    }
    if (addressDialog.isEdit) {
      await updateOrderAddress(addressDialog.editId, payload)
    } else {
      await createOrderAddress(payload)
    }
    ElMessage.success('保存成功')
    addressDialog.visible = false
    fetchSettingList()
  } finally {
    addressDialog.saving = false
  }
}

onMounted(fetchSettingList)
</script>

<style scoped>
.order-setting-page {
  min-height: calc(100vh - 120px);
}

.panel-card {
  margin-bottom: 12px;
  border-radius: 8px;
  border: none;
}

.setting-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.setting-tabs {
  flex: 1;
}

.setting-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.visible-yes {
  color: #303133;
}

.visible-no {
  color: #f56c6c;
  font-weight: 500;
}

.btn-blue {
  color: #409eff;
  border-color: #b3d8ff;
  background: #ecf5ff;
}

.btn-red {
  color: #f56c6c;
  border-color: #fbc4c4;
  background: #fef0f0;
}

.btn-green-solid {
  color: #fff;
  border-color: #67c23a;
  background: #67c23a;
}

.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-info {
  font-size: 13px;
  color: #606266;
}
</style>
