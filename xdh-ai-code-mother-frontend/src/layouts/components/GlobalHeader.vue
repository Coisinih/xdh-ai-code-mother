<template>
  <div class="global-header">
    <RouterLink class="global-header__brand" to="/">
      <img alt="" class="global-header__logo" src="@/assets/logo.png" />
      <span class="global-header__brand-text">
        <span class="global-header__title">一句话生所想</span>
        <span class="global-header__subtitle">AI 应用生成平台</span>
      </span>
    </RouterLink>

    <a-menu
      v-if="!isCompact"
      class="global-header__menu"
      mode="horizontal"
      :disabled-overflow="true"
      :items="menuItems"
      :selected-keys="selectedKeys"
      @click="handleMenuClick"
    />

    <div class="global-header__actions">
      <a-dropdown v-if="isCompact" placement="bottomRight" trigger="click">
        <a-button
          aria-label="打开导航菜单"
          class="global-header__compact-trigger"
          size="large"
        >
          <template #icon>
            <MenuOutlined />
          </template>
        </a-button>
        <template #overlay>
          <a-menu :items="compactMenuItems" :selected-keys="selectedKeys" @click="handleCompactMenu" />
        </template>
      </a-dropdown>

      <div v-if="loginUserStore.loginUser.id">
        <a-dropdown placement="bottomRight">
          <button aria-label="账号菜单" class="global-header__user" type="button">
            <a-avatar :size="30" :src="loginUserStore.loginUser.userAvatar">
              {{ userInitial }}
            </a-avatar>
            <span class="global-header__user-name">
              {{ loginUserStore.loginUser.userName ?? '未命名用户' }}
            </span>
            <DownOutlined class="global-header__user-caret" />
          </button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="logout" @click="handleLogout">
                <LogoutOutlined />
                <span class="global-header__menu-label">退出登录</span>
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
      <div v-else>
        <a-button class="global-header__login" type="primary" @click="router.push('/user/login')">
          登录
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onBeforeUnmount, onMounted, ref } from 'vue'
import {
  AppstoreOutlined,
  CommentOutlined,
  DownOutlined,
  HomeOutlined,
  LogoutOutlined,
  MenuOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { type MenuProps, message } from 'ant-design-vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { userLogout } from '@/api/userController'
import { useLoginUserStore } from '@/stores/loginUser'

type HeaderMenuItem = {
  key: string
  label: string
  path?: string
  icon: typeof HomeOutlined
}

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const originItems: HeaderMenuItem[] = [
  { key: '/', label: '首页', path: '/', icon: HomeOutlined },
  { key: '/admin/userManage', label: '用户管理', path: '/admin/userManage', icon: UserOutlined },
  { key: '/admin/appManage', label: '应用管理', path: '/admin/appManage', icon: AppstoreOutlined },
  {
    key: '/admin/chatHistoryManage',
    label: '对话管理',
    path: '/admin/chatHistoryManage',
    icon: CommentOutlined,
  },
]

const isCompact = ref(false)
let mediaQuery: MediaQueryList | undefined

const syncCompact = () => {
  isCompact.value = mediaQuery?.matches ?? false
}

const visibleItems = computed(() =>
  originItems.filter((menu) => {
    // 如果是 /admin 菜单，只有管理员才能展示
    if (menu.path?.startsWith('/admin')) {
      return loginUserStore.loginUser.userRole === 'admin'
    }
    // 其他菜单正常显示
    return true
  }),
)

const menuItems = computed<MenuProps['items']>(() =>
  visibleItems.value
    .map((menu) => ({
      key: menu.key,
      label: menu.label,
      icon: h(menu.icon),
    })),
)

const compactMenuItems = computed<MenuProps['items']>(() => {
  const items: NonNullable<MenuProps['items']> = visibleItems.value.map((menu) => ({
    key: menu.key,
    label: menu.label,
    icon: h(menu.icon),
  }))

  items.push({ type: 'divider' })

  if (loginUserStore.loginUser.id) {
    items.push({
      key: 'logout',
      label: '退出登录',
      icon: h(LogoutOutlined),
    })
  } else {
    items.push({
      key: 'login',
      label: '登录',
      icon: h(UserOutlined),
    })
  }

  return items
})

const handleMenuClick: MenuProps['onClick'] = (event) => {
  const key = event.key as string
  if (key.startsWith('/')) {
    void router.push(key)
  }
}

const handleCompactMenu: MenuProps['onClick'] = (event) => {
  const key = event.key as string
  if (key === 'logout') {
    void handleLogout()
    return
  }
  if (key === 'login') {
    void router.push('/user/login')
    return
  }
  handleMenuClick(event)
}

const selectedKeys = computed(() => {
  if (route.path.startsWith('/admin/chatHistoryManage')) {
    return ['/admin/chatHistoryManage']
  }
  if (route.path.startsWith('/admin/appManage')) {
    return ['/admin/appManage']
  }
  if (route.path.startsWith('/admin/userManage')) {
    return ['/admin/userManage']
  }
  return ['/']
})

const userInitial = computed(() => (loginUserStore.loginUser.userName || 'U').slice(0, 1))

const handleLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
    return
  }
  message.error(`退出登录失败，${res.data.message}`)
}

