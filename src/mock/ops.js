export const messageCategories = [
  { label: '秒杀活动', value: 'seckill' },
  { label: '客服消息', value: 'service' },
  { label: '运营消息', value: 'ops' },
  { label: '系统通知', value: 'system' },
]

export const memberLevelOptions = [
  { label: '全部会员', value: 'all' },
  { label: '普通会员', value: 'normal' },
  { label: '黄金会员', value: 'gold' },
  { label: '铂金会员', value: 'platinum' },
  { label: '钻石会员', value: 'diamond' },
]

export const tagGroups = [
  { key: 'newUser', label: '新老用户标签', options: [
    { label: '全部标签', value: 'all' },
    { label: '7天内注册新用户', value: '7d' },
    { label: '15天内注册新用户', value: '15d' },
    { label: '不限', value: 'none' },
  ]},
  { key: 'firstBuy', label: '首购标签', options: [
    { label: '全部标签', value: 'all' },
    { label: '7天内首购', value: '7d' },
    { label: '15天内首购', value: '15d' },
    { label: '不限', value: 'none' },
  ]},
  { key: 'repurchase', label: '复购标签', options: [
    { label: '全部标签', value: 'all' },
    { label: '7天内购买', value: '7d' },
    { label: '15天内购买', value: '15d' },
    { label: '不限', value: 'none' },
  ]},
  { key: 'active', label: '活跃标签', options: [
    { label: '全部标签', value: 'all' },
    { label: '7天内登录', value: '7d' },
    { label: '15天内登录', value: '15d' },
    { label: '不限', value: 'none' },
  ]},
]

export const innerLinkOptions = [
  { label: '自定义', value: 'custom' },
  { label: '活动', value: 'activity' },
  { label: '专题', value: 'topic' },
  { label: '商品', value: 'product' },
  { label: '优惠券', value: 'coupon' },
]

export const advTypeOptions = [
  { label: 'APP首页轮播', value: 'carousel' },
  { label: 'APP首页活动', value: 'activity' },
  { label: '猜你喜欢', value: 'guess' },
]

const baseMessages = [
  {
    id: 'BZ6542',
    title: '春季家电家具疯狂秒杀',
    publishStatus: 1,
    publishStatusText: '已发布',
    appPush: '已推送',
    publishTime: '2024-8-8 16:14',
    msgCategory: 'ops',
    msgCategoryText: '运营消息',
    pushCount: 1,
    pushVolume: 1000,
    clickCount: 500,
    receiveVolume: 1000,
    pushUser: '200位',
  },
  {
    id: 'BZ6543',
    title: '新用户专享优惠券到账提醒',
    publishStatus: 0,
    publishStatusText: '未发布',
    appPush: '不推送',
    publishTime: '2024-8-10 09:00',
    msgCategory: 'system',
    msgCategoryText: '系统消息',
    pushCount: 0,
    pushVolume: 0,
    clickCount: 0,
    receiveVolume: 0,
    pushUser: '-',
  },
  {
    id: 'BZ6544',
    title: '您的订单已发货，请注意查收',
    publishStatus: 1,
    publishStatusText: '已发布',
    appPush: '已推送',
    publishTime: '2024-8-6 14:30',
    msgCategory: 'service',
    msgCategoryText: '客服消息',
    pushCount: 2,
    pushVolume: 800,
    clickCount: 320,
    receiveVolume: 780,
    pushUser: '13066660001',
  },
]

export const mockSystemMessages = baseMessages
export const mockSmsMessages = baseMessages.map((m) => ({ ...m, title: m.title, content: m.title }))
export const mockStationMessages = baseMessages.map((m) => ({ ...m, title: m.title }))

export const mockAdvertisements = [
  {
    id: 'BZ6542',
    name: '春季家电家具疯狂秒杀',
    publishTime: '2024-8-8 16:14',
    endTime: '2024-8-18 16:14',
    advType: 'APP首页轮播',
    exposureCount: 1000,
    clickCount: 1000,
    sort: 2,
    status: 1,
    statusText: '已上线',
  },
  {
    id: 'BZ6543',
    name: '夏日清凉专场',
    publishTime: '2024-8-1 10:00',
    endTime: '2024-8-31 22:00',
    advType: '猜你喜欢',
    exposureCount: 5000,
    clickCount: 800,
    sort: 1,
    status: 0,
    statusText: '已下架',
  },
]

export function createEmptyPushForm() {
  return {
    category: '',
    title: '',
    intro: '',
    jumpType: 'TEXT',
    jumpUrl: '',
    innerLinkType: 'custom',
    detail: '',
    coverImages: [],
    memberLevels: ['all'],
    regions: [],
    tags: { newUser: ['all'], firstBuy: ['all'], repurchase: ['all'], active: ['all'] },
    sendType: 1,
    publishTime: '',
    generateTypes: [],
    appPush: false,
    estimatedUsers: 6000,
    pushMethod: ['tag'],
    stationContent: '',
    smsContent: '',
  }
}
