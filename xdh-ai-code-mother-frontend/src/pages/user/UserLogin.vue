<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="auth-card__title">一句话生所想</h2>
      <p class="auth-card__desc">登录后即可创建应用、持续对话优化并管理自己的作品。</p>

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
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { message } from 'ant-design-vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

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

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  padding-top: 40px;
}

.auth-card {
  width: min(100%, 420px);
  padding: 32px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(220, 230, 255, 0.9);
  border-radius: 28px;
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.08);
}

.auth-card__title {
  margin: 0 0 12px;
  text-align: center;
  font-size: 1.9rem;
  font-weight: 700;
}

.auth-card__desc {
  margin: 0 0 24px;
  color: var(--app-text-secondary);
  text-align: center;
  line-height: 1.7;
}

.auth-card__tips {
  margin-bottom: 18px;
  color: var(--app-text-secondary);
  text-align: right;
  font-size: 13px;
}
</style>
