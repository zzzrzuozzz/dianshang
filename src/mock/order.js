const thumb =
  'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

/** 订单列表状态 Tabs */
export const orderStatusTabs = [
  { key: 'all', label: '全部', count: 265 },
  { key: 'pending_payment', label: '待付款', count: 225 },
  { key: 'pending_ship', label: '待发货', count: 225 },
  { key: 'shipped', label: '已发货', count: 10 },
  { key: 'after_sale', label: '售后订单', count: 20 },
  { key: 'completed', label: '已完成', count: 225 },
  { key: 'closed', label: '已关闭', count: 225 },
  { key: 'refunded', label: '已退款', count: 225 },
]

/** 售后列表状态 Tabs */
export const afterSaleStatusTabs = [
  { key: 'platform_pending', label: '待平台处理', count: 225 },
  { key: 'user_pending', label: '待用户处理', count: 225 },
  { key: 'platform_confirm', label: '待平台确认收货', count: 225 },
  { key: 'completed', label: '已完成', count: 20 },
  { key: 'rejected', label: '已拒绝', count: 225 },
  { key: 'closed', label: '已关闭', count: 225 },
  { key: 'all', label: '全部', count: 265 },
]

export const orderStatusLabel = {
  pending_payment: '待付款',
  paid: '已付款',
  pending_ship: '待发货',
  shipped: '已发货',
  after_sale: '售后订单',
  completed: '已完成',
  closed: '已关闭',
  refunded: '已退款',
}

export const shipStatusLabel = {
  none: '-',
  not_shipped: '未发货',
  in_transit: '运输中',
  signed: '已签收',
}

export const afterSaleTypeLabel = {
  refund_only: '仅退款',
  return_refund: '退货退款',
  exchange: '换货',
}

/** 订单列表 Mock */
export const mockOrderList = [
  {
    id: 'BZ6542',
    productName: '100角磨机配件手动自锁压板神箭手',
    thumb,
    spec: '莫兰迪双色 (蓝色)',
    quantity: 1,
    actualAmount: 56,
    discount: -36,
    freight: 8,
    freightFree: false,
    orderStatus: 'pending_ship',
    shipStatus: 'not_shipped',
    logistics: '',
    payTime: '2024-8-6 17:08:56',
    autoConfirmTime: '2024-8-16 17:08:56',
    receiverName: '李大海',
    receiverPhone: '13088880000',
    receiverAddress: '广东省深圳市龙华区民治街道民乐新村',
    supplier: '自营',
    supplierPhone: '400-888-8888',
  },
  {
    id: 'BZ6543',
    productName: '2024新款夏季纯棉T恤 男女同款',
    thumb,
    spec: '白色 XL',
    quantity: 2,
    actualAmount: 72,
    discount: -20,
    freight: 0,
    freightFree: true,
    orderStatus: 'shipped',
    shipStatus: 'in_transit',
    logistics: '申通快递 (st26546464)',
    payTime: '2024-8-5 14:20:30',
    autoConfirmTime: '2024-8-15 14:20:30',
    receiverName: '王小明',
    receiverPhone: '13800138000',
    receiverAddress: '北京市朝阳区望京街道阜通东大街',
    supplier: '第三方',
    supplierPhone: '010-12345678',
  },
  {
    id: 'BZ6544',
    productName: '进口全脂纯牛奶 1L*12盒整箱装',
    thumb,
    spec: '整箱装',
    quantity: 1,
    actualAmount: 69,
    discount: -20,
    freight: 0,
    freightFree: true,
    orderStatus: 'pending_payment',
    shipStatus: 'none',
    logistics: '',
    payTime: '',
    autoConfirmTime: '',
    receiverName: '张三',
    receiverPhone: '13900139000',
    receiverAddress: '上海市浦东新区陆家嘴环路1000号',
    supplier: '自营',
    supplierPhone: '400-888-8888',
  },
]

