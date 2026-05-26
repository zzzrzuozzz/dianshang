<template>
  <div class="stats-card-header">
    <span class="stats-card-header__title">{{ title }}</span>
    <div class="stats-card-header__actions">
      <el-radio-group v-model="timeRange" size="small" @change="$emit('time-change', timeRange)">
        <el-radio-button value="week">本周</el-radio-button>
        <el-radio-button value="month">本月</el-radio-button>
      </el-radio-group>
      <el-date-picker
        v-model="customRange"
        type="daterange"
        size="small"
        range-separator="至"
        start-placeholder="开始"
        end-placeholder="结束"
        style="width: 220px; margin-left: 8px"
        @change="$emit('custom-change', customRange)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  title: { type: String, required: true },
})

defineEmits(['time-change', 'custom-change'])

const timeRange = ref('week')
const customRange = ref(null)
</script>

<style scoped>
.stats-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}
.stats-card-header__title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}
.stats-card-header__actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
</style>
