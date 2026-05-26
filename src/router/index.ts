import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/redirect/:path(.*)',
      name: 'Redirect',
      component: () => import('@/views/Redirect.vue'),
    },
    {
      path: '/',
      component: () => import('@/layouts/AdminLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '首页' },
        },
        {
          path: 'profile/index',
          name: 'Profile',
          component: () => import('@/views/profile/index.vue'),
          meta: { title: '个人中心', subTitle: '账号设置' },
        },
        {
          path: 'product/list',
          name: 'ProductList',
          component: () => import('@/views/product/index.vue'),
          meta: { title: '商品管理', subTitle: '商品列表' },
        },
        {
          path: 'product/add',
          name: 'ProductAdd',
          component: () => import('@/views/product/add.vue'),
          meta: { title: '商品管理', subTitle: '添加商品' },
        },
        {
          path: 'product/audit',
          name: 'ProductAudit',
          component: () => import('@/views/product/audit.vue'),
          meta: { title: '商品管理', subTitle: '商品审核' },
        },
        {
          path: 'product/recycle',
          name: 'ProductRecycle',
          component: () => import('@/views/product/recycle.vue'),
          meta: { title: '商品管理', subTitle: '回收站' },
        },
        {
          path: 'product/comment',
          name: 'ProductComment',
          component: () => import('@/views/product/comment.vue'),
          meta: { title: '商品管理', subTitle: '商品评价' },
        },
        {
          path: 'product/category',
          name: 'ProductCategory',
          component: () => import('@/views/product/category.vue'),
          meta: { title: '商品管理', subTitle: '商品分类' },
        },
        {
          path: 'product/brand',
          name: 'ProductBrand',
          component: () => import('@/views/product/brand.vue'),
          meta: { title: '商品管理', subTitle: '品牌管理' },
        },
        {
          path: 'order/list',
          name: 'OrderList',
          component: () => import('@/views/order/list.vue'),
          meta: { title: '订单管理', subTitle: '订单列表' },
        },
        {
          path: 'order/confirm',
          name: 'OrderConfirm',
          component: () => import('@/views/order/confirm.vue'),
          meta: { title: '订单管理', subTitle: '确认收货' },
        },
        {
          path: 'order/after-sale',
          name: 'OrderAfterSale',
          component: () => import('@/views/order/afterSale.vue'),
          meta: { title: '订单管理', subTitle: '售后列表' },
        },
        {
          path: 'order/setting',
          name: 'OrderSetting',
          component: () => import('@/views/order/orderSetting.vue'),
          meta: { title: '订单管理', subTitle: '订单设置' },
        },
        {
          path: 'order/detail/:orderId',
          name: 'OrderDetail',
          component: () => import('@/views/order/orderDetail.vue'),
          meta: { title: '订单管理', subTitle: '订单详情' },
        },
        {
          path: 'inventory/list',
          name: 'InventoryList',
          component: () => import('@/views/inventory/index.vue'),
          meta: { title: '库存管理', subTitle: '库存看板' },
        },
        {
          path: 'inventory/flow',
          name: 'InventoryFlow',
          component: () => import('@/views/inventory/flow.vue'),
          meta: { title: '库存管理', subTitle: '出入库流水' },
        },
        {
          path: 'user/list',
          name: 'UserList',
          component: () => import('@/views/user/list.vue'),
          meta: { title: '用户管理', subTitle: '用户列表' },
        },
        {
          path: 'user/detail/:userId',
          name: 'UserDetail',
          component: () => import('@/views/user/listDetail.vue'),
          meta: { title: '用户管理', subTitle: '用户详情' },
        },
        {
          path: 'user/edit/:userId',
          name: 'UserEdit',
          component: () => import('@/views/user/listEdit.vue'),
          meta: { title: '用户管理', subTitle: '编辑资料' },
        },
        {
          path: 'user/tag',
          name: 'UserTag',
          component: () => import('@/views/user/tag.vue'),
          meta: { title: '用户管理', subTitle: '标签管理' },
        },
        {
          path: 'user/tag/edit/new',
          name: 'UserTagEditNew',
          component: () => import('@/views/user/tagEdit.vue'),
          meta: { title: '用户管理', subTitle: '新增标签' },
        },
        {
          path: 'user/tag/edit/:tagId',
          name: 'UserTagEdit',
          component: () => import('@/views/user/tagEdit.vue'),
          meta: { title: '用户管理', subTitle: '编辑标签' },
        },
        {
          path: 'user/tag/users',
          name: 'UserTagUsers',
          component: () => import('@/views/user/tagUser.vue'),
          meta: { title: '用户管理', subTitle: '标签人数' },
        },
        {
          path: 'user/level',
          name: 'UserLevel',
          component: () => import('@/views/user/level.vue'),
          meta: { title: '用户管理', subTitle: '会员等级' },
        },
        {
          path: 'user/level/edit/new',
          name: 'UserLevelEditNew',
          component: () => import('@/views/user/levelEdit.vue'),
          meta: { title: '用户管理', subTitle: '新增等级' },
        },
        {
          path: 'user/level/edit/:levelId',
          name: 'UserLevelEdit',
          component: () => import('@/views/user/levelEdit.vue'),
          meta: { title: '用户管理', subTitle: '编辑等级' },
        },
        {
          path: 'user/growth',
          name: 'UserGrowth',
          component: () => import('@/views/user/growth.vue'),
          meta: { title: '用户管理', subTitle: '成长值积分' },
        },
        {
          path: 'promotion/seckill',
          name: 'Seckill',
          component: () => import('@/views/promotion/seckill.vue'),
          meta: { title: '营销管理', subTitle: '秒杀活动' },
        },
        {
          path: 'promotion/seckill/time',
          name: 'SeckillTime',
          component: () => import('@/views/promotion/seckillTime.vue'),
          meta: { title: '营销管理', subTitle: '时间段设置' },
        },
        {
          path: 'promotion/seckill/:activityId/products',
          name: 'SeckillProduct',
          component: () => import('@/views/promotion/seckillProduct.vue'),
          meta: { title: '营销管理', subTitle: '设置商品' },
        },
        {
          path: 'promotion/seckill/:activityId/sku/:timeId',
          name: 'SeckillSku',
          component: () => import('@/views/promotion/seckillSku.vue'),
          meta: { title: '营销管理', subTitle: '商品列表' },
        },
        {
          path: 'promotion/group-buy',
          name: 'GroupBuy',
          component: () => import('@/views/promotion/groupBuy.vue'),
          meta: { title: '营销管理', subTitle: '团购活动' },
        },
        {
          path: 'promotion/group-buy/:activityId/time',
          name: 'GroupBuyTime',
          component: () => import('@/views/promotion/groupBuyTime.vue'),
          meta: { title: '营销管理', subTitle: '时间段设置' },
        },
        {
          path: 'promotion/group-buy/:activityId/products',
          name: 'GroupBuyProduct',
          component: () => import('@/views/promotion/groupBuyProduct.vue'),
          meta: { title: '营销管理', subTitle: '设置商品' },
        },
        {
          path: 'promotion/group-buy/:activityId/sku/:timeId',
          name: 'GroupBuySku',
          component: () => import('@/views/promotion/groupBuySku.vue'),
          meta: { title: '营销管理', subTitle: '商品列表' },
        },
        {
          path: 'promotion/coupon',
          name: 'Coupon',
          component: () => import('@/views/promotion/coupon.vue'),
          meta: { title: '营销管理', subTitle: '优惠券' },
        },
        {
          path: 'promotion/coupon/:id/detail',
          name: 'CouponDetail',
          component: () => import('@/views/promotion/couponDetail.vue'),
          meta: { title: '营销管理', subTitle: '优惠券明细' },
        },
        {
          path: 'ops/system-message',
          name: 'OpsSystemMessage',
          component: () => import('@/views/ops/systemMessage.vue'),
          meta: { title: '运营管理', subTitle: '系统消息' },
        },
        {
          path: 'ops/system-message/add',
          name: 'OpsSystemMessageAdd',
          component: () => import('@/views/ops/messagePushEdit.vue'),
          meta: { title: '运营管理', subTitle: '新增推送', mode: 'system' },
        },
        {
          path: 'ops/system-message/edit/:id',
          name: 'OpsSystemMessageEdit',
          component: () => import('@/views/ops/messagePushEdit.vue'),
          meta: { title: '运营管理', subTitle: '编辑推送', mode: 'system' },
        },
        {
          path: 'ops/sms',
          name: 'OpsSms',
          component: () => import('@/views/ops/sms.vue'),
          meta: { title: '运营管理', subTitle: '短信推送' },
        },
        {
          path: 'ops/sms/add',
          name: 'OpsSmsAdd',
          component: () => import('@/views/ops/smsPushEdit.vue'),
          meta: { title: '运营管理', subTitle: '新增推送' },
        },
        {
          path: 'ops/sms/edit/:id',
          name: 'OpsSmsEdit',
          component: () => import('@/views/ops/smsPushEdit.vue'),
          meta: { title: '运营管理', subTitle: '编辑推送' },
        },
        {
          path: 'ops/station-message',
          name: 'OpsStationMessage',
          component: () => import('@/views/ops/stationMessage.vue'),
          meta: { title: '运营管理', subTitle: '站内信' },
        },
        {
          path: 'ops/station-message/add',
          name: 'OpsStationMessageAdd',
          component: () => import('@/views/ops/messagePushEdit.vue'),
          meta: { title: '运营管理', subTitle: '新增推送', mode: 'station' },
        },
        {
          path: 'ops/station-message/edit/:id',
          name: 'OpsStationMessageEdit',
          component: () => import('@/views/ops/messagePushEdit.vue'),
          meta: { title: '运营管理', subTitle: '编辑推送', mode: 'station' },
        },
        {
          path: 'ops/advertisement',
          name: 'OpsAdvertisement',
          component: () => import('@/views/ops/advertisement.vue'),
          meta: { title: '运营管理', subTitle: '广告位' },
        },
        {
          path: 'ops/advertisement/add',
          name: 'OpsAdvertisementAdd',
          component: () => import('@/views/ops/advertisementEdit.vue'),
          meta: { title: '运营管理', subTitle: '新增广告' },
        },
        {
          path: 'ops/advertisement/edit/:id',
          name: 'OpsAdvertisementEdit',
          component: () => import('@/views/ops/advertisementEdit.vue'),
          meta: { title: '运营管理', subTitle: '编辑广告' },
        },
        {
          path: 'content/topic',
          name: 'ContentTopic',
          component: () => import('@/views/content/topic.vue'),
          meta: { title: '内容管理', subTitle: '专题管理' },
        },
        {
          path: 'content/topic/type',
          name: 'ContentTopicType',
          component: () => import('@/views/content/topicType.vue'),
          meta: { title: '内容管理', subTitle: '专题类型' },
        },
        {
          path: 'content/topic/type/add',
          name: 'ContentTopicTypeAdd',
          component: () => import('@/views/content/topicTypeEdit.vue'),
          meta: { title: '内容管理', subTitle: '添加类型' },
        },
        {
          path: 'content/topic/type/edit/:id',
          name: 'ContentTopicTypeEdit',
          component: () => import('@/views/content/topicTypeEdit.vue'),
          meta: { title: '内容管理', subTitle: '编辑类型' },
        },
        {
          path: 'content/topic/add',
          name: 'ContentTopicAdd',
          component: () => import('@/views/content/topicEdit.vue'),
          meta: { title: '内容管理', subTitle: '新增专题' },
        },
        {
          path: 'content/topic/edit/:id',
          name: 'ContentTopicEdit',
          component: () => import('@/views/content/topicEdit.vue'),
          meta: { title: '内容管理', subTitle: '编辑专题' },
        },
        {
          path: 'content/topic/detail/:id',
          name: 'ContentTopicDetail',
          component: () => import('@/views/content/topicDetail.vue'),
          meta: { title: '内容管理', subTitle: '专题详情' },
        },
        {
          path: 'content/help',
          name: 'ContentHelp',
          component: () => import('@/views/content/help.vue'),
          meta: { title: '内容管理', subTitle: '帮助管理' },
        },
        {
          path: 'content/help/type',
          name: 'ContentHelpType',
          component: () => import('@/views/content/helpType.vue'),
          meta: { title: '内容管理', subTitle: '帮助分类' },
        },
        {
          path: 'content/help/type/add',
          name: 'ContentHelpTypeAdd',
          component: () => import('@/views/content/helpTypeEdit.vue'),
          meta: { title: '内容管理', subTitle: '添加分类' },
        },
        {
          path: 'content/help/type/edit/:id',
          name: 'ContentHelpTypeEdit',
          component: () => import('@/views/content/helpTypeEdit.vue'),
          meta: { title: '内容管理', subTitle: '编辑分类' },
        },
        {
          path: 'content/help/add',
          name: 'ContentHelpAdd',
          component: () => import('@/views/content/helpEdit.vue'),
          meta: { title: '内容管理', subTitle: '新增帮助' },
        },
        {
          path: 'content/help/edit/:id',
          name: 'ContentHelpEdit',
          component: () => import('@/views/content/helpEdit.vue'),
          meta: { title: '内容管理', subTitle: '编辑帮助' },
        },
        {
          path: 'stats/transaction',
          name: 'StatsTransaction',
          component: () => import('@/views/stats/transaction.vue'),
          meta: { title: '统计', subTitle: '交易统计' },
        },
        {
          path: 'stats/flow',
          name: 'StatsFlow',
          component: () => import('@/views/stats/flow.vue'),
          meta: { title: '统计', subTitle: '流量统计' },
        },
        {
          path: 'stats/product',
          name: 'StatsProduct',
          component: () => import('@/views/stats/product.vue'),
          meta: { title: '统计', subTitle: '商品统计' },
        },
      ],
    },
  ],
})

const publicPaths = ['/login']

router.beforeEach((to, _from, next) => {
  const token = getToken()
  if (publicPaths.includes(to.path)) {
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
    return
  }
  if (!token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  next()
})

export default router
