<template>
  <a-layout class="basic-layout" :style="layoutStyle">
    <a-layout-header ref="headerRef" class="basic-layout__header">
      <GlobalHeader />
    </a-layout-header>

    <a-layout-content
      :class="[
        'basic-layout__content',
        isImmersiveLayout ? 'basic-layout__content--immersive' : '',
      ]"
    >
      <div class="basic-layout__content-inner">
        <div class="basic-layout__route-view">
          <RouterView v-slot="{ Component }">
            <component :is="Component" />
          </RouterView>
        </div>
      </div>
    </a-layout-content>

    <a-layout-footer v-if="!isImmersiveLayout" class="basic-layout__footer">
      <GlobalFooter />
    </a-layout-footer>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, type CSSProperties } from 'vue'
import type { ComponentPublicInstance } from 'vue'
import { RouterView, useRoute } from 'vue-router'

import GlobalFooter from './components/GlobalFooter.vue'
import GlobalHeader from './components/GlobalHeader.vue'

const route = useRoute()
const isImmersiveLayout = computed(() => route.meta.layout === 'immersive')

const headerRef = ref<ComponentPublicInstance | HTMLElement | null>(null)
const headerHeight = ref(72)

let resizeObserver: ResizeObserver | undefined

const resolveElement = (target: ComponentPublicInstance | HTMLElement | null) => {
  if (!target) {
    return null
  }
  if (target instanceof HTMLElement) {
    return target
  }
  return (target.$el as HTMLElement | undefined) ?? null
}

const updateLayoutHeights = () => {
  headerHeight.value = resolveElement(headerRef.value)?.offsetHeight ?? 72
}

const layoutStyle = computed<CSSProperties>(() => ({
  '--layout-header-height': `${headerHeight.value}px`,
}))

onMounted(async () => {
  await nextTick()
  updateLayoutHeights()

  resizeObserver = new ResizeObserver(() => {
    updateLayoutHeights()
  })

  const headerElement = resolveElement(headerRef.value)

  if (headerElement) {
    resizeObserver.observe(headerElement)
  }

  window.addEventListener('resize', updateLayoutHeights)
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  window.removeEventListener('resize', updateLayoutHeights)
})
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  /* 透明，让页面底部的温馨氛围背景透出来 */
  background: transparent !important;
}

.basic-layout :deep(.ant-layout),
.basic-layout :deep(.ant-layout-content),
.basic-layout :deep(.ant-layout-footer) {
  background: transparent !important;
}

.basic-layout__header {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky, 100);
  height: auto;
  padding: 0 var(--space-6);
  line-height: normal;
  background: rgba(255, 255, 255, 0.86) !important;
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-color);
  box-shadow: none;
}

.basic-layout__content {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  min-height: 0;
  flex: 1;
  padding: var(--space-8) var(--space-6) var(--space-12);
  background: transparent !important;
}

.basic-layout__content--immersive {
  height: calc(100vh - var(--layout-header-height, 72px));
  height: calc(100dvh - var(--layout-header-height, 72px));
  min-height: 480px;
  max-height: calc(100vh - var(--layout-header-height, 72px));
  max-height: calc(100dvh - var(--layout-header-height, 72px));
  padding: 0;
  background: transparent !important;
  overflow: hidden;
}

.basic-layout__content-inner {
  display: flex;
  flex-direction: column;
  flex: 1;
  height: 100%;
  min-height: 0;
  width: 100%;
  margin: 0 auto;
  overflow: hidden;
}

.basic-layout__route-view {
  display: flex;
  flex-direction: column;
  flex: 1;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.basic-layout__footer {
  position: static;
  padding: 0;
  background: transparent !important;
}

@media (max-width: 900px) {
  .basic-layout__header {
    padding: 0 var(--space-4);
  }

  .basic-layout__content {
    padding: var(--space-5) var(--space-4) var(--space-8);
  }

  /* 小屏下沉浸式布局允许纵向滚动，避免对话页内容被裁切 */
  .basic-layout__content--immersive {
    height: auto;
    max-height: none;
    min-height: calc(100dvh - var(--layout-header-height, 72px));
    padding: 0;
    overflow-y: auto;
  }
}
</style>
