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
  --app-bg: #f5fbff;
  --app-surface: rgba(255, 255, 255, 0.9);
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
}

body {
  margin: 0;
  color: var(--app-text);
  font-family: 'Avenir Next', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background:
    radial-gradient(circle at top left, rgba(120, 255, 228, 0.16), transparent 28%),
    radial-gradient(circle at top right, rgba(122, 150, 255, 0.18), transparent 24%),
    var(--app-bg);
}

a {
  color: inherit;
}
</style>
