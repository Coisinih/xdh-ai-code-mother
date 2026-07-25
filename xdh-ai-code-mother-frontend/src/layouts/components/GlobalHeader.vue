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
      <a-button type="primary">登录</a-button>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent } from 'vue'
import type { PropType } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

interface MenuItemConfig {
  key: string
  label: string
  path: string
}

export default defineComponent({
  name: 'GlobalHeader',
  components: {
    RouterLink,
  },
  props: {
    menuItems: {
      type: Array as PropType<MenuItemConfig[]>,
      default: () => [],
    },
  },
  setup(props) {
    const route = useRoute()
    const router = useRouter()

    // 使用方式：把 computed 结果直接绑定给 a-menu 的 selectedKeys。
    // 优势：当前路由变化时会自动重新计算，高亮状态始终和页面保持同步。
    const selectedKeys = computed(() => {
      const matchedItem =
        props.menuItems.find((item) => route.path === item.path) ??
        props.menuItems.find((item) => item.path !== '/' && route.path.startsWith(item.path))

      return matchedItem ? [matchedItem.key] : []
    })

    // 使用方式：Menu 点击后只拿到 key，我们通过配置反查目标路由并跳转。
    // 优势：模板层不需要写死跳转地址，后续接入权限菜单或接口菜单时更容易扩展。
    const handleMenuClick = ({ key }: { key: string }) => {
      const targetItem = props.menuItems.find((item) => item.key === key)

      if (targetItem && targetItem.path !== route.path) {
        router.push(targetItem.path)
      }
    }

    return {
      selectedKeys,
      handleMenuClick,
    }
  },
})
</script>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  gap: 24px;
  width: 100%;
  padding: 0 32px;
}

.global-header__brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex-shrink: 0;
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
}

.global-header__menu {
  min-width: fit-content;
  margin-left: auto;
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
  flex-shrink: 0;
  min-width: fit-content;
}

@media (max-width: 900px) {
  .global-header {
    flex-wrap: wrap;
    justify-content: center;
    gap: 12px;
    padding: 12px 16px;
  }

  .global-header__brand {
    width: 100%;
    justify-content: center;
    padding: 0;
  }

  .global-header__menu {
    order: 3;
    width: 100%;
    margin-left: 0;
  }

  :deep(.global-header__menu.ant-menu-horizontal) {
    justify-content: flex-start;
    overflow-x: auto;
    overflow-y: hidden;
    white-space: nowrap;
  }

  .global-header__actions {
    margin-left: auto;
  }
}

@media (max-width: 640px) {
  .global-header__title {
    font-size: 0.95rem;
    white-space: normal;
    text-align: center;
  }

  .global-header__actions {
    width: 100%;
    justify-content: center;
  }
}
</style>
