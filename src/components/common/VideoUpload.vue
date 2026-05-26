<template>
  <div class="video-upload">
    <div v-if="modelValue" class="thumb has-video">
      <div class="video-icon">
        <el-icon :size="32"><VideoPlay /></el-icon>
      </div>
      <span class="thumb-remove" @click="clear">×</span>
    </div>
    <div
      v-else
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
      accept=".mp4,video/mp4"
      @change="onFileChange"
    />
    <p v-if="hint" class="hint">{{ hint }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Plus, VideoPlay } from '@element-plus/icons-vue'
import { uploadVideo } from '@/api/upload'

const props = defineProps({
  modelValue: { type: String, default: '' },
  biz: { type: String, default: 'product' },
  addLabel: { type: String, default: '上传视频' },
  hint: { type: String, default: '仅支持 MP4 格式' },
})

const emit = defineEmits(['update:modelValue'])

const fileInputRef = ref()
const uploading = ref(false)

const pickFile = () => {
  if (uploading.value) return
  fileInputRef.value?.click()
}

const clear = () => emit('update:modelValue', '')

const onFileChange = async (e) => {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file) return
  uploading.value = true
  try {
    const { url } = await uploadVideo(file, props.biz)
    emit('update:modelValue', url)
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.video-upload {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.thumb {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
}
.video-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #303133;
  color: #fff;
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
.thumb-add.uploading {
  pointer-events: none;
  opacity: 0.7;
}
.file-input {
  display: none;
}
.hint {
  margin: 0;
  font-size: 12px;
  color: #909399;
}
</style>
