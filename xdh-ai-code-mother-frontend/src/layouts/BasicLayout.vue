<template>
  <a-layout :class="['basic-layout', isHomePage ? 'basic-layout--home' : '']" :style="layoutStyle">
    <a-layout-header ref="headerRef" class="basic-layout__header">
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
        <div class="basic-layout__route-view">
          <RouterView v-slot="{ Component }">
            <keep-alive include="HomePage">
              <component :is="Component" />
            </keep-alive>
          </RouterView>
        </div>
      </div>
    </a-layout-content>

    <a-layout-footer ref="footerRef" class="basic-layout__footer">
      <GlobalFooter />
    </a-layout-footer>
  </a-layout>
</template>

<script setup lang="ts">
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  type ComponentPublicInstance,
  type CSSProperties,
} from 'vue'
import { RouterView, useRoute } from 'vue-router'

import GlobalFooter from './components/GlobalFooter.vue'
import GlobalHeader from './components/GlobalHeader.vue'

const route = useRoute()
const isImmersiveLayout = computed(() => route.meta.layout === 'immersive')
const isHomePage = computed(() => route.path === '/')

const headerRef = ref<ComponentPublicInstance | HTMLElement | null>(null)
const footerRef = ref<ComponentPublicInstance | HTMLElement | null>(null)
const headerHeight = ref(0)
const footerHeight = ref(0)

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
  headerHeight.value = resolveElement(headerRef.value)?.offsetHeight ?? 0
  footerHeight.value = resolveElement(footerRef.value)?.offsetHeight ?? 0
}

const layoutStyle = computed<CSSProperties>(() => ({
  '--layout-header-height': `${headerHeight.value}px`,
  '--layout-footer-height': `${footerHeight.value}px`,
}))

onMounted(async () => {
  await nextTick()
  updateLayoutHeights()

  resizeObserver = new ResizeObserver(() => {
    updateLayoutHeights()
  })

  const headerElement = resolveElement(headerRef.value)
  const footerElement = resolveElement(footerRef.value)

  if (headerElement) {
    resizeObserver.observe(headerElement)
  }
  if (footerElement) {
    resizeObserver.observe(footerElement)
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
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  min-height: 0;
  flex: 1;
  padding: 32px 20px 104px;
  background: #ffffff !important;
}

.basic-layout__content--home {
  padding: 0 0 104px;
  background: transparent !important;
}

.basic-layout__content--immersive {
  height: calc(100vh - var(--layout-header-height, 0px) - var(--layout-footer-height, 0px));
  min-height: calc(100vh - var(--layout-header-height, 0px) - var(--layout-footer-height, 0px));
  max-height: calc(100vh - var(--layout-header-height, 0px) - var(--layout-footer-height, 0px));
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
