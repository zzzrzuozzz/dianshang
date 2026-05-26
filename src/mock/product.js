/** 商品分类树 Mock 数据 */
export const categoryTreeData = [
  {
    id: 1,
    label: '日用百货',
    children: [
      { id: 11, label: '卫生纸' },
      { id: 12, label: '垃圾袋' },
    ],
  },
  {
    id: 2,
    label: '母婴宠物',
    children: [
      { id: 21, label: '牛奶' },
      { id: 22, label: '火腿肠' },
      { id: 23, label: '儿童车' },
    ],
  },
  {
    id: 3,
    label: '大牌',
    children: [
      { id: 31, label: 'T恤' },
      { id: 32, label: '女装' },
    ],
  },
]

/** 商品分类下拉选项 */
export const categoryOptions = [
  { label: '日用百货', value: 'daily' },
  { label: '母婴宠物', value: 'baby' },
  { label: '大牌', value: 'brand' },
]

const thumb =
  'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

/** 商品列表 Mock 数据 */
export const mockProductList = [
  {
    id: '025342',
    title: '2024新款夏季纯棉T恤 男女同款宽松短袖',
    subtitle: '舒适透气 多色可选',
    thumb,
    originalPrice: 56,
    discountPrice: 36,
    status: 'on',
    auditStatus: 'passed',
    remark: '',
    sku: 'SKU-001',
    sort: 1,
    stock: 1200,
    monthSales: 352,
    totalSales: 2555,
    supplier: '自营',
  },
  {
    id: '025343',
    title: '进口全脂纯牛奶 1L*12盒整箱装',
    subtitle: '新西兰进口 高钙营养',
    thumb,
    originalPrice: 89,
    discountPrice: 69,
    status: 'off',
    auditStatus: 'pending',
    remark: '',
    sku: 'SKU-002',
    sort: 2,
    stock: 800,
    monthSales: 128,
    totalSales: 980,
    supplier: '第三方',
  },
  {
    id: '025344',
    title: '加厚垃圾袋 家用厨房一次性',
    subtitle: '50只装 不易破',
    thumb,
    originalPrice: 19.9,
    discountPrice: 12.9,
    status: 'on',
    auditStatus: 'rejected',
    remark: '商品信息不全',
    sku: 'SKU-003',
    sort: 3,
    stock: 5000,
    monthSales: 890,
    totalSales: 12000,
    supplier: '自营',
  },
]

/** 审核状态映射 */
export const auditStatusMap = {
  pending: { label: '待审核', type: 'warning' },
  passed: { label: '已通过', type: 'success' },
  rejected: { label: '未通过', type: 'danger' },
}

/** 列表页状态 Tabs */
export const listStatusTabs = [
  { key: 'all', label: '全部', count: 1800 },
  { key: 'on', label: '已上架', count: 1500 },
  { key: 'off', label: '已下架', count: 100 },
  { key: 'pending', label: '待审核', count: 10 },
  { key: 'rejected', label: '未通过', count: 10 },
]

/** 审核页状态 Tabs */
export const auditStatusTabs = [
  { key: 'all', label: '全部', count: 200 },
  { key: 'audited', label: '已审核', count: 120 },
  { key: 'pending', label: '待审核', count: 50 },
  { key: 'rejected', label: '未通过', count: 30 },
]
