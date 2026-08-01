<template>
  <AuthCardShell
    description="登录后即可创建应用、持续对话优化并管理自己的作品。"
    title="一句话生成所想"
  >
    <a-form :model="formState" autocomplete="off" layout="vertical" @finish="doSubmit">
      <a-form-item
        label="账号"
        name="userAccount"
        :rules="[{ required: true, message: '请输入账号' }]"
      >
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>

      <a-form-item
        label="密码"
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不能小于 8 位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>

      <div class="auth-card__tips">
        没有账号？
        <RouterLink to="/user/register">立即注册</RouterLink>
      </div>

      <a-form-item>
        <a-button block html-type="submit" type="primary">登录</a-button>
      </a-form-item>
    </a-form>
  </AuthCardShell>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { message } from 'ant-design-vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import AuthCardShell from '@/components/auth/AuthCardShell.vue'
import { userLogin } from '@/api/userController'
import { useLoginUserStore } from '@/stores/loginUser'

const loginUserStore = useLoginUserStore()
const router = useRouter()
const route = useRoute()

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const doSubmit = async (values: API.UserLoginRequest) => {
  const res = await userLogin(values)
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    const redirect = route.query.redirect as string | undefined
    await router.replace(redirect || '/')
    return
  }

  message.error(`登录失败，${res.data.message}`)
}
</script>
