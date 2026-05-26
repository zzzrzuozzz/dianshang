<template>
  <div class="invite-generate-page">
    <el-card shadow="never">
      <template #header>
        <span>随机生成邀请码</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 520px">
        <el-form-item label="绑定角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="注册成功后自动绑定该角色" style="width: 100%">
            <el-option
              v-for="r in roleOptions"
              :key="r.id"
              :label="`${r.roleName} (${r.roleKey})`"
              :value="r.id"
            />
          </el-select>
          <p class="form-tip">注册用户的菜单权限由该角色决定，可在「角色管理」中调整。</p>
        </el-form-item>
        <el-form-item label="生成数量" prop="count">
          <el-input-number v-model="form.count" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选，如：2026校招批次" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleGenerate">生成邀请码</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="generatedCodes.length" shadow="never" class="result-card">
      <template #header>
        <div class="result-header">
          <span>本次生成 {{ generatedCodes.length }} 个邀请码</span>
          <el-button type="primary" link @click="copyAll">复制全部</el-button>
        </div>
      </template>
      <div class="code-list">
        <el-tag v-for="code in generatedCodes" :key="code" class="code-tag" effect="plain" size="large">
          {{ code }}
        </el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchRoleOptions } from '@/api/permission'
import { generateInviteCodes } from '@/api/invite'

const formRef = ref(null)
const submitting = ref(false)
const roleOptions = ref([])
const generatedCodes = ref([])

const form = reactive({
  roleId: undefined,
  count: 5,
  remark: '',
})

const rules = {
  roleId: [{ required: true, message: '请选择绑定角色', trigger: 'change' }],
  count: [{ required: true, message: '请输入数量', trigger: 'change' }],
}

const loadRoles = async () => {
  roleOptions.value = await fetchRoleOptions()
  const operator = roleOptions.value.find((r) => r.roleKey === 'operator')
  if (operator) form.roleId = operator.id
}

const handleGenerate = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    generatedCodes.value = await generateInviteCodes({
      roleId: form.roleId,
      count: form.count,
      remark: form.remark || undefined,
    })
    ElMessage.success('邀请码已生成')
  } finally {
    submitting.value = false
  }
}

const copyAll = async () => {
  try {
    await navigator.clipboard.writeText(generatedCodes.value.join('\n'))
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.warning('复制失败，请手动选择复制')
  }
}

onMounted(loadRoles)
</script>

<style scoped>
.invite-generate-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-tip {
  margin: 6px 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.code-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.code-tag {
  font-family: ui-monospace, monospace;
  letter-spacing: 0.05em;
}
</style>
