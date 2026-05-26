<template>
  <template v-for="item in menus" :key="item.id">
    <el-sub-menu v-if="item.menuType === 'M'" :index="String(item.id)">
      <template #title>
        <el-icon v-if="item.icon"><component :is="resolveMenuIcon(item.icon)" /></el-icon>
        <span>{{ item.menuName }}</span>
      </template>
      <DynamicMenu :menus="item.children || []" />
    </el-sub-menu>
    <el-menu-item v-else-if="item.menuType === 'C' && item.path" :index="item.path">
      <el-icon v-if="item.icon"><component :is="resolveMenuIcon(item.icon)" /></el-icon>
      <template #title>{{ item.menuName }}</template>
    </el-menu-item>
  </template>
</template>

<script setup>
import DynamicMenu from './DynamicMenu.vue'
import { resolveMenuIcon } from '@/utils/menuIcons'

defineProps({
  menus: { type: Array, default: () => [] },
})
</script>