/** 售后列表 Mock */
export const mockAfterSaleList = [
  {
    id: 'AS6542',
    orderId: 'BZ6542',
    productName: '100角磨机配件手动自锁压板神箭手',
    thumb,
    orderStatus: 'paid',
    shipStatus: 'not_shipped',
    afterSaleStatus: 'platform_pending',
    afterSaleType: 'refund_only',
    refundAmount: 8.0,
    applyTime: '2024-8-3 15:40',
    processTime: '2024-8-3 15:40',
  },
  {
    id: 'AS6543',
    orderId: 'BZ6543',
    productName: '2024新款夏季纯棉T恤 男女同款',
    thumb,
    orderStatus: 'shipped',
    shipStatus: 'in_transit',
    afterSaleStatus: 'user_pending',
    afterSaleType: 'return_refund',
    refundAmount: 72.0,
    applyTime: '2024-8-4 10:20',
    processTime: '',
  },
  {
    id: 'AS6544',
    orderId: 'BZ6545',
    productName: '加厚垃圾袋 家用厨房一次性',
    thumb,
    orderStatus: 'completed',
    shipStatus: 'signed',
    afterSaleStatus: 'completed',
    afterSaleType: 'exchange',
    refundAmount: 0.0,
    applyTime: '2024-8-1 09:00',
    processTime: '2024-8-2 16:30',
  },
]

/** 订单设置 Mock */
export const mockReturnReasons = [
  { id: 'BZ6542', reason: '商品信息拍错 (规格/尺码/颜色等)', addTime: '2024-8-6 17:08:56', visible: true, sort: 10 },
  { id: 'BZ6543', reason: '七天无理由', addTime: '2024-8-5 14:20:30', visible: true, sort: 9 },
  { id: 'BZ6544', reason: '质量问题', addTime: '2024-8-4 10:00:00', visible: false, sort: 8 },
  { id: 'BZ6545', reason: '不喜欢/不想要', addTime: '2024-8-3 09:30:00', visible: true, sort: 7 },
]

