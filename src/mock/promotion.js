export const seckillStatusMap = {
  pending: { label: '待开始', type: 'primary' },
  active: { label: '进行中', type: '' },
  offline: { label: '已下架', type: 'danger' },
  ended: { label: '已结束', type: 'danger' },
}

export const mockSeckillList = [
  {
    id: 'BZ6542',
    title: '春季家电家具疯狂秒杀',
    status: 'pending',
    startTime: '2024-8-8 16:14',
    endTime: '2024-8-18 16:14',
    warning: '有1款商品库存低于预警值',
    online: true,
  },
  {
    id: 'BZ6543',
    title: '夏日清凉专场秒杀',
    status: 'active',
    startTime: '2024-8-1 10:00',
    endTime: '2024-8-31 22:00',
    warning: '',
    online: true,
  },
  {
    id: 'BZ6544',
    title: '双十一预热秒杀',
    status: 'offline',
    startTime: '2024-7-1 00:00',
    endTime: '2024-7-31 23:59',
    warning: '',
    online: false,
  },
  {
    id: 'BZ6545',
    title: '国庆特惠秒杀',
    status: 'ended',
    startTime: '2024-6-1 08:00',
    endTime: '2024-6-7 23:59',
    warning: '',
    online: false,
  },
]

export const mockTimeSlots = [
  { id: 'BZ6542', name: '秒杀8点', start: '08:00:00', end: '10:00:00', enabled: true },
  { id: 'BZ6543', name: '秒杀10点', start: '10:00:01', end: '12:00:00', enabled: true },
  { id: 'BZ6544', name: '秒杀12点', start: '12:00:01', end: '14:00:00', enabled: true },
  { id: 'BZ6545', name: '秒杀14点', start: '14:00:01', end: '16:00:00', enabled: true },
  { id: 'BZ6546', name: '秒杀16点', start: '16:00:01', end: '18:00:00', enabled: false },
  { id: 'BZ6547', name: '秒杀18点', start: '18:00:01', end: '20:00:00', enabled: false },
  { id: 'BZ6548', name: '秒杀20点', start: '20:00:01', end: '22:00:00', enabled: true },
  { id: 'BZ6549', name: '秒杀22点', start: '22:00:01', end: '00:00:00', enabled: true },
]

export const mockStoreProducts = [
  { id: 'NS656555', name: '100角磨机配件手动自锁压板神箭手', price: 12, stock: 1500 },
  { id: 'NS656556', name: '2024新款夏季纯棉T恤 男女同款', price: 56, stock: 3200 },
  { id: 'NS656557', name: '加厚垃圾袋 家用厨房一次性', price: 19.9, stock: 5000 },
]

export const mockSeckillSkus = [
  {
    id: 'BZ6542',
    name: '100角磨机配件手动自锁压板神箭手',
    productCode: 'NS656555',
    price: 12,
    seckillPrice: 10,
    seckillQty: 500,
    remainStock: 480,
    totalStock: 1500,
    warningStock: 500,
    limitQty: 2,
    sort: 1,
  },
  {
    id: 'BZ6543',
    name: '2024新款夏季纯棉T恤 男女同款',
    productCode: 'NS656556',
    price: 56,
    seckillPrice: 36,
    seckillQty: 200,
    remainStock: 50,
    totalStock: 3200,
    warningStock: 100,
    limitQty: 1,
    sort: 2,
  },
]

export const mockGroupBuyList = [
  {
    id: 'GB6542',
    title: '夏季家电家具疯狂团购',
    status: 'pending',
    startTime: '2024-8-8 16:14',
    endTime: '2024-8-18 16:14',
    warning: '有1款商品库存低于预警值',
    online: true,
  },
  {
    id: 'GB6543',
    title: '母婴用品拼团专场',
    status: 'active',
    startTime: '2024-8-1 10:00',
    endTime: '2024-8-31 22:00',
    warning: '',
    online: true,
  },
]

export const couponTypeOptions = [
  { label: '新人券', value: 'newcomer' },
  { label: '购物赠券', value: 'shopping' },
  { label: '会员赠券', value: 'member' },
  { label: '全场赠券', value: 'sitewide' },
]

export const mockCouponList = [
  {
    id: 'BZ6542',
    name: '全品类通用券',
    type: 'newcomer',
    typeLabel: '新人券',
    products: '全部商品',
    threshold: '满20减10元',
    faceValue: 10,
    issueQty: '不限量',
    claimed: 1000,
    used: 500,
    platform: 'APP',
    validity: '15天',
    timeRange: '2024-8-10至2024-10-1',
    status: 'active',
    statusLabel: '进行中',
    online: true,
  },
  {
    id: 'BZ6543',
    name: '满100减30券',
    type: 'shopping',
    typeLabel: '购物赠券',
    products: '300款',
    threshold: '满100减30元',
    faceValue: 30,
    issueQty: '1000',
    claimed: 800,
    used: 400,
    platform: '小程序',
    validity: '30天',
    timeRange: '2024-8-1至2024-9-30',
    status: 'pending',
    statusLabel: '待开始',
    online: true,
  },
]

export const mockCouponDetail = {
  id: 'BZ6542',
  name: '全品类通用券',
  type: '新人券',
  products: '全部商品',
  threshold: '满20减10元',
  faceValue: 10,
  status: '进行中',
  timeRange: '2024-8-10 至 2024-10-1',
  validity: '15天',
  totalIssue: 10000,
  remain: 5000,
  claimed: 2000,
  used: 1000,
  pending: 500,
  expired: 500,
}

export const mockCouponHistory = [
  {
    couponId: 'BZ6542',
    member: '13088888888 (565655)',
    method: '主动领取',
    claimTime: '2024-8-10 10:00',
    status: 'pending',
    useTime: '-',
    orderId: '',
  },
  {
    couponId: 'BZ6542',
    member: '13800138000 (565656)',
    method: '系统赠送',
    claimTime: '2024-8-11 14:20',
    status: 'used',
    useTime: '2024-8-12 16:30',
    orderId: '69265555522',
  },
  {
    couponId: 'BZ6542',
    member: '13900139000 (565657)',
    method: '主动领取',
    claimTime: '2024-8-5 09:00',
    status: 'expired',
    useTime: '2024-8-20 23:59',
    orderId: '',
  },
]
