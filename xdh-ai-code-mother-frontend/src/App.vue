<template>
  <a-config-provider :locale="locale">
    <BasicLayout />
  </a-config-provider>
</template>

<script lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { defineComponent } from 'vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
dayjs.locale('zh-cn')

export default defineComponent({
  name: 'App',
  components: {
    BasicLayout,
  },
  data() {
    return {
      locale: zhCN,
    }
  },
})
</script>
