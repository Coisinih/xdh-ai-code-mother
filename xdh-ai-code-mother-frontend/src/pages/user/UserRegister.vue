<template>
  <div class="userRegister">
    <h2 class="title">咸蛋黄 AI 应用生成平台 - 用户注册</h2>
    <div class="desc">不写一行代码，生成完整应用</div>
    <a-form ref="formRef" :model="formState" name="basic" autocomplete="off">
      <a-form-item
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

      <div class="tips">
        已有账号？
        <RouterLink to="/user/login">去登录</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" style="width: 100%" @click="handleSubmit">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController.ts'

const router = useRouter()
const formRef = ref<FormInstance>()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const validateCheckPassword = async (_rule: any, value: string) => {
  if (!value || value === formState.userPassword) {
    return Promise.resolve()
  }
  return Promise.reject('两次输入的密码不一致')
}

const doSubmit = async (values: API.UserRegisterRequest) => {
  const res = await userRegister(values)
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功，请登录')
    router.replace('/user/login')
  } else {
    message.error('注册失败，' + res.data.message)
  }
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  await doSubmit(formState)
}
</script>

<style>
.userRegister {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  margin-bottom: 16px;
  text-align: center;
}

.desc {
  margin-bottom: 16px;
  text-align: center;
  color: #999;
}

.tips {
  margin-bottom: 16px;
  text-align: right;
  color: #999;
  font-size: 13px;
}
</style>
