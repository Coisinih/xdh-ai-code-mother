<template>
  <a-config-provider :locale="locale">
    <BasicLayout />
  </a-config-provider>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.locale('zh-cn')
dayjs.extend(relativeTime)

const locale = zhCN
</script>

<style>
:root {
  --app-bg: #e8f7ff;
  --app-surface: rgba(255, 255, 255, 0.96);
  --app-border: rgba(220, 230, 255, 0.9);
  --app-text: #172033;
  --app-text-secondary: #64748b;
  --app-primary: #1f7aff;
}

* {
  box-sizing: border-box;
}

html,
body,
#app {
  min-height: 100%;
  background: transparent;
}

body {
  margin: 0;
  color: var(--app-text);
  font-family: 'Avenir Next', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: #ffffff;
}

#app {
  min-height: 100vh;
}

a {
  color: inherit;
}
</style>
