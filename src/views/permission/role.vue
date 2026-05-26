<template>
  <div class="role-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="角色名/权限字符" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="primary" @click="openRoleDialog()">新增角色</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-loading="loading" shadow="never">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="角色编号" width="90" />
        <el-table-column prop="roleName" label="角色名称" min-width="140" />
        <el-table-column prop="roleKey" label="权限字符" width="120">
          <template #default="{ row }">
            <el-tag type="info" effect="plain">{{ row.roleKey }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(v) => toggleStatus(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openPermDialog(row)">分配权限</el-button>
            <el-button link type="primary" @click="openRoleDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDeleteRole(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          layout="total, prev, pager, next"
          background
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="roleDialogVisible" :title="roleForm.id ? '编辑角色' : '新增角色'" width="480px">
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" />
        </el-form-item>
        <el-form-item label="权限字符" prop="roleKey">
          <el-input v-model="roleForm.roleKey" placeholder="如 finance" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="roleForm.sortNum" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="roleForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitting" @click="submitRole">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permDialogVisible" title="分配菜单权限" width="520px" destroy-on-close>
      <p class="perm-tip">
        勾选目录、菜单及按钮要素。保存时后端在事务内先清空该角色旧关联再批量插入，防止主键冲突。
      </p>
      <el-tree
        ref="permTreeRef"
        v-loading="permLoading"
        :data="menuTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'menuName', children: 'children' }"
        default-expand-all
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSubmitting" @click="submitPermissions">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onActivated, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchRolePage,
  saveRole,
  updateRoleStatus,
  deleteRole,
  fetchMenuTree,
  fetchRoleMenuIds,
  saveRolePermissions,
} from '@/api/permission'

const loading = ref(false)
const tableData = ref([])
const query = reactive({ keyword: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const roleDialogVisible = ref(false)
const roleSubmitting = ref(false)
const roleFormRef = ref(null)
const roleForm = reactive({
  id: undefined,
  roleName: '',
  roleKey: '',
  sortNum: 0,
  remark: '',
})
const roleRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入权限字符', trigger: 'blur' }],
}

const permDialogVisible = ref(false)
const permLoading = ref(false)
const permSubmitting = ref(false)
const permTreeRef = ref(null)
const menuTree = ref([])
const currentRoleId = ref(null)

const fetchData = async () => {
  loading.value = true
  try {
    const data = await fetchRolePage({
      keyword: query.keyword || undefined,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = data.list || []
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const openRoleDialog = (row) => {
  roleForm.id = row?.id
  roleForm.roleName = row?.roleName || ''
  roleForm.roleKey = row?.roleKey || ''
  roleForm.sortNum = row?.sortNum ?? 0
  roleForm.remark = row?.remark || ''
  roleDialogVisible.value = true
}

const submitRole = async () => {
  const valid = await roleFormRef.value?.validate().catch(() => false)
  if (!valid) return
  roleSubmitting.value = true
  try {
    await saveRole({ ...roleForm })
    ElMessage.success('角色已保存')
    roleDialogVisible.value = false
    fetchData()
  } finally {
    roleSubmitting.value = false
  }
}

const toggleStatus = async (row, enabled) => {
  await updateRoleStatus(row.id, enabled ? 1 : 0)
  row.status = enabled ? 1 : 0
  ElMessage.success('状态已更新')
}

const handleDeleteRole = async (row) => {
  await ElMessageBox.confirm(`确定删除角色「${row.roleName}」？`, '提示', { type: 'warning' })
  await deleteRole(row.id)
  ElMessage.success('已删除')
  fetchData()
}

const openPermDialog = async (row) => {
  currentRoleId.value = row.id
  permDialogVisible.value = true
  permLoading.value = true
  try {
    const [tree, checked] = await Promise.all([fetchMenuTree(), fetchRoleMenuIds(row.id)])
    menuTree.value = tree
    await nextTick()
    permTreeRef.value?.setCheckedKeys(checked || [], false)
  } finally {
    permLoading.value = false
  }
}

const submitPermissions = async () => {
  if (!currentRoleId.value) return
  const checked = permTreeRef.value?.getCheckedKeys(false) || []
  const half = permTreeRef.value?.getHalfCheckedKeys() || []
  const menuIds = [...new Set([...checked, ...half])]
  permSubmitting.value = true
  try {
    await saveRolePermissions(currentRoleId.value, menuIds)
    ElMessage.success('权限分配已保存')
    permDialogVisible.value = false
  } finally {
    permSubmitting.value = false
  }
}

onMounted(fetchData)
onActivated(fetchData)
</script>

<style scoped>
.role-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}
.pagination-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.perm-tip {
  font-size: 12px;
  color: #909399;
  margin: 0 0 12px;
  line-height: 1.6;
}
</style>
