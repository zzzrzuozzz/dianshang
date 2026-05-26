<template>
  <div v-loading="loading" class="order-detail">
    <!-- 顶部操作栏 -->
    <el-card shadow="never" class="panel-card action-card">
      <div class="action-bar">
        <span class="action-label">操作</span>
        <div class="action-btns">
          <el-button v-if="hasAction('contact')" type="success" size="small">联系用户</el-button>
          <el-button v-if="hasAction('download')" type="primary" size="small">下载配货单</el-button>
          <el-button v-if="hasAction('printShip')" type="primary" size="small">打印发货单</el-button>
          <el-button v-if="hasAction('printExpress')" type="primary" size="small" @click="openExpressPrint">
            打印快递单
          </el-button>
          <el-button v-if="hasAction('export')" type="primary" size="small">导出订单</el-button>
          <el-button
            v-if="hasAction('reship')"
            type="primary"
            size="small"
            :loading="reissueLoading"
            @click="handleReissue"
          >
            重新发货
          </el-button>
          <el-button v-if="hasAction('close')" type="danger" size="small">关闭订单</el-button>
        </div>
      </div>
    </el-card>

    <!-- 步骤条 -->
    <el-card shadow="never" class="panel-card">
      <el-steps :active="activeStep" align-center finish-status="success">
        <el-step
          v-for="(step, idx) in orderInfo.steps"
          :key="idx"
          :title="step.title"
          :description="stepDesc(step)"
          :status="stepStatus(step)"
        />
      </el-steps>
    </el-card>

    <!-- 基本信息 -->
    <el-card shadow="never" class="panel-card section-card">
      <template #header>
        <div class="section-header">
          <span class="section-title">基本信息</span>
          <div>
            <el-button
              v-if="hasAction('reship')"
              type="primary"
              size="small"
              :loading="reissueLoading"
              @click="handleReissue"
            >
              重新发货
            </el-button>
            <el-button type="primary" size="small">物流信息</el-button>
          </div>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="订单状态">
          <span :class="statusClass(orderInfo.orderStatus)">{{ orderInfo.orderStatusText }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="收货状态">{{ orderInfo.shipStatusText }}</el-descriptions-item>
        <el-descriptions-item label="售后状态">
          <span :class="orderInfo.afterSalesStatus !== 'none' ? 'text-danger' : ''">
            {{ orderInfo.afterSalesStatusText }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ orderInfo.id }}</el-descriptions-item>
        <el-descriptions-item label="发货单流水号">{{ orderInfo.deliverySerial }}</el-descriptions-item>
        <el-descriptions-item label="用户账号">{{ orderInfo.userAccount }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">
          {{ orderInfo.payMethod }}
          <span v-if="orderInfo.payMethodDetail" class="sub-text">{{ orderInfo.payMethodDetail }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="订单来源">{{ orderInfo.orderSource }}</el-descriptions-item>
        <el-descriptions-item label="订单类型">{{ orderInfo.orderType }}</el-descriptions-item>
        <el-descriptions-item label="配送方式">{{ orderInfo.deliveryMethod }}</el-descriptions-item>
        <el-descriptions-item label="物流单号">{{ orderInfo.logisticsNo }}</el-descriptions-item>
        <el-descriptions-item label="自动确认收货时间">{{ orderInfo.autoConfirmDays }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 售后信息（仅售后状态展示） -->
    <el-card v-if="orderInfo.afterSale" shadow="never" class="panel-card section-card">
      <template #header>
        <div class="section-header">
          <span class="section-title">售后信息</span>
          <el-button type="primary" size="small">处理售后</el-button>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="售后类型">
          <span class="text-danger">{{ orderInfo.afterSale.typeText }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="售后原因">{{ orderInfo.afterSale.reason }}</el-descriptions-item>
        <el-descriptions-item label="退款说明">{{ orderInfo.afterSale.description }}</el-descriptions-item>
        <el-descriptions-item label="凭证">
          <el-image
            v-if="orderInfo.afterSale.evidence"
            :src="orderInfo.afterSale.evidence"
            fit="cover"
            class="evidence-img"
          />
        </el-descriptions-item>
        <el-descriptions-item label="退款金额">{{ orderInfo.afterSale.refundAmount }}元</el-descriptions-item>
        <el-descriptions-item label="退回物流">{{ orderInfo.afterSale.returnLogistics }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ orderInfo.afterSale.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="同意时间">{{ orderInfo.afterSale.agreeTime }}</el-descriptions-item>
        <el-descriptions-item label="退款时间">{{ orderInfo.afterSale.refundTime }}</el-descriptions-item>
        <el-descriptions-item label="退货地址" :span="2">
          {{ orderInfo.afterSale.returnAddress }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 商品信息 -->
    <el-card shadow="never" class="panel-card section-card">
      <template #header>
        <div class="section-header">
          <span class="section-title">商品信息</span>
          <el-button type="primary" size="small">修改商品信息</el-button>
        </div>
      </template>
      <el-table :data="orderInfo.products" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="商品图片" width="80" align="center">
          <template #default="{ row }">
            <el-image :src="row.thumb" fit="cover" class="product-thumb" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="160" />
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column prop="quantity" label="数量" width="70" align="center" />
        <el-table-column prop="sku" label="货号" width="100" align="center" />
        <el-table-column prop="unitPrice" label="单价" width="80" align="center">
          <template #default="{ row }">{{ row.unitPrice }}元</template>
        </el-table-column>
        <el-table-column label="订单应付金额" width="110" align="center">
          <template #default="{ row }">
            <div class="cell-stack">
              <span>{{ row.payable }}元</span>
              <span class="sub-text">优惠: {{ row.discount }}元</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="freight" label="运费" width="70" align="center">
          <template #default="{ row }">{{ row.freight }}元</template>
        </el-table-column>
        <el-table-column prop="subtotal" label="应付小计" width="90" align="center">
          <template #default="{ row }">{{ row.subtotal }}元</template>
        </el-table-column>
        <el-table-column label="实付金额" width="90" align="center">
          <template #default="{ row }">
            {{ row.actualPaid ? `${row.actualPaid}元` : '-' }}
          </template>
        </el-table-column>
      </el-table>
      <p class="product-summary">
        应付小计: {{ orderInfo.payment.payableSubtotal }}元，商品:
        {{ orderInfo.payment.productTotal }}元，运费: {{ orderInfo.payment.freight }}元 |
        实付合计:
        <span class="text-danger">
          {{ orderInfo.payment.actualPaid ? `${orderInfo.payment.actualPaid}元` : '-' }}
        </span>
      </p>
    </el-card>

    <!-- 收货人信息 -->
    <el-card shadow="never" class="panel-card section-card">
      <template #header>
        <div class="section-header">
          <span class="section-title">收货人信息</span>
          <el-button type="primary" size="small">修改收货信息</el-button>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="收货人">{{ orderInfo.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ orderInfo.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址">{{ orderInfo.receiverAddress }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 订单备注 -->
    <el-card shadow="never" class="panel-card section-card">
      <template #header>
        <div class="section-header">
          <span class="section-title">订单备注</span>
          <el-button type="primary" size="small">添加备注</el-button>
        </div>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户备注">{{ orderInfo.userRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="平台备注">{{ orderInfo.platformRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 发票信息 -->
    <el-card shadow="never" class="panel-card section-card">
      <template #header>
        <div class="section-header">
          <span class="section-title">发票信息</span>
          <div>
            <el-button type="primary" size="small">修改发票信息</el-button>
            <el-button type="primary" size="small">修改发票</el-button>
          </div>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="发票类型">{{ orderInfo.invoiceType }}</el-descriptions-item>
        <el-descriptions-item label="发票状态">
          <span :class="orderInfo.invoiceStatus === '已作废' ? 'text-danger' : ''">
            {{ orderInfo.invoiceStatus }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="发票属性">{{ orderInfo.invoiceAttr }}</el-descriptions-item>
        <el-descriptions-item label="发票抬头">{{ orderInfo.invoiceTitle }}</el-descriptions-item>
        <el-descriptions-item label="纳税人识别号">{{ orderInfo.invoiceTaxNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票内容">{{ orderInfo.invoiceContent }}</el-descriptions-item>
        <el-descriptions-item label="收票人邮箱">{{ orderInfo.invoiceEmail }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 付款明细 -->
    <el-card shadow="never" class="panel-card section-card">
      <template #header>
        <span class="section-title">付款明细</span>
      </template>
      <el-table :data="[orderInfo.payment]" border>
        <el-table-column label="商品合计" align="center">
          <template #default="{ row }">{{ row.productTotal.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="运费" align="center">
          <template #default="{ row }">{{ row.freight.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="金币抵扣" align="center">
          <template #default="{ row }">{{ row.goldCoin.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="优惠活动" align="center">
          <template #default="{ row }">{{ row.promotion.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="折扣金额" align="center">
          <template #default="{ row }">{{ row.discount.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="优惠券" align="center">
          <template #default="{ row }">{{ row.coupon.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="应付小计" align="center">
          <template #default="{ row }">{{ row.payableSubtotal.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="实付金额" align="center">
          <template #default="{ row }">
            <span class="text-danger">{{ row.actualPaid ? row.actualPaid.toFixed(2) : '-' }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <ExpressWaybillDialog ref="expressDialogRef" />
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchOrderDetail, reissueOrder } from '@/api/order'
import ExpressWaybillDialog from '@/components/express/ExpressWaybillDialog.vue'

const route = useRoute()
const loading = ref(false)
const reissueLoading = ref(false)
const expressDialogRef = ref(null)

const openExpressPrint = () => {
  if (orderInfo.id) {
    expressDialogRef.value?.open(orderInfo.id)
  }
}

const handleReissue = () => {
  if (!orderInfo.id) return
  ElMessageBox.confirm(
    `确定为订单 ${orderInfo.id} 补发商品吗？将扣减库存并生成「补发」流水，可继续打印新快递单。`,
    '补发确认',
    { type: 'warning' },
  )
    .then(async () => {
      reissueLoading.value = true
      try {
        await reissueOrder(orderInfo.id)
        ElMessage.success('补发成功，已扣减库存')
        await getOrderDetail(orderInfo.id)
        openExpressPrint()
      } finally {
        reissueLoading.value = false
      }
    })
    .catch(() => {})
}

const orderInfo = reactive({
  id: '',
  orderStatus: '',
  orderStatusText: '',
  shipStatus: '',
  shipStatusText: '',
  afterSalesStatus: 'none',
  afterSalesStatusText: '未售后',
  deliverySerial: '',
  userAccount: '',
  payMethod: '',
  payMethodDetail: '',
  orderSource: '',
  orderType: '',
  deliveryMethod: '',
  logisticsNo: '',
  autoConfirmDays: '',
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  userRemark: '',
  platformRemark: '',
  invoiceType: '',
  invoiceStatus: '',
  invoiceAttr: '',
  invoiceTitle: '',
  invoiceTaxNo: '',
  invoiceContent: '',
  invoiceEmail: '',
  products: [],
  payment: {
    productTotal: 0,
    freight: 0,
    goldCoin: 0,
    promotion: 0,
    discount: 0,
    coupon: 0,
    payableSubtotal: 0,
    actualPaid: 0,
  },
  steps: [],
  afterSale: null,
  actions: [],
})

const activeStep = computed(() => {
  const finished = orderInfo.steps.filter((s) => s.status === 'finish').length
  return Math.max(0, finished - 1)
})

const hasAction = (key) => orderInfo.actions.includes(key)

const stepStatus = (step) => {
  if (step.status === 'finish') return 'finish'
  if (step.status === 'wait') return 'wait'
  return 'process'
}

const stepDesc = (step) => {
  if (step.time) return step.time
  return step.desc || ''
}

const statusClass = (status) => {
  if (['pending_payment'].includes(status)) return 'text-primary'
  if (['closed', 'refunded', 'after_sale'].includes(status)) return 'text-danger'
  if (['shipped'].includes(status)) return 'text-primary'
  return ''
}

/**
 * 获取订单详情
 * 此处后续对接 Spring Boot 后端 /api/order/detail/{id} 接口
 */
const getOrderDetail = async (orderId) => {
  loading.value = true
  try {
    const data = await fetchOrderDetail(orderId)
    Object.assign(orderInfo, {
      ...data,
      products: data.products || [],
      steps: data.steps || [],
      actions: data.actions || [],
      payment: data.payment || orderInfo.payment,
    })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const orderId = route.params.orderId
  getOrderDetail(orderId)
})
</script>

<style scoped>
.order-detail {
  min-height: calc(100vh - 120px);
}

.panel-card {
  margin-bottom: 12px;
  border-radius: 8px;
  border: none;
}

.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.action-label {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.action-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.section-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 14px;
  margin-right: 8px;
  background: #409eff;
  border-radius: 2px;
  vertical-align: middle;
}

.product-thumb {
  width: 48px;
  height: 48px;
  border-radius: 4px;
}

.evidence-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
}

.cell-stack {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
}

.sub-text {
  font-size: 12px;
  color: #909399;
}

.product-summary {
  margin: 12px 0 0;
  text-align: right;
  font-size: 13px;
  color: #606266;
}

.text-primary {
  color: #409eff;
  font-weight: 500;
}

.text-danger {
  color: #f56c6c;
  font-weight: 500;
}
</style>
