<template>
  <div v-loading="loading" class="promo-page">
    <el-card shadow="never" class="panel-card">
      <div class="section-header">
        <span class="section-title">时间段设置</span>
        <el-button type="primary" @click="openDialog()">+ 添加时间段</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="编号" width="100" align="center" />
        <el-table-column prop="name" label="秒杀时段名称" min-width="140" />
        <el-table-column prop="start" label="每日开始时间" width="130" align="center" />
        <el-table-column prop="end" label="每日结束时间" width="130" align="center" />
        <el-table-column label="启用" width="120" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" active-text="已开启" inactive-text="不启用" @change="toggleEnable(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button size="small" class="btn-blue" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" class="btn-red" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑时间段' : '添加时间段'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="dialog.form" :rules="rules" label-width="120px">
        <el-form-item label="秒杀时段名称" prop="name">
          <el-input v-model="dialog.form.name" placeholder="请输入（限60字）" maxlength="60" />
        </el-form-item>
        <el-form-item label="每日开始时间" prop="start">
          <div class="time-inputs">
            <el-input-number v-model="dialog.form.startH" :min="0" :max="23" controls-position="right" />
            <span>时</span>
            <el-input-number v-model="dialog.form.startM" :min="0" :max="59" controls-position="right" />
            <span>分</span>
            <el-input-number v-model="dialog.form.startS" :min="0" :max="59" controls-position="right" />
            <span>秒</span>
          </div>
        </el-form-item>
        <el-form-item label="每日结束时间" prop="end">
          <div class="time-inputs">
            <el-input-number v-model="dialog.form.endH" :min="0" :max="23" controls-position="right" />
            <span>时</span>
            <el-input-number v-model="dialog.form.endM" :min="0" :max="59" controls-position="right" />
            <span>分</span>
            <el-input-number v-model="dialog.form.endS" :min="0" :max="59" controls-position="right" />
            <span>秒</span>
          </div>
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="dialog.form.enabled" active-text="已开启" inactive-text="不启用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">关闭</el-button>
        <el-button type="primary" :loading="dialog.saving" @click="saveTimeInterval">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteSeckillTime,
  fetchSeckillTimeList,
  saveSeckillTime,
  toggleSeckillTime,
} from '@/api/promotion'

const loading = ref(false)
const tableData = ref([])
const formRef = ref(null)

const dialog = reactive({
  visible: false,
  saving: false,
  isEdit: false,
  form: { id: '', name: '', startH: 8, startM: 0, startS: 0, endH: 10, endM: 0, endS: 0, enabled: true },
})

const rules = { name: [{ required: true, message: '请输入时段名称', trigger: 'blur' }] }

const pad = (n) => String(n).padStart(2, '0')
const formatTime = (h, m, s) => `${pad(h)}:${pad(m)}:${pad(s)}`
const parseTime = (str) => {
  const [h, m, s] = (str || '00:00:00').split(':').map(Number)
  return { h: h || 0, m: m || 0, s: s || 0 }
}

const fetchList = async () => {
  loading.value = true
  try {
    tableData.value = await fetchSeckillTimeList()
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  dialog.isEdit = !!row
  if (row) {
    const s = parseTime(row.start)
    const e = parseTime(row.end)
    Object.assign(dialog.form, { id: row.id, name: row.name, enabled: row.enabled, startH: s.h, startM: s.m, startS: s.s, endH: e.h, endM: e.m, endS: e.s })
  } else {
    Object.assign(dialog.form, { id: '', name: '', enabled: true, startH: 8, startM: 0, startS: 0, endH: 10, endM: 0, endS: 0 })
  }
  dialog.visible = true
}

const saveTimeInterval = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  dialog.saving = true
  try {
    await saveSeckillTime({
      id: dialog.form.id || undefined,
      name: dialog.form.name,
      start: formatTime(dialog.form.startH, dialog.form.startM, dialog.form.startS),
      end: formatTime(dialog.form.endH, dialog.form.endM, dialog.form.endS),
      enabled: dialog.form.enabled,
    })
    ElMessage.success('保存成功')
    dialog.visible = false
    fetchList()
  } finally {
    dialog.saving = false
  }
}

const toggleEnable = async (row) => {
  await toggleSeckillTime(row.id, row.enabled)
  ElMessage.success(row.enabled ? '已开启' : '已关闭')
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteSeckillTime(row.id)
      ElMessage.success('删除成功')
      fetchList()
    })
    .catch(() => {})
}

onMounted(fetchList)
</script>

<style scoped>
.promo-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.section-header { display: flex; justify-content: space-between; align-items: center; }
.section-title::before { content: ''; display: inline-block; width: 4px; height: 14px; margin-right: 8px; background: #409eff; border-radius: 2px; vertical-align: middle; }
.time-inputs { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.time-inputs :deep(.el-input-number) { width: 72px; }
.btn-blue { color: #409eff; border-color: #b3d8ff; background: #ecf5ff; }
.btn-red { color: #f56c6c; border-color: #fbc4c4; background: #fef0f0; }
</style>
