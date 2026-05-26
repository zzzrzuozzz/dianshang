<template>
  <div v-loading="pageLoading" class="profile-page">
    <el-row :gutter="20">
      <el-col :xs="24" :md="8" :lg="7">
        <el-card shadow="never" class="panel-card info-card">
          <div class="avatar-block">
            <el-avatar :size="100" :src="profile.avatar" />
            <el-upload
              class="avatar-upload"
              :show-file-list="false"
              accept="image/jpeg,image/png"
              :before-upload="beforeAvatarUpload"
            >
              <el-button size="small" type="primary" plain>更换头像</el-button>
            </el-upload>
          </div>
          <ul class="static-info">
            <li><span class="label">登录账号</span><span class="value">{{ profile.username }}</span></li>
            <li><span class="label">所属角色</span><span class="value">{{ profile.roleName }}</span></li>
            <li><span class="label">创建时间</span><span class="value">{{ profile.createTime }}</span></li>
          </ul>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="16" :lg="17">
        <el-card shadow="never" class="panel-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本资料" name="basic">
              <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-width="100px"
                class="profile-form"
              >
                <el-form-item label="用户昵称" prop="nickname">
                  <el-input v-model="profileForm.nickname" placeholder="请输入昵称" maxlength="30" show-word-limit />
                </el-form-item>
                <el-form-item label="手机号码" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="请输入手机号" maxlength="11" />
                </el-form-item>
                <el-form-item label="用户邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="profileForm.gender">
                    <el-radio value="male">男</el-radio>
                    <el-radio value="female">女</el-radio>
                    <el-radio value="unknown">保密</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="profileSubmitting" @click="submitProfile">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="修改密码" name="password">
              <el-form
                ref="pwdFormRef"
                :model="pwdForm"
                :rules="pwdRules"
                label-width="100px"
                class="profile-form"
              >
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input
                    v-model="pwdForm.oldPassword"
                    type="password"
                    show-password
                    placeholder="请输入旧密码"
                    autocomplete="off"
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="pwdForm.newPassword"
                    type="password"
                    show-password
                    placeholder="6-20 位新密码"
                    autocomplete="new-password"
                  />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input
                    v-model="pwdForm.confirmPassword"
                    type="password"
                    show-password
                    placeholder="请再次输入新密码"
                    autocomplete="new-password"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="pwdSubmitting" @click="submitPassword">确定修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, updatePassword } from '@/api/profile'
import { clearToken } from '@/utils/auth'

const router = useRouter()
const pageLoading = ref(false)
const profileSubmitting = ref(false)
const pwdSubmitting = ref(false)
const activeTab = ref('basic')
const profileFormRef = ref()
const pwdFormRef = ref()

const profile = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  gender: 'male',
  avatar: '',
  roleName: '',
  createTime: '',
})

const profileForm = reactive({
  nickname: '',
  phone: '',
  email: '',
  gender: 'male',
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const phonePattern = /^1[3-9]\d{9}$/
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const profileRules = {
  nickname: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: phonePattern, message: '手机号格式不正确', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
    { pattern: emailPattern, message: '邮箱格式不正确', trigger: 'blur' },
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
}

const validateConfirmPwd = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '新密码长度需在 6-20 位之间', trigger: 'blur' },
  ],
  confirmPassword: [{ required: true, validator: validateConfirmPwd, trigger: 'blur' }],
}

/**
 * GET /api/system/user/profile
 * 后端从 Authorization Bearer JWT 解析当前管理员，勿传 userId
 */
const loadProfile = async () => {
  pageLoading.value = true
  try {
    const data = await getProfile()
    Object.assign(profile, data)
    Object.assign(profileForm, {
      nickname: data.nickname,
      phone: data.phone,
      email: data.email,
      gender: data.gender,
    })
  } catch {
    ElMessage.error('加载个人资料失败')
  } finally {
    pageLoading.value = false
  }
}

/** PUT /api/system/user/profile */
const submitProfile = async () => {
  const valid = await profileFormRef.value?.validate().catch(() => false)
  if (!valid) return

  profileSubmitting.value = true
  try {
    const updated = await updateProfile({ ...profileForm })
    Object.assign(profile, updated)
    ElMessage.success('资料已保存')
  } catch {
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    profileSubmitting.value = false
  }
}

/**
 * PUT /api/system/user/profile/updatePwd
 * 成功后清除 Token 并跳转登录页
 */
const submitPassword = async () => {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return

  pwdSubmitting.value = true
  try {
    await updatePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    clearToken()
    setTimeout(() => {
      router.replace('/login')
    }, 800)
  } catch (e) {
    ElMessage.error(e?.message || '修改密码失败')
  } finally {
    pwdSubmitting.value = false
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.warning('仅支持 JPG/PNG 格式')
    return false
  }
  if (!isLt2M) {
    ElMessage.warning('图片大小不能超过 2MB')
    return false
  }
  profile.avatar = URL.createObjectURL(file)
  ElMessage.success('头像已更新（联调时上传至 OSS 后保存 URL）')
  return false
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-page {
  min-height: calc(100vh - 120px);
}

.panel-card {
  border-radius: 8px;
  border: none;
  margin-bottom: 16px;
}

.info-card {
  text-align: center;
}

.avatar-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 24px 16px;
  border-bottom: 1px solid #ebeef5;
}

.static-info {
  list-style: none;
  margin: 0;
  padding: 20px 24px;
  text-align: left;
}

.static-info li {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f2f6fc;
  font-size: 14px;
}

.static-info li:last-child {
  border-bottom: none;
}

.static-info .label {
  color: #909399;
}

.static-info .value {
  color: #303133;
  font-weight: 500;
}

.profile-form {
  max-width: 520px;
  padding: 16px 8px 8px;
}
</style>
