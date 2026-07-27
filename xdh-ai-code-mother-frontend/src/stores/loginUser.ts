import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUser } from '@/api/userController.ts'

// 将用户信息作为全局状态来管理
export const useLoginUserStore = defineStore('loginUser', () => {
  // 定义登入用户信息常量
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登入',
  })

  // 异步获取用户登入信息
  async function fetchLoginUser() {
    const res = await getLoginUser()
    if (res.data.code === 0 && res.data.data != null) {
      loginUser.value = res.data.data
    }
  }

  // 更新登入用户信息
  function setLoginUser(user: any) {
    loginUser.value = user
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
