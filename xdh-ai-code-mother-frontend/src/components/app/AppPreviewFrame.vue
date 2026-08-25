<template>
  <div class="app-preview-frame">
    <div v-if="loading" class="app-preview-frame__loading">
      <a-spin size="large" />
      <p v-if="loadingText">{{ loadingText }}</p>
    </div>

    <iframe
      v-else-if="shouldShowFrame"
      ref="iframeRef"
      :key="iframeKey"
      :src="previewUrl"
      :style="iframeStyle"
      class="app-preview-frame__iframe"
      :title="iframeTitle"
      @load="handleIframeLoad"
    />

    <a-empty v-else :description="emptyDescription" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { CSSProperties } from 'vue'

const emit = defineEmits<{
  iframeLoad: [iframe: HTMLIFrameElement]
}>()

const props = withDefaults(
  defineProps<{
    loading?: boolean
    showPreview?: boolean
    previewUrl?: string
    iframeKey?: number | string
    iframeTitle?: string
    loadingText?: string
    emptyDescription?: string
    minHeight?: string
  }>(),
  {
    loading: false,
    showPreview: undefined,
    previewUrl: '',
    iframeKey: undefined,
    iframeTitle: '应用预览',
    loadingText: '',
    emptyDescription: '暂无可展示内容',
    minHeight: '',
  },
)

const shouldShowFrame = computed(() => {
  if (props.showPreview === undefined) {
    return Boolean(props.previewUrl)
  }

  return props.showPreview && Boolean(props.previewUrl)
})

const iframeRef = ref<HTMLIFrameElement>()

const iframeStyle = computed<CSSProperties>(() => ({
  minHeight: props.minHeight || undefined,
}))

const handleIframeLoad = () => {
  if (!iframeRef.value) {
    return
  }

  emit('iframeLoad', iframeRef.value)
}

defineExpose({
  getIframeEl: () => iframeRef.value,
  iframeRef,
})
</script>

<style scoped>
.app-preview-frame {
  display: flex;
  flex: 1;
  min-height: 0;
  width: 100%;
}

.app-preview-frame__loading {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  min-height: 320px;
  color: var(--app-text-secondary);
}

.app-preview-frame__iframe {
  display: block;
  flex: 1;
  width: 100%;
  height: 100%;
  min-height: 0;
  background: #ffffff;
  border: 1px solid rgba(220, 230, 255, 0.9);
  border-radius: 22px;
}

.app-preview-frame :deep(.ant-empty) {
  margin: auto;
}
</style>
