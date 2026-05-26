<template>
  <el-form ref="formRef" :model="pushForm" :rules="rules" label-width="120px" class="push-form">
    <template v-if="mode === 'system'">
      <el-form-item label="消息分类" prop="category">
        <el-select v-model="pushForm.category" placeholder="请选择分类" style="width: 400px">
          <el-option v-for="c in messageCategories" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="消息标题" prop="title">
        <el-input v-model="pushForm.title" placeholder="请输入（限60字）" maxlength="60" show-word-limit style="width: 400px" />
      </el-form-item>
      <el-form-item label="简介">
        <el-input v-model="pushForm.intro" type="textarea" :rows="3" placeholder="请输入（限100字）" maxlength="100" show-word-limit style="width: 400px" />
      </el-form-item>
    </template>

    <template v-else>
      <el-form-item label="消息标题" prop="title">
        <el-input v-model="pushForm.title" placeholder="请输入（限60字）" maxlength="60" show-word-limit style="width: 400px" />
      </el-form-item>
      <el-form-item label="消息内容" prop="stationContent">
        <el-input v-model="pushForm.stationContent" type="textarea" :rows="4" placeholder="请输入（限100字）" maxlength="100" show-word-limit style="width: 400px" />
      </el-form-item>
      <el-form-item label="推送方式">
        <el-checkbox-group v-model="pushForm.pushMethod">
          <el-checkbox value="assign">指定推送</el-checkbox>
          <el-checkbox value="tag">标签推送</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
    </template>

    <template v-if="mode === 'system'">
      <el-form-item label="跳转类型" prop="jumpType">
        <el-radio-group v-model="pushForm.jumpType">
          <el-radio value="TEXT">富文本</el-radio>
          <el-radio value="INNER">内链</el-radio>
          <el-radio value="OUTER">外链</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="pushForm.jumpType === 'TEXT'" label="详情" prop="detail">
        <div class="rich-editor-wrap">
          <div class="rich-toolbar">
            <el-button size="small">B</el-button>
            <el-button size="small">对齐</el-button>
            <el-button size="small">列表</el-button>
            <el-button size="small">图片</el-button>
            <el-button size="small">视频</el-button>
            <el-button size="small">链接</el-button>
          </div>
          <el-input v-model="pushForm.detail" type="textarea" :rows="8" placeholder="富文本内容（联调时替换为 WangEditor 等）" />
        </div>
      </el-form-item>

      <template v-if="pushForm.jumpType === 'INNER'">
        <el-form-item label="跳转页面">
          <el-radio-group v-model="pushForm.innerLinkType">
            <el-radio v-for="opt in innerLinkOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="">
          <el-input v-model="pushForm.jumpUrl" placeholder="请输入（限60字）" maxlength="60" show-word-limit style="width: 400px" />
          <p class="field-hint">内链、外链需填写；富文本无需填写</p>
        </el-form-item>
      </template>

      <el-form-item v-if="pushForm.jumpType === 'OUTER'" label="外链地址" prop="jumpUrl">
        <el-input v-model="pushForm.jumpUrl" placeholder="请输入外链地址（限60字）" maxlength="60" style="width: 400px" />
      </el-form-item>

      <el-form-item v-if="pushForm.jumpType === 'TEXT'" label="封面图">
        <div class="cover-list">
          <div v-for="(img, idx) in pushForm.coverImages" :key="idx" class="cover-item">
            <img :src="img" alt="cover" />
            <span class="cover-remove" @click="removeCover(idx)">×</span>
          </div>
          <div v-if="pushForm.coverImages.length < 5" class="cover-upload" @click="addMockCover">
            <el-icon><Plus /></el-icon>
            <span>上传图片</span>
          </div>
        </div>
        <p class="field-hint">仅支持 .jpg .png 格式，最多5张</p>
      </el-form-item>
    </template>

    <AudienceTargetFields :form="pushForm" @estimate-change="calcEstimate" />

    <el-form-item label="推送时间" prop="sendType">
      <el-radio-group v-model="pushForm.sendType">
        <el-radio :value="1">立即推送</el-radio>
        <el-radio :value="2">定时推送</el-radio>
      </el-radio-group>
      <el-date-picker
        v-if="pushForm.sendType === 2"
        v-model="pushForm.publishTime"
        type="datetime"
        placeholder="请选择开始时间"
        value-format="YYYY-MM-DD HH:mm:ss"
        style="margin-left: 12px"
      />
    </el-form-item>

    <el-form-item v-if="mode === 'system'" label="同时生成">
      <el-checkbox-group v-model="pushForm.generateTypes">
        <el-checkbox value="ad">广告位</el-checkbox>
        <el-checkbox value="article">文章</el-checkbox>
        <el-checkbox value="topic">主题</el-checkbox>
      </el-checkbox-group>
      <p class="field-hint">生成后，请到对应模板，进行发布</p>
    </el-form-item>

    <el-form-item label="APP推送">
      <el-radio-group v-model="pushForm.appPush">
        <el-radio :value="false">不推送</el-radio>
        <el-radio :value="true">推送</el-radio>
      </el-radio-group>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" class="submit-btn" :loading="submitting" @click="submitNotification">提交</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AudienceTargetFields from './AudienceTargetFields.vue'
