<template>
  <div class="simple-rich-editor">
    <div class="toolbar">
      <el-button-group size="small">
        <el-button @click="exec('bold')"><strong>B</strong></el-button>
        <el-button @click="exec('italic')"><em>I</em></el-button>
        <el-button @click="exec('underline')"><u>U</u></el-button>
      </el-button-group>
      <el-button-group size="small">
        <el-button @click="exec('insertUnorderedList')">列表</el-button>
        <el-button @click="insertLink">链接</el-button>
      </el-button-group>
      <el-button size="small" @click="clearFormat">清除格式</el-button>
    </div>
    <div
      ref="editorRef"
      class="editor-body"
      contenteditable="true"
      :data-placeholder="placeholder"
      :style="{ minHeight }"
      @input="onInput"
      @blur="onInput"
    />
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请输入详情内容，支持基础富文本格式' },
  minHeight: { type: String, default: '200px' },
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref()

const onInput = () => {
  if (!editorRef.value) return
  emit('update:modelValue', editorRef.value.innerHTML)
}

const exec = (cmd) => {
  editorRef.value?.focus()
  document.execCommand(cmd, false, null)
  onInput()
}

const insertLink = () => {
  ElMessageBox.prompt('请输入链接地址', '插入链接', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: 'https://',
  })
    .then(({ value }) => {
      if (!value) return
      editorRef.value?.focus()
      document.execCommand('createLink', false, value)
      onInput()
    })
    .catch(() => {})
}

const clearFormat = () => {
  editorRef.value?.focus()
  document.execCommand('removeFormat', false, null)
  onInput()
}

const syncFromModel = (html) => {
  if (editorRef.value && editorRef.value.innerHTML !== (html || '')) {
    editorRef.value.innerHTML = html || ''
  }
}

watch(
  () => props.modelValue,
  (val) => syncFromModel(val),
)

onMounted(() => syncFromModel(props.modelValue))
</script>

<style scoped>
.simple-rich-editor {
  width: 100%;
  max-width: 720px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}
.editor-body {
  padding: 12px;
  outline: none;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
}
.editor-body:empty::before {
  content: attr(data-placeholder);
  color: #c0c4cc;
}
.editor-body :deep(a) {
  color: #409eff;
}
</style>
