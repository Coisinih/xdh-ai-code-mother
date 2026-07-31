import { message } from 'ant-design-vue'

import router from '@/router'
import { useLoginUserStore } from '@/stores/loginUser'

let firstFetchLoginUser = true

router.beforeEach(async (to, _from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser

  if (firstFetchLoginUser) {
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser
    firstFetchLoginUser = false
  }

  if (to.fullPath.startsWith('/admin') && loginUser?.userRole !== 'admin') {
    message.error('没有权限访问该页面')
    next(`/user/login?redirect=${encodeURIComponent(to.fullPath)}`)
    return
  }

  next()
})
