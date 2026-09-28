<template>
  <a-config-provider :locale="locale" :theme="antdTheme">
    <BasicLayout />
  </a-config-provider>
</template>

<script setup lang="ts">
import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context'

import BasicLayout from '@/layouts/BasicLayout.vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.locale('zh-cn')
dayjs.extend(relativeTime)

const locale = zhCN

/**
 * 让 antd 组件与自定义样式共用同一套品牌令牌，
 * 彻底解决此前"组件默认蓝 + 自定义蓝"两套主色并存的问题。
 */
const antdTheme: ThemeConfig = {
  token: {
    colorPrimary: '#C2410C',
    colorInfo: '#C2410C',
    colorLink: '#C2410C',
    colorLinkHover: '#9A3412',
    colorSuccess: '#16A34A',
    colorWarning: '#D97706',
    colorError: '#DC2626',
    colorTextBase: '#1C1917',
    colorBgBase: '#FFFFFF',
    colorBorder: '#E4DCD0',
    colorBorderSecondary: '#EFE9DF',
    borderRadius: 10,
    borderRadiusLG: 14,
    borderRadiusSM: 8,
    controlHeight: 40,
    controlHeightLG: 46,
    fontSize: 15,
    boxShadowSecondary: '0 8px 24px rgba(28, 25, 23, 0.07)',
  },
}
</script>
