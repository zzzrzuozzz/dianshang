export const orderStatusLabel: Record<string, string> = {
  pending_payment: '待付款',
  paid: '已付款',
  pending_ship: '待发货',
  shipped: '已发货',
  after_sale: '售后订单',
  completed: '已完成',
  closed: '已关闭',
  refunded: '已退款',
}

export const shipStatusLabel: Record<string, string> = {
  none: '-',
  not_shipped: '未发货',
  in_transit: '运输中',
  signed: '已签收',
}

export const afterSaleTypeLabel: Record<string, string> = {
  refund_only: '仅退款',
  return_refund: '退货退款',
  exchange: '换货',
}

export const afterSaleStatusLabel: Record<string, string> = {
  platform_pending: '待平台处理',
  user_pending: '待用户处理',
  platform_confirm: '待平台确认收货',
  completed: '已完成',
  rejected: '已拒绝',
  closed: '已关闭',
}
