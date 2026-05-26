const avatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

export const memberLevelOptions = [
  { label: '普通会员', value: 'normal' },
  { label: '黄金会员', value: 'gold' },
  { label: '铂金会员', value: 'platinum' },
  { label: '钻石会员', value: 'diamond' },
]

export const cityOptions = [
  {
    value: 'guangdong',
    label: '广东省',
    children: [
      { value: 'shenzhen', label: '深圳市' },
      { value: 'guangzhou', label: '广州市' },
    ],
  },
  {
    value: 'beijing',
    label: '北京市',
    children: [{ value: 'chaoyang', label: '朝阳区' }],
  },
]

export const mockUserList = [
  {
    id: 'BZ6542',
    nickname: '李天霸',
    account: '13088888888',
    level: '普通会员',
    levelKey: 'normal',
    consumeAmount: 1000.0,
    orderCount: 10,
    points: 200,
    growth: 200,
    status: 'normal',
    statusText: '正常',
    remark: '-',
    registerTime: '2024-8-15 14:15:13',
    avatar,
  },
  {
    id: 'BZ6543',
    nickname: '王小明',
    account: '13800138000',
    level: '黄金会员',
    levelKey: 'gold',
    consumeAmount: 5600.0,
    orderCount: 45,
    points: 1200,
    growth: 800,
    status: 'normal',
    statusText: '正常',
    remark: '-',
    registerTime: '2024-7-20 09:30:00',
    avatar,
  },
  {
    id: 'BZ6544',
    nickname: '违规用户',
    account: '13900139000',
    level: '普通会员',
    levelKey: 'normal',
    consumeAmount: 200.0,
    orderCount: 2,
    points: 50,
    growth: 30,
    status: 'login_banned',
    statusText: '禁止登录',
    remark: '违规发帖',
    registerTime: '2024-6-10 16:20:00',
    avatar,
  },
]

export const mockTagList = [
  {
    id: 'BZ6542',
    name: '优质用户',
    memberCount: 1000,
    condition: '累计成功交易: 10笔  累计购买金额: ¥2000.00',
  },
  {
    id: 'BZ6543',
    name: '新用户',
    memberCount: 500,
    condition: '累计成功交易: 0笔  累计购买金额: ¥0.00',
  },
  {
    id: 'BZ6544',
    name: '复购用户',
    memberCount: 320,
    condition: '累计成功交易: 5笔  累计购买金额: ¥1000.00',
  },
]

export const mockLevelList = [
  {
    id: 'BZ6542',
    name: '普通会员',
    isDefault: true,
    growthPoint: 1,
    freeShipping: '40元包邮/每月2次',
    reviewReward: '+5成长值/条',
    sort: 1,
  },
  {
    id: 'BZ6543',
    name: '黄金会员',
    isDefault: false,
    growthPoint: 500,
    freeShipping: '30元包邮/每月3次',
    reviewReward: '+3成长值/条',
    sort: 2,
  },
  {
    id: 'BZ6544',
    name: '铂金会员',
    isDefault: false,
    growthPoint: 1000,
    freeShipping: '20元包邮/每月4次',
    reviewReward: '+2成长值/条',
    sort: 3,
  },
  {
    id: 'BZ6545',
    name: '钻石会员',
    isDefault: false,
    growthPoint: 2000,
    freeShipping: '15元包邮/每月5次',
    reviewReward: '+1成长值/条',
    sort: 4,
  },
]

export function getMockUserDetail(userId) {
  const user = mockUserList.find((u) => u.id === userId) || mockUserList[0]
  return {
    ...user,
    userId: user.id,
    phoneMasked: '130****6666',
    ip: '114.55.25.01',
    source: 'APP',
    tags: '服装、百货',
    birthday: '2002-6-22',
    city: '深圳',
    gender: '女',
    couponCount: 10,
    reviewCount: 20,
    returnCount: 5,
    loginCount: 10,
    favoriteProducts: 5,
    favoriteTopics: 10,
    orderFriends: 100,
    lotteryCount: 10,
    addresses: [
      {
        name: '李天霸',
        phone: '13088880000',
        region: '广东省深圳市龙华区',
        detail: '民治街道民乐新村100号',
      },
      {
        name: '李天霸',
        phone: '13088880001',
        region: '广东省深圳市南山区',
        detail: '科技园南区A栋',
      },
    ],
    orders: [
      {
        id: '69265555522',
        time: '2024-8-6 17:08:56',
        amount: 56.0,
        payMethod: '支付宝',
        source: 'APP',
        status: '待发货',
      },
      {
        id: '69265555523',
        time: '2024-8-1 10:20:00',
        amount: 128.0,
        payMethod: '微信',
        source: '小程序',
        status: '已完成',
      },
    ],
    permissions: ['normal'],
  }
}
