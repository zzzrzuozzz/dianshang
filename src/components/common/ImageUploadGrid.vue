<template>
  <div class="image-upload-grid">
    <div v-for="(url, idx) in modelValue" :key="url + idx" class="thumb">
      <el-image :src="url" fit="cover" class="thumb-img" />
      <span class="thumb-remove" title="移除" @click="removeAt(idx)">×</span>
    </div>
    <div
      v-if="modelValue.length < max"
      class="thumb thumb-add"
      :class="{ uploading }"
      @click="pickFile"
    >
      <el-icon v-if="!uploading"><Plus /></el-icon>
      <span>{{ uploading ? '上传中…' : addLabel }}</span>
    </div>
    <input
      ref="fileInputRef"
      type="file"
      class="file-input"
      accept=".jpg,.jpeg,.png,image/jpeg,image/png"
      @change="onFileChange"
    />
    <p v-if="hint" class="hint">{{ hint }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { uploadImage } from '@/api/upload'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  max: { type: Number, default: 5 },
  biz: { type: String, default: 'ops' },
  addLabel: { type: String, default: '上传图片' },
  hint: { type: String, default: '仅支持 .jpg .png 格式' },
})

const emit = defineEmits(['update:modelValue'])

const fileInputRef = ref()
const uploading = ref(false)

const pickFile = () => {
  if (uploading.value) return
  fileInputRef.value?.click()
}

const removeAt = (idx) => {
  const next = [...props.modelValue]
  next.splice(idx, 1)
  emit('update:modelValue', next)
}

const onFileChange = async (e) => {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file) return
  if (props.modelValue.length >= props.max) {
    ElMessage.warning(`最多上传 ${props.max} 张`)
    return
  }
  uploading.value = true
  try {
    const { url } = await uploadImage(file, props.biz)
    emit('update:modelValue', [...props.modelValue, url])
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.image-upload-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-start;
}
.thumb {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}
.thumb-img {
  width: 100%;
  height: 100%;
}
.thumb-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  background: #f56c6c;
  color: #fff;
  border-radius: 50%;
  text-align: center;
  line-height: 18px;
  cursor: pointer;
  font-size: 14px;
}
.thumb-add {
  border: 1px dashed #dcdfe6;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  cursor: pointer;
  font-size: 12px;
  gap: 4px;
}
.thumb-add:hover {
  border-color: #409eff;
  color: #409eff;
}
.thumb-add.uploading {
  pointer-events: none;
  opacity: 0.7;
}
.file-input {
  display: none;
}
.hint {
  flex-basis: 100%;
  font-size: 12px;
  color: #909399;
  margin: 0;
}
</style>