/** 根据 orderStatus 生成详情 Mock */
export function getMockOrderDetail(orderId, status = 'pending_payment') {
  const base = {
    id: orderId || 'BZ6542',
    orderStatus: status,
    shipStatus: 'not_shipped',
    shipStatusText: '未发货',
    afterSalesStatus: 'none',
    afterSalesStatusText: '未售后',
    deliverySerial: '未发货',
    userAccount: '18056565555 李大海',
    payMethod: '未支付',
    payMethodDetail: '',
    orderSource: 'APP',
    orderType: '普通订单',
    deliveryMethod: '快递',
    logisticsNo: '未发货',
    autoConfirmDays: '确定收货后15天',
    receiverName: '李天霸',
    receiverPhone: '13088880000',
    receiverAddress: '广东省深圳市龙华区民治街道民乐新村100号',
    userRemark: '快速发货，急用',
    platformRemark: '',
    invoiceType: '电子发票',
    invoiceStatus: '未开票',
    invoiceAttr: '个人',
    invoiceTitle: '李天霸',
    invoiceTaxNo: '',
    invoiceContent: '服装',
    invoiceEmail: 'user@example.com',
    products: [
      {
        thumb,
        name: '100角磨机配件手动自锁压板神箭手',
        spec: '莫兰迪双色 (蓝色)',
        quantity: 1,
        sku: 'SKU-001',
        unitPrice: 56,
        payable: 56,
        discount: -36,
        freight: 16,
        subtotal: 228,
        actualPaid: 0,
      },
    ],
    payment: {
      productTotal: 200.0,
      freight: 16.0,
      goldCoin: 0,
      promotion: 0,
      discount: 0,
      coupon: -36.0,
      payableSubtotal: 228.0,
      actualPaid: 0,
    },
    steps: [
      { title: '提交订单', time: '2024-8-2 16:29', status: 'finish' },
      { title: '付款', time: '', status: 'wait', desc: '未支付' },
      { title: '平台发货', time: '', status: 'wait', desc: '未发货' },
      { title: '用户收货', time: '', status: 'wait', desc: '未收货' },
      { title: '完成评价', time: '', status: 'wait', desc: '未评价' },
    ],
    afterSale: null,
    actions: ['contact', 'export', 'close'],
  }

  if (status === 'pending_ship' || status === 'paid') {
    return {
      ...base,
      orderStatus: 'pending_ship',
      orderStatusText: '待发货',
      payMethod: '支付宝',
      payMethodDetail: '2088123456789012',
      logisticsNo: '未发货',
      products: base.products.map((p) => ({ ...p, actualPaid: 228 })),
      payment: { ...base.payment, actualPaid: 228.0 },
      steps: [
        { title: '提交订单', time: '2024-8-2 16:29', status: 'finish' },
        { title: '付款', time: '2024-8-2 16:29', status: 'finish' },
        { title: '平台发货', time: '', status: 'wait', desc: '未发货' },
        { title: '用户收货', time: '', status: 'wait', desc: '未收货' },
        { title: '完成评价', time: '', status: 'wait', desc: '未评价' },
      ],
      actions: ['contact', 'download', 'printShip', 'printExpress', 'export'],
    }
  }

  if (status === 'shipped') {
    return {
      ...base,
      orderStatus: 'shipped',
      orderStatusText: '已发货',
      shipStatus: 'in_transit',
      shipStatusText: '派送中(申通快递 ST56576465)',
      deliverySerial: 'FH20240802001',
      payMethod: '支付宝',
      payMethodDetail: '2088123456789012',
      logisticsNo: 'ST56576465',
      products: base.products.map((p) => ({ ...p, actualPaid: 228 })),
      payment: { ...base.payment, actualPaid: 228.0 },
      steps: [
        { title: '提交订单', time: '2024-8-2 16:29', status: 'finish' },
        { title: '已付款', time: '2024-8-2 16:29', status: 'finish' },
        { title: '平台发货', time: '2024-8-2 16:29', status: 'finish' },
        { title: '用户收货', time: '', status: 'wait', desc: '未收货' },
        { title: '完成评价', time: '', status: 'wait', desc: '未评价' },
      ],
      actions: ['contact', 'download', 'printShip', 'printExpress', 'export', 'reship'],
    }
  }

  if (status === 'after_sale' || status === 'refunded' || status === 'closed') {
    return {
      ...base,
      orderStatus: 'closed',
      orderStatusText: '已关闭',
      shipStatus: 'signed',
      shipStatusText: '已签收(申通快递 ST56576465)',
      afterSalesStatus: 'refunded',
      afterSalesStatusText: '已退款',
      deliverySerial: 'FH20240802001',
      payMethod: '支付宝',
      payMethodDetail: '2088123456789012',
      logisticsNo: 'ST56576465',
      invoiceStatus: '已作废',
      products: base.products.map((p) => ({ ...p, actualPaid: 228 })),
      payment: { ...base.payment, actualPaid: 228.0 },
      steps: [
        { title: '提交订单', time: '2024-8-2 16:29', status: 'finish' },
        { title: '已付款', time: '2024-8-2 16:29', status: 'finish' },
        { title: '平台发货', time: '2024-8-2 16:29', status: 'finish' },
        { title: '已签收', time: '2024-8-2 16:29', status: 'finish' },
        { title: '完成评价', time: '', status: 'wait', desc: '未评价' },
      ],
      afterSale: {
        type: 'return_refund',
        typeText: '退货退款',
        reason: '商品信息拍错',
        description: '坏了',
        evidence: thumb,
        refundAmount: 64,
        returnLogistics: '申通快递 ST56576465',
        applyTime: '2024-8-3 15:40',
        agreeTime: '2024-8-4 10:00',
        refundTime: '2024-8-5 14:30',
        returnAddress: '广东省深圳市南山区科技园退货中心A栋',
      },
      actions: ['contact', 'download', 'printShip', 'printExpress', 'export'],
    }
  }

  return {
    ...base,
    orderStatusText: '待付款',
  }
}

/** 根据 orderId 推断演示状态 */
export function resolveDemoStatus(orderId) {
  if (orderId === 'BZ6543') return 'shipped'
  if (orderId === 'BZ6544') return 'after_sale'
  return 'pending_payment'
}
