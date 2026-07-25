<template>
  <a-layout class="basic-layout">
    <a-layout-header class="basic-layout__header">
      <GlobalHeader :menu-items="menuItems" />
    </a-layout-header>

    <a-layout-content class="basic-layout__content">
      <div class="basic-layout__content-inner">
        <RouterView />
      </div>
    </a-layout-content>

    <a-layout-footer class="basic-layout__footer">
      <GlobalFooter />
    </a-layout-footer>
  </a-layout>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { RouterView } from 'vue-router'

import GlobalFooter from './components/GlobalFooter.vue'
import GlobalHeader from './components/GlobalHeader.vue'

interface MenuItemConfig {
  key: string
  label: string
  path: string
}

export default defineComponent({
  name: 'BasicLayout',
  components: {
    GlobalFooter,
    GlobalHeader,
    RouterView,
  },
  setup() {
    // 使用方式：把所有导航项集中写在这里，再通过 props 传给头部组件。
    // 优势：布局层统一管理导航配置，头部组件只负责展示，复用性更好。
    const menuItems: MenuItemConfig[] = [
      {
        key: 'home',
        label: '首页',
        path: '/',
      },
      {
        key: 'about',
        label: '关于我们',
        path: '/about',
      },
    ]

    return {
      menuItems,
    }
  },
})
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
}

.basic-layout__header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: auto;
  padding: 0;
  line-height: normal;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid var(--app-border);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.05);
}

.basic-layout__content {
  flex: 1;
  padding: 32px 20px 104px;
}

.basic-layout__content-inner {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.basic-layout__footer {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 90;
  padding: 0;
  background: transparent;
}

@media (max-width: 768px) {
  .basic-layout__content {
    padding: 20px 16px 112px;
  }
}
</style>
