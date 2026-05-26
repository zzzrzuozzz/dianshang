<!--
  快递单模板预览与物理打印模块（实训方案入口组件）
  用法：<ExpressWaybill :order-id="orderNo" /> 或 <ExpressWaybillDialog ref="dialogRef" />
-->
<template>
  <ExpressWaybill v-bind="$attrs" />
</template>

<script setup>
import ExpressWaybill from './ExpressWaybill.vue'
</script>
