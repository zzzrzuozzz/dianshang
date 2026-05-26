import { createRouter, createWebHistory } from 'vue-router'

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
      ],
    },
  ],
})

export default router
