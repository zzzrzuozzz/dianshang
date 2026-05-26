<template>
  <div class="push-edit-page">
    <el-card shadow="never" class="panel-card">
      <template #header>
        <span class="section-title">新增推送</span>
      </template>
      <PushNotificationForm :mode="mode" :msg-type="msgType" />
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PushNotificationForm from '@/components/ops/PushNotificationForm.vue'

const route = useRoute()
const mode = computed(() => (route.meta.mode === 'station' ? 'station' : 'system'))
const msgType = computed(() => (mode.value === 'station' ? 'STATION' : 'SYSTEM'))
</script>

<style scoped>
.push-edit-page { min-height: calc(100vh - 120px); }
.panel-card { border-radius: 8px; border: none; }
.section-title { font-weight: 600; }
</style>