import { createEmptyPushForm, messageCategories, innerLinkOptions } from '@/mock/ops'

const props = defineProps({
  mode: { type: String, default: 'system' },
  msgType: { type: String, default: 'SYSTEM' },
})

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const pushForm = reactive(createEmptyPushForm())

const rules = {
  category: [{ required: true, message: '请选择消息分类', trigger: 'change' }],
  title: [{ required: true, message: '请输入消息标题', trigger: 'blur' }],
  jumpType: [{ required: true, message: '请选择跳转类型', trigger: 'change' }],
  sendType: [{ required: true, message: '请选择推送时间', trigger: 'change' }],
  stationContent: [{ required: true, message: '请输入消息内容', trigger: 'blur' }],
}

const calcEstimate = async () => {
  await new Promise((r) => setTimeout(r, 200))
  pushForm.estimatedUsers = 4000 + Math.floor(Math.random() * 4000)
}

const addMockCover = () => {
  pushForm.coverImages.push('https://picsum.photos/120/120?random=' + Date.now())
}
const removeCover = (idx) => pushForm.coverImages.splice(idx, 1)

/**
 * POST /api/ops/notification/save
 * 若标签圈定用户量巨大，后端建议采用 RabbitMQ / 线程池异步批量触达，
 * 避免长事务锁表或数据库 I/O 阻塞。
 */
const submitNotification = async () => {
  await formRef.value?.validate().catch(() => {
    ElMessage.warning('请完善必填项')
    return Promise.reject()
  })
  submitting.value = true
  try {
    await new Promise((r) => setTimeout(r, 600))
    const payload = { ...pushForm, msg_type: props.msgType }
    console.log('[mock] save notification', payload)
    ElMessage.success('提交成功')
    router.back()
  } finally {
    submitting.value = false
  }
}

onMounted(calcEstimate)
</script>

<style scoped>
.push-form { max-width: 900px; }
.rich-editor-wrap { width: 100%; border: 1px solid #dcdfe6; border-radius: 4px; }
.rich-toolbar { padding: 8px; border-bottom: 1px solid #ebeef5; display: flex; gap: 4px; }
.field-hint { font-size: 12px; color: #909399; margin-top: 4px; }
.cover-list { display: flex; flex-wrap: wrap; gap: 12px; }
.cover-item { position: relative; width: 100px; height: 100px; border-radius: 4px; overflow: hidden; }
.cover-item img { width: 100%; height: 100%; object-fit: cover; }
.cover-remove { position: absolute; top: 4px; right: 4px; width: 20px; height: 20px; background: #f56c6c; color: #fff; border-radius: 50%; text-align: center; line-height: 18px; cursor: pointer; font-size: 14px; }
.cover-upload { width: 100px; height: 100px; border: 1px dashed #dcdfe6; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #909399; cursor: pointer; font-size: 12px; }
.submit-btn { width: 100%; max-width: 400px; height: 44px; }
</style>
