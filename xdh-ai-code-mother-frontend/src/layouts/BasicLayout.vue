<template>
  <a-layout :class="['basic-layout', isHomePage ? 'basic-layout--home' : '']">
    <a-layout-header v-if="!isImmersiveLayout" class="basic-layout__header">
      <GlobalHeader />
    </a-layout-header>

    <a-layout-content
      :class="[
        'basic-layout__content',
        isImmersiveLayout ? 'basic-layout__content--immersive' : '',
        isHomePage ? 'basic-layout__content--home' : '',
      ]"
    >
      <div class="basic-layout__content-inner">
        <RouterView v-slot="{ Component }">
          <keep-alive include="HomePage">
            <component :is="Component" />
          </keep-alive>
        </RouterView>
      </div>
    </a-layout-content>

    <a-layout-footer v-if="!isImmersiveLayout" class="basic-layout__footer">
      <GlobalFooter />
    </a-layout-footer>
  </a-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'

import GlobalFooter from './components/GlobalFooter.vue'
import GlobalHeader from './components/GlobalHeader.vue'

const route = useRoute()
const isImmersiveLayout = computed(() => route.meta.layout === 'immersive')
const isHomePage = computed(() => route.path === '/')
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  background: #ffffff !important;
}

.basic-layout--home {
  background-image: linear-gradient(to top, #a8edea 0%, #fed6e3 100%) !important;
  background-attachment: fixed;
}

.basic-layout :deep(.ant-layout),
.basic-layout :deep(.ant-layout-content),
.basic-layout :deep(.ant-layout-footer) {
  background: transparent !important;
}

.basic-layout__header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: auto;
  padding: 0;
  line-height: normal;
  background: #ffffff !important;
  backdrop-filter: none;
  border-bottom: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: none;
}

.basic-layout__content {
  flex: 1;
  padding: 32px 20px 104px;
  background: #ffffff !important;
}

.basic-layout__content--home {
  padding: 0 0 104px;
  background: transparent !important;
}

.basic-layout__content--immersive {
  padding: 0;
  background: transparent !important;
}

.basic-layout__content-inner {
  width: 100%;
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

  .basic-layout__content--home {
    padding: 0 0 104px;
  }

  .basic-layout__content--immersive {
    padding: 0;
  }
}
</style>
