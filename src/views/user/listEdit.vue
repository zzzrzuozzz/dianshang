<template>
  <div v-loading="loading" class="user-edit-page">
    <el-card shadow="never" class="panel-card">
      <template #header><span class="section-title">编辑资料</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="edit-form">
        <el-form-item label="用户手机" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" style="width: 320px" />
        </el-form-item>
        <el-form-item label="会员等级" prop="level">
          <el-select v-model="form.level" placeholder="请选择类型" style="width: 320px">
            <el-option v-for="opt in memberLevelOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户头像">
          <div class="avatar-upload">
            <div class="upload-trigger" @click="mockUpload">
              <el-icon :size="28"><Plus /></el-icon>
              <span>上传照片</span>
            </div>
            <div v-if="form.avatar" class="upload-preview">
              <el-image :src="form.avatar" fit="cover" class="preview-img" />
              <span class="remove-btn" @click="form.avatar = ''">×</span>
            </div>
          </div>
          <p class="form-hint">只支持 jpg png 格式，最多1张</p>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="secret">保密</el-radio>
            <el-radio value="male">男</el-radio>
            <el-radio value="female">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="城市">
          <el-cascader v-model="form.city" :options="cityOptions" placeholder="请选择地区" style="width: 320px" />
        </el-form-item>
        <el-form-item label="登录密码">
          <el-input v-model="form.password" type="password" show-password placeholder="留空则不修改" style="width: 320px" />
        </el-form-item>
        <el-form-item label="权限">
          <el-checkbox-group v-model="form.permissions">
            <el-checkbox value="normal">正常</el-checkbox>
            <el-checkbox value="withdraw_banned">禁止提现</el-checkbox>
            <el-checkbox value="login_banned">禁止登录</el-checkbox>
            <el-checkbox value="lottery_banned">禁止抽奖</el-checkbox>
            <el-checkbox value="order_banned">禁止下单</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="100" show-word-limit placeholder="请输入 (限100字)" style="width: 480px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="saving" @click="submitUserEdit">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getMockUserDetail, memberLevelOptions, cityOptions } from '@/mock/user'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const formRef = ref(null)

const form = reactive({
  phone: '',
  level: '',
  avatar: '',
  gender: 'secret',
  city: [],
  password: '',
  permissions: ['normal'],
  remark: '',
})

const rules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  level: [{ required: true, message: '请选择会员等级', trigger: 'change' }],
}

const mockThumb = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const loadUser = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 300))
    const data = getMockUserDetail(route.params.userId)
    Object.assign(form, {
      phone: data.account,
      level: data.levelKey,
      avatar: data.avatar,
      gender: data.gender === '女' ? 'female' : data.gender === '男' ? 'male' : 'secret',
      city: ['guangdong', 'shenzhen'],
      permissions: data.permissions || ['normal'],
      remark: data.remark === '-' ? '' : data.remark,
    })
  } finally {
    loading.value = false
  }
}

const mockUpload = () => {
  form.avatar = mockThumb
  ElMessage.success('上传成功（模拟）')
}

/**
 * POST /api/user/update
 */
const submitUserEdit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    // await axios.post('/api/user/update', { userId: route.params.userId, ...form })
    await new Promise((r) => setTimeout(r, 400))
    ElMessage.success('保存成功')
    router.push(`/user/detail/${route.params.userId}`)
  } finally {
    saving.value = false
  }
}

onMounted(loadUser)
</script>

<style scoped>
.user-edit-page { min-height: calc(100vh - 120px); max-width: 720px; margin: 0 auto; }
.panel-card { border-radius: 8px; border: none; }
.section-title::before { content: ''; display: inline-block; width: 4px; height: 14px; margin-right: 8px; background: #409eff; border-radius: 2px; vertical-align: middle; }
.avatar-upload { display: flex; gap: 12px; }
.upload-trigger { width: 100px; height: 100px; border: 1px dashed #dcdfe6; border-radius: 6px; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; color: #909399; font-size: 12px; }
.upload-preview { position: relative; width: 100px; height: 100px; }
.preview-img { width: 100%; height: 100%; border-radius: 6px; }
.remove-btn { position: absolute; top: 4px; right: 4px; width: 20px; height: 20px; background: #f56c6c; color: #fff; border-radius: 50%; text-align: center; line-height: 18px; cursor: pointer; }
.form-hint { margin: 4px 0 0; font-size: 12px; color: #909399; }
.submit-btn { width: 100%; max-width: 400px; }
</style>
