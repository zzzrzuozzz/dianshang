const thumb = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const cover = 'https://picsum.photos/200/200?random=topic'

export const mockTopicTypes = [
  { id: 1, code: 'BZ6542', name: '服装专题', icon: cover, topicCount: 100, visible: true, sort: 10, intro: '服装类专题' },
  { id: 2, code: 'BZ6543', name: '家居服饰', icon: cover, topicCount: 80, visible: true, sort: 9, intro: '家居类' },
  { id: 3, code: 'BZ6544', name: '餐饮厨房', icon: cover, topicCount: 45, visible: false, sort: 8, intro: '厨房类' },
]

export const mockTopics = [
  {
    id: 'BZ6542',
    typeId: 2,
    typeName: '家居服饰',
    title: '春季家电家具疯狂秒杀',
    publishTime: '2024-8-8 16:14',
    productCount: 10,
    clickCount: 1000,
    collectCount: 1000,
    commentCount: 1000,
    sort: 2,
    status: 1,
    statusText: '已上线',
  },
  {
    id: 'BZ6543',
    typeId: 3,
    typeName: '餐饮厨房',
    title: '夏日清凉厨房好物',
    publishTime: '2024-8-10 10:00',
    productCount: 5,
    clickCount: 500,
    collectCount: 200,
    commentCount: 80,
    sort: 1,
    status: 0,
    statusText: '已下架',
  },
]

export const mockTopicDetail = {
  id: 'BZ6542',
  title: '春季家电家具疯狂秒杀春季家电家具疯狂秒杀',
  intro: '春季大促，家电家具限时秒杀，品质好物低至五折。',
  content:
    '春季家电家具疯狂秒杀活动火热进行中，精选品牌家电、舒适家居好物，限时特惠不容错过。活动期间下单即享满减优惠，更有惊喜赠品等你来拿。',
  images: [cover, cover, cover],
  collectCount: 10,
  readCount: 1,
  shareCount: 100,
  products: [
    { id: '025342', name: '春季家电家具疯狂秒杀...', price: 36.55, thumb },
    { id: '025343', name: '春季家电家具疯狂秒杀...', price: 36.55, thumb },
    { id: '025344', name: '春季家电家具疯狂秒杀...', price: 36.55, thumb },
  ],
}

export const mockTopicComments = [
  {
    id: 'BZ6542',
    content: '挺好的，不错',
    replyContent: '感谢支持，我们会继续加油的',
    pics: [thumb, thumb, thumb],
    status: 1,
    statusText: '已加精',
  },
  {
    id: 'BZ6543',
    content: '垃圾',
    replyContent: '',
    pics: [],
    status: 2,
    statusText: '已隐藏',
  },
  {
    id: 'BZ6544',
    content: '期待更多活动',
    replyContent: '',
    pics: [thumb],
    status: 0,
    statusText: '待审核',
  },
]

export const mockHelpTypes = [
  { id: 1, code: 'BZ6542', name: '新手教程', icon: cover, articleCount: 100, visible: true, sort: 10 },
  { id: 2, code: 'BZ6543', name: '常见问题', icon: cover, articleCount: 80, visible: true, sort: 9 },
  { id: 3, code: 'BZ6544', name: '售后政策', icon: cover, articleCount: 30, visible: false, sort: 8 },
]

export const mockHelpArticles = [
  {
    id: 'BZ6542',
    title: '常见问题',
    publishTime: '2024-8-8 16:14',
    categoryName: '常见问题',
    typeId: 2,
    clickCount: 1000,
    sort: 2,
    status: 1,
    statusText: '已上线',
  },
  {
    id: 'BZ6543',
    title: '如何注册会员',
    publishTime: '2024-8-9 11:00',
    categoryName: '新手帮助',
    typeId: 1,
    clickCount: 800,
    sort: 1,
    status: 0,
    statusText: '已下架',
  },
]

export const pickProducts = [
  { id: '025342', name: '2024新款夏季纯棉T恤 男女同款', price: 36.55, stock: 1200, thumb },
  { id: '025343', name: '进口全脂纯牛奶 1L*12盒', price: 69, stock: 800, thumb },
  { id: '025344', name: '加厚垃圾袋 家用厨房', price: 12.9, stock: 5000, thumb },
  { id: '025345', name: '春季家电家具疯狂秒杀', price: 36.55, stock: 200, thumb },
]

export function createTopicForm() {
  return {
    typeId: '',
    title: '',
    intro: '',
    content: '',
    coverImage: '',
    specifyProducts: true,
    productIds: [],
    memberLevels: ['all'],
    tags: { newUser: ['all'], firstBuy: ['all'], repurchase: ['all'], active: ['all'] },
    estimatedUsers: 6000,
    sort: 0,
    status: 1,
  }
}

export function createTypeForm() {
  return { name: '', sort: 0, visible: true, icon: '', intro: '' }
}

export function createHelpForm() {
  return { typeId: '', title: '', intro: '', content: '', coverImages: [], online: true }
}
