<template>
  <div v-loading="loading" class="category-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="panel-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入名称或编号"
            clearable
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 级别 Tabs + 操作按钮 -->
    <el-card shadow="never" class="panel-card">
      <div class="toolbar">
        <el-tabs v-model="activeLevel" @tab-change="fetchData">
          <el-tab-pane label="一级类目" name="1" />
          <el-tab-pane label="二级类目" name="2" />
          <el-tab-pane label="三级类目" name="3" />
          <el-tab-pane label="四级类目" name="4" />
        </el-tabs>
        <div class="toolbar-actions">
          <el-button type="primary" :icon="Plus" @click="openCreate">添加分类</el-button>
          <el-button type="danger" :icon="Delete" :disabled="!selected.length" @click="handleBatchDelete">
            批量删除
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 分类树 + 表格 -->
    <el-card shadow="never" class="panel-card body-card">
      <div class="body-layout">
        <aside class="category-tree">
          <el-tree
            :data="categoryTreeData"
            :props="{ label: 'label', children: 'children' }"
            default-expand-all
            highlight-current
          />
        </aside>

        <div class="table-wrap">
          <el-table :data="tableData" border stripe @selection-change="(r) => (selected = r)">
            <el-table-column type="selection" width="48" align="center" />
            <el-table-column prop="id" label="类目编号" width="110" align="center" />
            <el-table-column prop="name" label="分类名称" min-width="120" />
            <el-table-column prop="level" label="级别" width="80" align="center" />
            <el-table-column prop="count" label="商品数量" width="100" align="center" />
            <el-table-column prop="unit" label="数量单位" width="90" align="center" />
            <el-table-column label="是否显示" width="90" align="center">
              <template #default="{ row }">
                <span :class="row.visible ? 'visible-yes' : 'visible-no'">
                  {{ row.visible ? '显示' : '隐藏' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="sort" label="排序" width="70" align="center" />

            <el-table-column label="设置" width="320" align="center">
              <template #default="{ row }">
                <el-button size="small" class="btn-green" @click="handleAddChild(row)">新增下级</el-button>
                <el-button size="small" class="btn-green" @click="handleViewChild(row)">查看下级</el-button>
                <el-button size="small" class="btn-green" @click="handleTransfer(row)">转移商品</el-button>
                <el-button
                  size="small"
                  :class="row.visible ? 'btn-red' : 'btn-green-solid'"
                  @click="toggleVisible(row)"
                >
                  {{ row.visible ? '隐藏' : '显示' }}
                </el-button>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="120" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-bar">
            <span class="total-text">共 {{ pagination.total }} 条</span>
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="pagination.total"
              layout="prev, pager, next, sizes"
              background
              @current-change="fetchData"
              @size-change="onPageSizeChange"
            />
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="dialogForm.name" placeholder="请输入分类名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="级别">
          <el-input :model-value="levelLabel(dialogForm.level)" disabled />
        </el-form-item>
        <el-form-item label="数量单位">
          <el-input v-model="dialogForm.unit" placeholder="如：件" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input v-model.number="dialogForm.sort" placeholder="数字越小越靠前" />
        </el-form-item>
        <el-form-item label="是否显示">
          <el-switch v-model="dialogForm.visible" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dialogSaving" @click="submitDialog">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="transferVisible" title="转移商品分类" width="480px" destroy-on-close>
      <p class="transfer-tip">
        将分类「{{ transferForm.fromName }}」下的全部商品移至目标分类，源分类商品数将归零。
      </p>
      <el-form label-width="90px">
        <el-form-item label="目标分类">
          <el-select v-model="transferForm.toCode" placeholder="请选择目标分类" style="width: 100%">
            <el-option
              v-for="opt in categoryOptions.filter((o) => o.value !== transferForm.fromId)"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" :loading="transferSaving" @click="submitTransfer">确认转移</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import {
  createCategory,
  deleteCategory,
  fetchCategoryList,
  fetchCategoryOptions,
  fetchCategoryTree,
  transferCategoryProducts,
  updateCategory,
  updateCategoryVisible,
} from '@/api/product'

const LEVEL_LABELS = { 1: '一级', 2: '二级', 3: '三级', 4: '四级' }

const loading = ref(false)
const activeLevel = ref('1')
const selected = ref([])
const tableData = ref([])
const categoryTreeData = ref([])
const dialogVisible = ref(false)
const dialogSaving = ref(false)
const dialogMode = ref('create')
const dialogFormRef = ref(null)
const transferVisible = ref(false)
const transferSaving = ref(false)
const categoryOptions = ref([])

const transferForm = reactive({
  fromId: '',
  fromName: '',
  toCode: '',
})

const dialogForm = reactive({
  id: '',
  name: '',
  level: '一级',
  unit: '件',
  sort: 0,
  visible: true,
})

const dialogRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

const dialogTitle = computed(() => (dialogMode.value === 'create' ? '添加分类' : '编辑分类'))

const levelLabel = (level) => level || '一级'

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const loadTree = async () => {
  categoryTreeData.value = await fetchCategoryTree()
  categoryOptions.value = await fetchCategoryOptions()
}

const fetchData = async () => {
  loading.value = true
  try {
    const data = await fetchCategoryList({
      keyword: searchForm.keyword || undefined,
      level: Number(activeLevel.value),
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = data.list
    pagination.total = data.total
  } finally {
    loading.value = false
  }
}

const onPageSizeChange = () => {
  pagination.page = 1
  fetchData()
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
  ElMessage.success('查询成功')
}

const handleReset = () => {
  searchForm.keyword = ''
  fetchData()
}

const openCreate = () => {
  dialogMode.value = 'create'
  Object.assign(dialogForm, {
    id: '',
    name: '',
    level: LEVEL_LABELS[Number(activeLevel.value)] || '一级',
    unit: '件',
    sort: 0,
    visible: true,
  })
  dialogVisible.value = true
}

const submitDialog = async () => {
  const valid = await dialogFormRef.value?.validate().catch(() => false)
  if (!valid) return
  dialogSaving.value = true
  try {
    const payload = {
      name: dialogForm.name.trim(),
      level: dialogForm.level,
      unit: dialogForm.unit || '件',
      sort: dialogForm.sort ?? 0,
      visible: dialogForm.visible,
    }
    if (dialogMode.value === 'create') {
      await createCategory(payload)
      ElMessage.success('分类已添加')
    } else {
      await updateCategory(dialogForm.id, payload)
      ElMessage.success('分类已更新')
    }
    dialogVisible.value = false
    await Promise.all([fetchData(), loadTree()])
  } finally {
    dialogSaving.value = false
  }
}

const handleBatchDelete = () => {
  if (!selected.value.length) return
  ElMessageBox.confirm(`确定删除选中的 ${selected.value.length} 个分类吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await Promise.all(selected.value.map((row) => deleteCategory(row.id)))
      ElMessage.success('批量删除成功')
      selected.value = []
      await Promise.all([fetchData(), loadTree()])
    })
    .catch(() => {})
}

const toggleVisible = async (row) => {
  const next = !row.visible
  await updateCategoryVisible(row.id, next)
  row.visible = next
  ElMessage.success(next ? '已显示' : '已隐藏')
}

const handleAddChild = (row) => {
  const nextLevel = Math.min(Number(activeLevel.value) + 1, 4)
  activeLevel.value = String(nextLevel)
  dialogMode.value = 'create'
  Object.assign(dialogForm, {
    id: '',
    name: '',
    level: LEVEL_LABELS[nextLevel] || '四级',
    unit: row.unit || '件',
    sort: 0,
    visible: true,
  })
  dialogVisible.value = true
}

const handleViewChild = (row) => {
  const nextLevel = Math.min(Number(activeLevel.value) + 1, 4)
  activeLevel.value = String(nextLevel)
  searchForm.keyword = ''
  pagination.page = 1
  fetchData()
  ElMessage.info(`已切换到 ${LEVEL_LABELS[nextLevel]} 类目列表`)
}

const handleTransfer = (row) => {
  transferForm.fromId = row.id
  transferForm.fromName = row.name
  transferForm.toCode = ''
  transferVisible.value = true
}

const submitTransfer = async () => {
  if (!transferForm.toCode) {
    ElMessage.warning('请选择目标分类')
    return
  }
  transferSaving.value = true
  try {
    const { moved } = await transferCategoryProducts(transferForm.fromId, transferForm.toCode)
    ElMessage.success(`已转移 ${moved} 件商品`)
    transferVisible.value = false
    await Promise.all([fetchData(), loadTree()])
  } finally {
    transferSaving.value = false
  }
}

const handleEdit = (row) => {
  dialogMode.value = 'edit'
  Object.assign(dialogForm, {
    id: row.id,
    name: row.name,
    level: row.level,
    unit: row.unit || '件',
    sort: row.sort ?? 0,
    visible: row.visible,
  })
  dialogVisible.value = true
}
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除分类 ${row.name} 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteCategory(row.id)
      ElMessage.success('删除成功')
      fetchData()
      loadTree()
    })
    .catch(() => {})
}

onMounted(async () => {
  await loadTree()
  await fetchData()
})
</script>

<style scoped>
.category-page {
  min-height: calc(100vh - 120px);
}

.panel-card {
  margin-bottom: 12px;
  border-radius: 8px;
  border: none;
}

.search-actions {
  margin-left: auto;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.body-layout {
  display: flex;
  gap: 16px;
}

.category-tree {
  flex-shrink: 0;
  width: 180px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.table-wrap {
  flex: 1;
  min-width: 0;
  overflow: auto;
}

.visible-yes {
  color: #303133;
}

.visible-no {
  color: #f56c6c;
  font-weight: 500;
}

.btn-green {
  color: #67c23a;
  border-color: #c2e7b0;
  background: #f0f9eb;
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
  margin-top: 16px;
}

.total-text {
  font-size: 13px;
  color: #606266;
}

.transfer-tip {
  margin: 0 0 16px;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}
</style>
