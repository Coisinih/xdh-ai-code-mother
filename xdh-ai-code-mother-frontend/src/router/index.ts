import { createRouter, createWebHistory } from 'vue-router'

const scrollPositions = new Map<string, number>()

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    const saved = scrollPositions.get(to.fullPath)
    if (saved !== undefined) {
      return { top: saved }
    }
    return { top: 0 }
  },
  routes: [
    {
      path: '/',
      name: '首页',
      component: () => import('@/pages/HomePage.vue'),
    },
    {
      path: '/app/chat/:id',
      name: '应用对话页',
      component: () => import('@/pages/app/AppChatPage.vue'),
      meta: {
        layout: 'immersive',
      },
    },
    {
      path: '/app/edit/:id',
      name: '应用信息编辑页',
      component: () => import('@/pages/app/AppEditPage.vue'),
    },
    {
      path: '/admin/appManage',
      name: '应用管理',
      component: () => import('@/pages/admin/AppManage.vue'),
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: () => import('@/pages/admin/UserManage.vue'),
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: () => import('@/pages/user/UserLogin.vue'),
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: () => import('@/pages/user/UserRegister.vue'),
    },
  ],
})

router.beforeEach((_to, from) => {
  scrollPositions.set(from.fullPath, window.scrollY || document.documentElement.scrollTop)
})

export default router