onMounted(() => {
  mediaQuery = window.matchMedia('(max-width: 900px)')
  syncCompact()
  mediaQuery.addEventListener('change', syncCompact)
})

onBeforeUnmount(() => {
  mediaQuery?.removeEventListener('change', syncCompact)
})
</script>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  gap: var(--space-6);
  height: 72px;
  width: 100%;
  max-width: var(--layout-max-width);
  margin: 0 auto;
}

.global-header__brand {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  gap: 10px;
  min-width: 0;
  padding: var(--space-1) 0;
  border-radius: var(--radius-sm);
}

.global-header__logo {
  width: 38px;
  height: 38px;
  object-fit: contain;
  border-radius: var(--radius-sm);
  box-shadow: 0 4px 12px rgba(180, 83, 9, 0.16);
}

.global-header__brand-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
  line-height: 1.25;
}

.global-header__title {
  color: var(--text-primary);
  font-size: var(--font-size-md);
  font-weight: 600;
  letter-spacing: -0.01em;
  white-space: nowrap;
}

.global-header__subtitle {
  color: var(--text-tertiary);
  font-size: var(--font-size-xs);
  white-space: nowrap;
}

.global-header__menu {
  flex: 1;
  min-width: 0;
  background: transparent;
  border-bottom: none;
}

:deep(.global-header__menu.ant-menu-horizontal) {
  height: 72px;
  line-height: 72px;
  background: transparent;
  border-bottom: none;
}

:deep(.global-header__menu.ant-menu-horizontal > .ant-menu-item) {
  padding: 0 var(--space-3);
  font-size: var(--font-size-base);
  border-radius: var(--radius-sm);
}

:deep(.global-header__menu.ant-menu-horizontal > .ant-menu-item::after) {
  display: none;
}

:deep(.global-header__menu.ant-menu-horizontal > .ant-menu-item-selected) {
  background: var(--color-primary-soft);
}

:deep(.global-header__menu .ant-menu-title-content) {
  font-weight: 500;
}

.global-header__actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  gap: var(--space-2);
  margin-left: auto;
}

.global-header__compact-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  padding: 0;
}

.global-header__user {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  max-width: 220px;
  padding: 4px 12px 4px 4px;
  color: var(--text-primary);
  font-size: var(--font-size-base);
  font-family: inherit;
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius-pill);
  cursor: pointer;
  transition:
    background var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-out);
}

.global-header__user:hover {
  background: var(--bg-surface-muted);
  border-color: var(--border-color);
}

.global-header__user-name {
  overflow: hidden;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.global-header__user-caret {
  color: var(--text-tertiary);
  font-size: 11px;
}

.global-header__login {
  height: 42px;
  padding: 0 var(--space-5);
  border-radius: var(--radius-sm);
}

.global-header__menu-label {
  margin-left: var(--space-2);
}

@media (max-width: 1080px) {
  .global-header__subtitle {
    display: none;
  }
}

@media (max-width: 900px) {
  .global-header {
    gap: var(--space-3);
  }

  .global-header__user-name {
    display: none;
  }

  .global-header__user {
    max-width: none;
    padding: 3px;
  }

  .global-header__user-caret {
    display: none;
  }
}
</style>
