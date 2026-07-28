<template>
  <div class="userLogin">
    <h2 class="title">咸蛋黄 AI 应用生成平台 - 用户登入</h2>
    <div class="desc">不写一行代码，生成完整应用</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="doSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不能小于8位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>

      <div class="tips">
        没有账号？
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">登入</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogin } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'

const loginUserStore = useLoginUserStore()
const router = useRouter()
const route = useRoute()

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
const doSubmit = async (values: any) => {
  const res = await userLogin(values)
  // 登入成功，将登入状态保存到全局状态中
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登入成功')
    // 登入成功，跳转到首页
    const redirect = route.query.redirect as string
    router.replace(redirect || '/')
  } else {
    message.error('登入失败，' + res.data.message)
  }
}
</script>

<style>
.userLogin {
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
