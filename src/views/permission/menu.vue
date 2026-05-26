<template>
  <div v-loading="loading" class="menu-page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openDialog(null, 0)">新增根菜单</el-button>
        <el-button @click="loadTree">刷新</el-button>
      </div>
      <el-table
        :data="treeData"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="MENU_TYPE_TAG[row.menuType] || 'info'" size="small">
              {{ MENU_TYPE_LABEL[row.menuType] || row.menuType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortNum" label="排序" width="70" align="center" />
        <el-table-column prop="path" label="路由地址" min-width="160" show-overflow-tooltip />
        <el-table-column prop="perms" label="权限标识" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(null, row.id)">添加下级</el-button>
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="根节点选 0"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType" @change="onTypeChange">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="菜单图标">
          <el-select v-model="form.icon" clearable placeholder="Element Plus 图标名" style="width: 100%">
            <el-option v-for="ic in iconOptions" :key="ic" :label="ic" :value="ic" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="路由地址" prop="path">
          <el-input v-model="form.path" placeholder="/finance/withdraw" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 'F'" label="权限标识" prop="perms">
          <el-input v-model="form.perms" placeholder="finance:withdraw:verify" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortNum" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onActivated } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { fetchMenuTree, saveMenu, deleteMenu } from '@/api/permission'
import { MENU_TYPE_LABEL, MENU_TYPE_TAG, MENU_ICON_OPTIONS } from '@/constants/permission'

const loading = ref(false)
const submitting = ref(false)
const treeData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const editingId = ref(null)

const iconOptions = MENU_ICON_OPTIONS

const form = reactive({
  parentId: 0,
  menuName: '',
  menuType: 'C',
  path: '',
  perms: '',
  icon: '',
  sortNum: 0,
})

const rules = computed(() => ({
  menuName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  path: form.menuType === 'F' ? [] : [{ required: form.menuType === 'C', message: '请输入路由', trigger: 'blur' }],
  perms: form.menuType === 'F' ? [{ required: true, message: '请输入权限标识', trigger: 'blur' }] : [],
}))

const dialogTitle = computed(() => (editingId.value ? '编辑菜单' : '新增菜单'))

const parentOptions = computed(() => [{ id: 0, menuName: '根目录', children: treeData.value }])

const loadTree = async () => {
  loading.value = true
  try {
    treeData.value = await fetchMenuTree()
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  editingId.value = null
  form.parentId = 0
  form.menuName = ''
  form.menuType = 'C'
  form.path = ''
  form.perms = ''
  form.icon = ''
  form.sortNum = 0
}

const openDialog = (row, parentId) => {
  resetForm()
  if (row) {
    editingId.value = row.id
    form.parentId = row.parentId ?? 0
    form.menuName = row.menuName
    form.menuType = row.menuType
    form.path = row.path || ''
    form.perms = row.perms || ''
    form.icon = row.icon || ''
    form.sortNum = row.sortNum ?? 0
  } else if (parentId !== undefined) {
    form.parentId = parentId
    form.menuType = parentId === 0 ? 'M' : 'C'
  }
  dialogVisible.value = true
}

const onTypeChange = () => {
  if (form.menuType === 'F') {
    form.path = ''
    form.icon = ''
  } else {
    form.perms = ''
  }
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await saveMenu({
      id: editingId.value || undefined,
      parentId: form.parentId || 0,
      menuName: form.menuName,
      menuType: form.menuType,
      path: form.path || undefined,
      perms: form.perms || undefined,
      icon: form.icon || undefined,
      sortNum: form.sortNum,
    })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadTree()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除「${row.menuName}」？若存在子节点或已绑定角色将无法删除。`, '删除确认', {
    type: 'warning',
  })
  try {
    await deleteMenu(row.id)
    ElMessage.success('已删除')
    await loadTree()
  } catch (e) {
    /* 后端返回业务错误提示 */
  }
}

onMounted(loadTree)
onActivated(loadTree)
</script>

<style scoped>
.menu-page .toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
</style>
