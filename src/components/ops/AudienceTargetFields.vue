<template>
  <div class="audience-fields">
    <el-form-item label="推送会员">
      <el-checkbox-group v-model="form.memberLevels">
        <el-checkbox v-for="lv in memberLevelOptions" :key="lv.value" :value="lv.value">{{ lv.label }}</el-checkbox>
      </el-checkbox-group>
    </el-form-item>

    <el-form-item label="限制地区">
      <AreaCascader
        v-model="form.regions"
        multiple
        placeholder="不选则面向全国；可多选省/市/区"
      />
      <p class="region-tip">良性调联：按需懒加载区划，用于秒杀/短信等地域圈选（示例字段）</p>
    </el-form-item>

    <el-form-item label="推送标签">
      <el-link type="primary" class="tag-link" @click="$emit('edit-tags')">新增/编辑标签 &gt;</el-link>
      <div v-for="group in tagGroups" :key="group.key" class="tag-row">
        <span class="tag-label">{{ group.label }}：</span>
        <el-checkbox-group v-model="form.tags[group.key]" @change="onTagsChange">
          <el-checkbox v-for="opt in group.options" :key="opt.value" :value="opt.value">{{ opt.label }}</el-checkbox>
        </el-checkbox-group>
      </div>
      <p class="estimate-tip">已选择：{{ form.estimatedUsers }}位用户</p>
    </el-form-item>
  </div>
</template>

<script setup>
import AreaCascader from '@/components/AreaCascader/index.vue'
import { memberLevelOptions, tagGroups } from '@/mock/ops'

defineProps({
  form: { type: Object, required: true },
})

const emit = defineEmits(['edit-tags', 'estimate-change'])

const onTagsChange = () => {
  emit('estimate-change')
}
</script>

<style scoped>
.tag-link { margin-bottom: 8px; display: inline-block; }
.tag-row { display: flex; flex-wrap: wrap; align-items: flex-start; margin-bottom: 10px; font-size: 13px; }
.tag-label { min-width: 110px; color: #606266; line-height: 32px; }
.estimate-tip { color: #f56c6c; font-size: 13px; margin: 8px 0 0; }
.region-tip { color: #909399; font-size: 12px; margin: 6px 0 0; line-height: 1.4; }
</style>
