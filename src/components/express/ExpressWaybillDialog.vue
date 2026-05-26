<template>
  <el-dialog
    v-model="visible"
    title="快递单预览"
    width="480px"
    destroy-on-close
    class="express-dialog"
    @closed="onClosed"
  >
    <ExpressWaybill
      v-if="visible && orderId"
      ref="waybillRef"
      :order-id="orderId"
      :show-toolbar="true"
    />
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import ExpressWaybill from './ExpressWaybill.vue'

const visible = ref(false)
const orderId = ref('')
const waybillRef = ref(null)

const open = (id) => {
  orderId.value = id
  visible.value = true
}

const onClosed = () => {
  orderId.value = ''
}

defineExpose({ open })
</script>

<style>
.express-dialog .el-dialog__body {
  padding-top: 8px;
  max-height: 80vh;
  overflow-y: auto;
}
</style>
