<template>
  <div class="login-page">
    <div class="login-card">
      <header class="login-header">
        <svg
          class="login-logo"
          viewBox="0 0 32 32"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          aria-hidden="true"
        >
          <rect x="6" y="10" width="20" height="14" rx="4" fill="#409EFF" />
          <rect x="10" y="6" width="12" height="8" rx="3" fill="#409EFF" />
          <circle cx="12" cy="17" r="2" fill="#fff" />
          <circle cx="20" cy="17" r="2" fill="#fff" />
          <rect x="13" y="21" width="6" height="2" rx="1" fill="#fff" />
          <rect x="4" y="14" width="3" height="6" rx="1.5" fill="#409EFF" />
          <rect x="25" y="14" width="3" height="6" rx="1.5" fill="#409EFF" />
        </svg>
        <h1 class="login-title">暴走电商 | 管理后台</h1>
      </header>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入账号"
            size="large"
            clearable
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item class="login-btn-item">
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            native-type="submit"
          >
            <span class="login-btn-text">登录</span>
            <el-icon class="login-btn-icon"><Lightning /></el-icon>
          </el-button>
        </el-form-item>
      </el-form>

      <footer class="login-footer">
        <a href="javascript:void(0)" class="forgot-link" @click.prevent="handleForgotPassword">
          忘记密码？
        </a>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, Lightning } from '@element-plus/icons-vue'

interface LoginForm {
  username: string
  password: string
}

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive<LoginForm>({
  username: '',
  password: '',
})

const rules: FormRules<LoginForm> = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await new Promise((resolve) => setTimeout(resolve, 600))
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}

const handleForgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}
</script>

<style scoped>
.login-page {
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

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 40px 36px 28px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
}

.login-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 32px;
}

.login-logo {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
}

.login-title {
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
  line-height: 1.4;
  white-space: nowrap;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-form-item__error) {
  padding-top: 4px;
}

.login-btn-item {
  margin-top: 8px;
  margin-bottom: 0;
}

.login-btn-item :deep(.el-form-item__content) {
  line-height: normal;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  border: none;
  position: relative;
}

.login-btn-text {
  flex: 1;
  text-align: center;
}

.login-btn :deep(span) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.login-btn-icon {
  position: absolute;
  right: 16px;
  font-size: 18px;
}

.login-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.forgot-link {
  font-size: 13px;
  color: #909399;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #409eff;
}
</style>
