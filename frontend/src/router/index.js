/**
 * Vue Router 配置文件
 * 用于定义应用的前端路由
 */

import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/Index.vue'
import ClientLayout from '@/layout/ClientLayout.vue'
import { useUserStore } from '@/store/user'
import { useClientUserStore } from '@/store/clientUser'

// 定义路由表
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Index.vue'),
    meta: { title: '管理端登录', hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/admin/home/Index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'employee',
        name: 'Employee',
        component: () => import('@/views/employee/Index.vue'),
        meta: { title: '员工管理' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/category/Index.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'dish',
        name: 'Dish',
        component: () => import('@/views/dish/Index.vue'),
        meta: { title: '菜品管理' }
      },
      {
        path: 'setmeal',
        name: 'Setmeal',
        component: () => import('@/views/setmeal/Index.vue'),
        meta: { title: '套餐管理' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/order/Index.vue'),
        meta: { title: '订单管理' }
      }
    ]
  },
  // C端路由
  {
    path: '/client/login',
    name: 'ClientLogin',
    component: () => import('@/views/client/login/Index.vue'),
    meta: { title: '用户登录', hidden: true }
  },
  {
    path: '/client',
    component: ClientLayout,
    redirect: '/client/home',
    children: [
      {
        path: 'home',
        name: 'ClientHome',
        component: () => import('@/views/client/home/Index.vue'),
        meta: { title: '欢迎光临' }
      },
      {
        path: 'menu',
        name: 'ClientMenu',
        component: () => import('@/views/client/menu/Index.vue'),
        meta: { title: '点餐' }
      },
      {
        path: 'order/submit',
        name: 'ClientOrderSubmit',
        component: () => import('@/views/client/order/Submit.vue'),
        meta: { title: '确认订单' }
      },
      {
        path: 'order/history',
        name: 'ClientOrderHistory',
        component: () => import('@/views/client/order/History.vue'),
        meta: { title: '历史订单' }
      },
      {
        path: 'address',
        name: 'ClientAddress',
        component: () => import('@/views/client/address/Index.vue'),
        meta: { title: '收货地址' }
      }
    ]
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 白名单路由
const whiteList = ['/login', '/client/login', '/client/home', '/client/menu', '/client/order/submit']

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - MealOps`
  }

  // 判断是否为C端路由
  const isClient = to.path.startsWith('/client')

  if (isClient) {
    const clientUserStore = useClientUserStore()
    const hasClientToken = clientUserStore.token

    if (hasClientToken) {
      if (to.path === '/client/login') {
        next({ path: '/client/home' })
      } else {
        next()
      }
    } else {
      if (whiteList.includes(to.path)) {
        next()
      } else {
        next(`/client/login?redirect=${to.path}`)
      }
    }
  } else {
    // 获取B端用户信息 store
    const userStore = useUserStore()
    const hasToken = userStore.token

    if (hasToken) {
      if (to.path === '/login') {
        // 如果已登录，访问登录页重定向到首页
        next({ path: '/' })
      } else {
        next()
      }
    } else {
      // 没有 token
      if (whiteList.includes(to.path)) {
        // 在白名单中的路由直接放行
        next()
      } else {
        // 否则全部重定向到登录页
        next(`/login?redirect=${to.path}`)
      }
    }
  }
})

export default router
