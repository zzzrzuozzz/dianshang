<template>
  <div class="push-edit-page">
    <el-card shadow="never" class="panel-card">
      <template #header>
        <span class="section-title">{{ isEdit ? '编辑推送' : '新增推送' }}</span>
      </template>
      <SmsPushForm />
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SmsPushForm from '@/components/ops/SmsPushForm.vue'

const route = useRoute()
const isEdit = computed(() => Boolean(route.params.id))
</script>

<style scoped>
.push-edit-page { min-height: calc(100vh - 120px); }
.panel-card { border-radius: 8px; border: none; }
.section-title { font-weight: 600; }
</style>
