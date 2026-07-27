<template>
  <div class="global-header">
    <RouterLink class="global-header__brand" to="/">
      <img alt="咸蛋黄 AI" class="global-header__logo" src="@/assets/logo.png" />
      <span class="global-header__title">咸蛋黄AI零代码应用生成平台</span>
    </RouterLink>

    <a-menu
      class="global-header__menu"
      mode="horizontal"
      :selected-keys="selectedKeys"
      @click="handleMenuClick"
    >
      <a-menu-item v-for="item in menuItems" :key="item.key">
        {{ item.label }}
      </a-menu-item>
    </a-menu>

    <div class="global-header__actions">
      <div v-if="loginUserStore.loginUser.id">
        <a-dropdown>
          <a-space>
            <a-avatar :src="loginUserStore.loginUser.userAvatar" />
            {{ loginUserStore.loginUser.userName ?? '无名' }}
          </a-space>
          <template #overlay>
            <a-menu>
              <a-menu-item key="logout" @click="handleLogout">
                <LogoutOutlined />
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
      <div v-else>
        <a-button type="primary" href="/user/login">登录</a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { LogoutOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { message } from 'ant-design-vue'

interface MenuItemConfig {
  key: string
  label: string
  path: string
}

const props = withDefaults(
  defineProps<{
    menuItems: MenuItemConfig[]
  }>(),
  {
    menuItems: () => [],
  },
)

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const selectedKeys = computed(() => {
  const matchedItem =
    props.menuItems.find((item) => route.path === item.path) ??
    props.menuItems.find((item) => item.path !== '/' && route.path.startsWith(item.path))

  return matchedItem ? [matchedItem.key] : []
})

const handleMenuClick = ({ key }: { key: string }) => {
  const targetItem = props.menuItems.find((item) => item.key === key)

  if (targetItem && targetItem.path !== route.path) {
    router.push(targetItem.path)
  }
}

const handleLogout = async () => {
  // 1.调用用户注销接口
  const res = await userLogout()
  if (res.data.code === 0) {
    // 2.清除用户信息
    loginUserStore.setLoginUser({
      userName: '未登入',
    })
    message.success('退出登入成功')
    // 3.跳转到登录页
    router.push('/user/login')
  } else {
    message.error('退出登入失败，' + res.data.message)
  }
}
</script>

<style scoped>
.global-header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 16px 24px;
  width: 100%;
  padding: 0 32px;
}

.global-header__brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  padding: 18px 0;
}

.global-header__logo {
  width: 40px;
  height: 40px;
  object-fit: contain;
  border-radius: 10px;
  box-shadow: 0 8px 20px rgba(22, 119, 255, 0.18);
}

.global-header__title {
  color: var(--app-text);
  font-size: 1.05rem;
  font-weight: 600;
  letter-spacing: 0.01em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.global-header__menu {
  min-width: 0;
  justify-self: end;
  background: transparent;
  border-bottom: none;
}

:deep(.global-header__menu.ant-menu-horizontal) {
  justify-content: flex-end;
  border-bottom: none;
}

.global-header__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-width: max-content;
}

@media (max-width: 1024px) {
  .global-header {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 12px 16px;
  }

  .global-header__brand {
    width: 100%;
    justify-content: center;
    padding: 0;
  }

  .global-header__menu {
    width: 100%;
    justify-self: stretch;
  }

  :deep(.global-header__menu.ant-menu-horizontal) {
    justify-content: center;
    overflow-x: auto;
    overflow-y: hidden;
    white-space: nowrap;
  }

  .global-header__actions {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 640px) {
  .global-header__title {
    font-size: 0.95rem;
    white-space: normal;
    text-align: center;
    overflow: visible;
  }
}
</style>
