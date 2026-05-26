<template>
  <div class="express-waybill-root">
    <div v-if="showToolbar" class="waybill-toolbar no-print">
      <el-button type="primary" :icon="Printer" :loading="loading" @click="handlePrint">
        确认打印面单
      </el-button>
    </div>

    <div ref="printAreaRef" class="waybill-print-area">
      <div v-loading="loading" class="waybill-card">
        <div class="waybill-top">
          <div class="brand-block">
            <span class="brand-logo">{{ brandShort }}</span>
            <span class="brand-name">{{ data.expressCompany || '快递' }}</span>
          </div>
          <div class="barcode-block">
            <div class="barcode-lines" :title="data.expressNumber" />
            <div class="barcode-text">{{ data.expressNumber || '—' }}</div>
          </div>
        </div>

        <div class="section receiver">
          <div class="section-tag">收</div>
          <div class="section-body">
            <div class="line-main">
              <span class="name">{{ data.receiverName }}</span>
              <span class="phone">{{ data.receiverPhone }}</span>
            </div>
            <div class="line-addr">{{ data.receiverAddress }}</div>
          </div>
        </div>

        <div class="section sender">
          <div class="section-tag muted">寄</div>
          <div class="section-body small">
            <div class="line-main">
              <span class="name">{{ data.senderName }}</span>
              <span class="phone">{{ data.senderPhone }}</span>
            </div>
            <div class="line-addr">{{ data.senderAddress }}</div>
          </div>
        </div>

        <div class="waybill-divider" />

        <div class="goods-block">
          <div class="goods-title">内件品名</div>
          <ul class="sku-list">
            <li v-for="(sku, idx) in data.skuList" :key="idx">
              {{ sku.name }}
              <span v-if="sku.spec">（{{ sku.spec }}）</span>
              × {{ sku.quantity }}
            </li>
            <li v-if="!data.skuList?.length">—</li>
          </ul>
        </div>

        <div class="waybill-footer">
          <div class="order-no">订单编号：{{ data.orderId }}</div>
          <div v-if="data.templateSpec" class="template-spec">{{ data.templateSpec }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Printer } from '@element-plus/icons-vue'
import { fetchExpressPreview } from '@/api/express'

const props = defineProps({
  orderId: {
    type: String,
    default: '',
  },
  /** 是否显示顶部打印按钮（弹窗内由外层控制时可关闭） */
  showToolbar: {
    type: Boolean,
    default: true,
  },
})

const loading = ref(false)
const printAreaRef = ref(null)

const data = reactive({
  orderId: '',
  expressCompany: '',
  expressNumber: '',
  templateName: '',
  templateSpec: '',
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  senderName: '',
  senderPhone: '',
  senderAddress: '',
  skuList: [],
})

const brandShort = computed(() => {
  const name = data.expressCompany || ''
  return name.length >= 2 ? name.slice(0, 2) : '快递'
})

const applyPreview = (dto) => {
  Object.assign(data, {
    orderId: dto.orderId || '',
    expressCompany: dto.expressCompany || '',
    expressNumber: dto.expressNumber || '',
    templateName: dto.templateName || '',
    templateSpec: dto.templateSpec || '',
    receiverName: dto.receiverName || '',
    receiverPhone: dto.receiverPhone || '',
    receiverAddress: dto.receiverAddress || '',
    senderName: dto.senderName || '',
    senderPhone: dto.senderPhone || '',
    senderAddress: dto.senderAddress || '',
    skuList: dto.skuList || [],
  })
}

const loadExpressData = async () => {
  if (!props.orderId) return
  loading.value = true
  try {
    const dto = await fetchExpressPreview(props.orderId)
    applyPreview(dto)
  } catch {
    ElMessage.error('加载快递单数据失败')
  } finally {
    loading.value = false
  }
}

const handlePrint = () => {
  if (!data.orderId) {
    ElMessage.warning('暂无面单数据')
    return
  }
  const area = printAreaRef.value
  if (!area) return

  const iframe = document.createElement('iframe')
  iframe.style.cssText = 'position:fixed;right:0;bottom:0;width:0;height:0;border:0'
  document.body.appendChild(iframe)

  const doc = iframe.contentWindow.document
  doc.open()
  doc.write(`
    <!DOCTYPE html>
    <html><head>
      <meta charset="utf-8" />
      <title>快递面单 ${data.orderId}</title>
      <style>${collectPrintStyles()}</style>
    </head><body>${area.innerHTML}</body></html>
  `)
  doc.close()

  iframe.contentWindow.focus()
  iframe.contentWindow.print()
  setTimeout(() => document.body.removeChild(iframe), 1000)
}

