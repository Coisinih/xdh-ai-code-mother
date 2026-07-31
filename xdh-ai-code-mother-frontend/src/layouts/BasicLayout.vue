<template>
  <a-layout class="basic-layout">
    <a-layout-header v-if="!isImmersiveLayout" class="basic-layout__header">
      <GlobalHeader />
    </a-layout-header>

    <a-layout-content
      :class="[
        'basic-layout__content',
        isImmersiveLayout ? 'basic-layout__content--immersive' : '',
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

.basic-layout__content--immersive {
  padding: 0;
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

  .basic-layout__content--immersive {
    padding: 0;
  }
}
</style>
