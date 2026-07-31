<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="auth-card__title">创建账号</h2>
      <p class="auth-card__desc">注册后即可通过一句需求开始生成网站应用。</p>

      <a-form ref="formRef" :model="formState" autocomplete="off" layout="vertical">
        <a-form-item
          label="账号"
          name="userAccount"
          validate-trigger="blur"
          :rules="[
            { required: true, message: '请输入账号' },
            { min: 4, message: '账号长度不能小于 4 位' },
          ]"
        >
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
        </a-form-item>

        <a-form-item
          label="密码"
          name="userPassword"
          validate-trigger="blur"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 8, message: '密码长度不能小于 8 位' },
          ]"
        >
          <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
        </a-form-item>

        <a-form-item
          label="确认密码"
          name="checkPassword"
          validate-trigger="blur"
          :rules="[
            { required: true, message: '请再次输入密码' },
            { min: 8, message: '确认密码长度不能小于 8 位' },
            { validator: validateCheckPassword },
          ]"
        >
          <a-input-password
            v-model:value="formState.checkPassword"
            placeholder="请再次输入密码"
          />
        </a-form-item>

        <div class="auth-card__tips">
          已有账号？
          <RouterLink to="/user/login">去登录</RouterLink>
        </div>

        <a-form-item>
          <a-button block type="primary" @click="handleSubmit">注册</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { RouterLink, useRouter } from 'vue-router'

import { userRegister } from '@/api/userController'

const router = useRouter()
const formRef = ref<FormInstance>()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const validateCheckPassword = async (_rule: unknown, value: string) => {
  if (!value || value === formState.userPassword) {
    return Promise.resolve()
  }
  return Promise.reject('两次输入的密码不一致')
}

const doSubmit = async (values: API.UserRegisterRequest) => {
  const res = await userRegister(values)
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功，请登录')
    await router.replace('/user/login')
    return
  }

  message.error(`注册失败，${res.data.message}`)
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  await doSubmit(formState)
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