function collectPrintStyles() {
  return `
    @page { size: 100mm 180mm; margin: 0; }
    body { margin: 0; padding: 8mm; font-family: "Microsoft YaHei", sans-serif; }
    .waybill-card { width: 84mm; min-height: 164mm; border: 2px solid #111; padding: 4mm; box-sizing: border-box; }
    .waybill-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 4mm; }
    .brand-logo { display: inline-block; background: #e74c3c; color: #fff; font-weight: 700; padding: 2px 6px; margin-right: 4px; }
    .brand-name { font-size: 14px; font-weight: 700; }
    .barcode-block { text-align: right; }
    .barcode-lines { width: 36mm; height: 12mm; background: repeating-linear-gradient(90deg,#000 0 2px,#fff 2px 4px); margin-left: auto; }
    .barcode-text { font-size: 10px; letter-spacing: 1px; margin-top: 2px; }
    .section { display: flex; gap: 3mm; margin-bottom: 3mm; }
    .section-tag { width: 7mm; height: 7mm; line-height: 7mm; text-align: center; background: #111; color: #fff; font-size: 11px; font-weight: 700; flex-shrink: 0; }
    .section-tag.muted { background: #666; }
    .line-main { font-size: 13px; font-weight: 700; }
    .receiver .line-main { font-size: 15px; }
    .line-addr { font-size: 11px; margin-top: 2px; line-height: 1.4; }
    .sender .section-body { font-size: 10px; }
    .waybill-divider { border-top: 1px dashed #999; margin: 3mm 0; }
    .goods-title { font-size: 10px; color: #666; margin-bottom: 2mm; }
    .sku-list { margin: 0; padding-left: 4mm; font-size: 11px; }
    .waybill-footer { margin-top: 4mm; font-size: 10px; }
    .template-spec { color: #888; margin-top: 2px; }
  `
}

watch(
  () => props.orderId,
  (id) => {
    if (id) loadExpressData()
  },
  { immediate: true },
)

defineExpose({ loadExpressData, handlePrint })
</script>

<style scoped>
.express-waybill-root {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.waybill-toolbar {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}

.waybill-print-area {
  display: flex;
  justify-content: center;
}

/* 约 100mm × 180mm 电子面单比例（屏幕预览缩放） */
.waybill-card {
  width: 380px;
  min-height: 684px;
  border: 2px solid #1a1a1a;
  border-radius: 4px;
  padding: 16px;
  box-sizing: border-box;
  background: #fff;
  color: #1a1a1a;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.waybill-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 6px;
}

.brand-logo {
  display: inline-block;
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  color: #fff;
  font-weight: 700;
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 2px;
}

.brand-name {
  font-size: 16px;
  font-weight: 700;
}

.barcode-block {
  text-align: right;
}

.barcode-lines {
  width: 140px;
  height: 48px;
  margin-left: auto;
  background: repeating-linear-gradient(
    90deg,
    #111 0,
    #111 2px,
    #fff 2px,
    #fff 4px
  );
  border: 1px solid #ddd;
}

.barcode-text {
  font-size: 12px;
  letter-spacing: 2px;
  margin-top: 4px;
  font-family: 'Courier New', monospace;
}

.section {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.section-tag {
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  background: #1a1a1a;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
  border-radius: 2px;
}

.section-tag.muted {
  background: #666;
}

.receiver .line-main {
  font-size: 17px;
  font-weight: 700;
}

.line-main .phone {
  margin-left: 12px;
  font-weight: 500;
}

.line-addr {
  font-size: 13px;
  margin-top: 6px;
  line-height: 1.5;
  color: #333;
}

.sender .section-body {
  font-size: 12px;
}

.sender .line-main {
  font-size: 13px;
}

.waybill-divider {
  border-top: 1px dashed #bbb;
  margin: 14px 0;
}

.goods-title {
  font-size: 12px;
  color: #888;
  margin-bottom: 8px;
}

.sku-list {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  line-height: 1.6;
}

.waybill-footer {
  margin-top: 20px;
  font-size: 12px;
  color: #555;
}

.template-spec {
  margin-top: 4px;
  color: #999;
}

@media print {
  .no-print {
    display: none !important;
  }

  @page {
    size: 100mm 180mm;
    margin: 0;
  }

  .waybill-card {
    width: 100mm;
    min-height: 180mm;
    box-shadow: none;
    border-radius: 0;
  }
}
</style>
