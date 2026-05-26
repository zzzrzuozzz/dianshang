<template>
  <div class="placeholder">
    <el-empty :description="`${title} 页面开发中`" />
    <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta.title || '功能')
</script>

<style scoped>
.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 16px;
}
</style>
