<template>
  <div class="auth-page">
    <div class="auth-card">
      <header class="auth-header">
        <svg class="auth-logo" viewBox="0 0 32 32" fill="none" aria-hidden="true">
          <rect x="6" y="10" width="20" height="14" rx="4" fill="#409EFF" />
          <rect x="10" y="6" width="12" height="8" rx="3" fill="#409EFF" />
          <circle cx="12" cy="17" r="2" fill="#fff" />
          <circle cx="20" cy="17" r="2" fill="#fff" />
        </svg>
        <h1 class="auth-title">{{ shopName }} | 找回密码</h1>
      </header>

      <p class="auth-tip">请填写注册时的手机号/邮箱及邀请码，验证通过后可重置密码。</p>

      <el-form ref="formRef" :model="form" :rules="rules" class="auth-form" @submit.prevent="handleSubmit">
        <el-form-item label="验证方式" prop="accountType">
          <el-radio-group v-model="form.accountType">
            <el-radio value="PHONE">手机号</el-radio>
            <el-radio value="EMAIL">邮箱</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item :label="form.accountType === 'PHONE' ? '手机号' : '邮箱'" prop="account">
          <el-input
            v-model="form.account"
            :placeholder="form.accountType === 'PHONE' ? '注册时填写的手机号' : '注册时填写的邮箱'"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item label="邀请码" prop="inviteCode">
          <el-input v-model="form.inviteCode" placeholder="注册时使用的邀请码" size="large" clearable />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="不少于6个字符" size="large" show-password />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入新密码" size="large" show-password />
        </el-form-item>

        <el-form-item class="auth-btn-item">
          <el-button type="primary" size="large" class="auth-btn" :loading="loading" native-type="submit">
            重置密码
          </el-button>
        </el-form-item>
      </el-form>

      <footer class="auth-footer">
        <router-link to="/login" class="auth-link">返回登录</router-link>
        <router-link to="/register" class="auth-link">没有账号？去注册</router-link>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { forgotPassword } from '@/api/auth'
import { usePlatformConfig } from '@/composables/usePlatformConfig'

const { shopName, loadPublicPlatformConfig } = usePlatformConfig()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  accountType: 'PHONE',
  account: '',
  inviteCode: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirm = (_rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  accountType: [{ required: true, message: '请选择验证方式', trigger: 'change' }],
  account: [{ required: true, message: '请输入手机号或邮箱', trigger: 'blur' }],
  inviteCode: [{ required: true, message: '请输入邀请码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码不少于6个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await forgotPassword({
      accountType: form.accountType,
      account: form.account.trim(),
      inviteCode: form.inviteCode.trim(),
      newPassword: form.newPassword,
    })
    ElMessage.success('密码已重置，请使用新密码登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}

onMounted(loadPublicPlatformConfig)
</script>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
  background:
    linear-gradient(135deg, rgba(15, 23, 42, 0.72), rgba(30, 41, 59, 0.55)),
    url('https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1920&q=80')
      center / cover no-repeat;
}

.auth-card {
  width: 100%;
  max-width: 480px;
  padding: 36px 32px 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
}

.auth-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 12px;
}

.auth-logo {
  width: 32px;
  height: 32px;
}

.auth-title {
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
}

.auth-tip {
  font-size: 13px;
  color: #909399;
  text-align: center;
  margin: 0 0 20px;
  line-height: 1.6;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.auth-btn-item :deep(.el-form-item__content) {
  justify-content: center;
}

.auth-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.auth-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
}

.auth-link {
  font-size: 13px;
  color: #409eff;
  text-decoration: none;
}
</style>
