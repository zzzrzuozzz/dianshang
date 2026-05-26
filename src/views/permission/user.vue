<template>
  <div class="admin-user-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="登录账号">
          <el-input v-model="query.username" clearable />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="query.nickname" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="primary" @click="openDialog()">新增管理员</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-loading="loading" shadow="never">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="username" label="登录账号" width="140" />
        <el-table-column prop="nickname" label="用户昵称" min-width="120" />
        <el-table-column label="所属角色" min-width="200">
          <template #default="{ row }">
            <el-tag
              v-for="r in row.roles"
              :key="r.id"
              class="role-tag"
              type="primary"
              effect="plain"
              size="small"
            >
              {{ r.roleName }}
            </el-tag>
            <span v-if="!row.roles?.length" class="muted">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(v) => toggleStatus(row, v)"
            />
            <el-tag v-if="row.status === 0" type="danger" size="small" class="status-tag">禁止登录</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑管理员' : '新增管理员'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="用户昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item :label="form.id ? '新密码' : '登录密码'" :prop="form.id ? '' : 'password'">
          <el-input v-model="form.password" type="password" show-password placeholder="编辑时留空则不修改" />
        </el-form-item>
        <el-form-item label="所属角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="选择角色" style="width: 100%">
            <el-option
              v-for="r in roleOptions"
              :key="r.id"
              :label="`${r.roleName} (${r.roleKey})`"
              :value="r.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <p class="form-tip">
        分配角色后，该账号登录时仅加载对应菜单；JWT 会话内携带 roleKey，前端根据菜单树动态渲染侧栏（router 静态注册 + 路径守卫）。
      </p>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onActivated } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminPage, saveAdmin, updateAdminStatus, fetchRoleOptions } from '@/api/permission'
import { refreshAuthSession } from '@/api/auth'
import { getProfile } from '@/api/profile'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const roleOptions = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

const query = reactive({ username: '', nickname: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const form = reactive({
  id: undefined,
  username: '',
  nickname: '',
  password: '',
  roleIds: [],
  status: 1,
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  roleIds: [{ required: true, type: 'array', min: 1, message: '请至少选择一个角色', trigger: 'change' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const data = await fetchAdminPage({
      username: query.username || undefined,
      nickname: query.nickname || undefined,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = data.list || []
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

const loadRoleOptions = async () => {
  roleOptions.value = await fetchRoleOptions()
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const openDialog = (row) => {
  form.id = row?.id
  form.username = row?.username || ''
  form.nickname = row?.nickname || ''
  form.password = ''
  form.roleIds = row?.roles?.map((r) => r.id) || []
  form.status = row?.status ?? 1
  dialogVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await saveAdmin({
      id: form.id,
      username: form.username,
      nickname: form.nickname,
      password: form.password || undefined,
      roleIds: form.roleIds,
      status: form.status,
    })
    let msg = '保存成功，该用户重新登录后生效新菜单'
    if (form.id) {
      try {
        const me = await getProfile()
        if (me?.id === form.id) {
          await refreshAuthSession()
          msg = '保存成功，您的菜单权限已刷新'
        }
      } catch {
        /* ignore */
      }
    }
    ElMessage.success(msg)
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row, enabled) => {
  await updateAdminStatus(row.id, enabled ? 1 : 0)
  row.status = enabled ? 1 : 0
  ElMessage.success(enabled ? '已启用' : '已禁用登录')
}

onMounted(() => {
  loadRoleOptions()
  fetchData()
})
onActivated(fetchData)
</script>

<style scoped>
.admin-user-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.role-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}
.muted {
  color: #909399;
  font-size: 12px;
}
.status-tag {
  margin-left: 6px;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
  margin: 0;
}
.pagination-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
