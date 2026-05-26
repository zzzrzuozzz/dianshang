<template>
  <div class="single-image-upload">
    <div v-if="modelValue" class="thumb has-image">
      <el-image :src="modelValue" fit="cover" class="thumb-img" />
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
      accept=".jpg,.jpeg,.png,image/jpeg,image/png"
      @change="onFileChange"
    />
    <p v-if="hint" class="hint">{{ hint }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { uploadImage } from '@/api/upload'

const props = defineProps({
  modelValue: { type: String, default: '' },
  biz: { type: String, default: 'product' },
  addLabel: { type: String, default: '上传图片' },
  hint: { type: String, default: '' },
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
    const { url } = await uploadImage(file, props.biz)
    emit('update:modelValue', url)
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.single-image-upload {
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
