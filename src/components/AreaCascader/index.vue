<template>
  <el-cascader
    v-model="innerValue"
    :props="cascaderProps"
    :placeholder="placeholder"
    :multiple="multiple"
    :clearable="clearable"
    :disabled="disabled"
    :collapse-tags="multiple"
    :collapse-tags-tooltip="multiple"
    filterable
    style="width: 100%"
    @change="onChange"
  />
</template>

<script setup>
/**
 * 省市区三级级联（异步懒加载）
 *
 * 良性调联说明：坚决避免一次性加载全国几万条行政数据。
 * 采用节点触发、按需拉取。后端 MySQL/H2 在 parent_code 字段上建立索引，
 * 确保每次懒加载查询在 10ms 内响应。
 */
import { computed, toRef } from 'vue'
import { fetchRegionsByParent } from '@/api/region'

const props = defineProps({
  modelValue: {
    type: [Array, String],
    default: () => [],
  },
  /** 单选为代码数组；多选为二维数组 */
  multiple: {
    type: Boolean,
    default: false,
  },
  placeholder: {
    type: String,
    default: '请选择省 / 市 / 区',
  },
  clearable: {
    type: Boolean,
    default: true,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

const innerValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const multipleRef = toRef(props, 'multiple')

const cascaderProps = computed(() => ({
  lazy: true,
  multiple: multipleRef.value,
  value: 'value',
  label: 'label',
  lazyLoad(node, resolve) {
    const { level, value } = node
    const parentCode = level === 0 ? '0' : value
    fetchRegionsByParent(parentCode)
      .then((list) => {
        const nodes = (list || []).map((item) => ({
          value: item.code,
          label: item.name,
          leaf: level >= 2 || item.level >= 3,
        }))
        resolve(nodes)
      })
      .catch(() => resolve([]))
  },
}))

const onChange = (val) => {
  emit('change', val)
}
</script>
