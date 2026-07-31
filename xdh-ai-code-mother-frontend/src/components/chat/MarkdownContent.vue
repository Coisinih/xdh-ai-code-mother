<template>
  <div class="markdown-content" v-html="html"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import { renderMarkdown } from '@/utils/markdown'

const props = defineProps<{
  content?: string
}>()

const html = computed(() => renderMarkdown(props.content))
</script>

<style scoped>
.markdown-content {
  color: inherit;
  font-size: 0.98rem;
  line-height: 1.75;
  word-break: break-word;
}

.markdown-content :deep(:first-child) {
  margin-top: 0;
}

.markdown-content :deep(:last-child) {
  margin-bottom: 0;
}

.markdown-content :deep(p),
.markdown-content :deep(ul),
.markdown-content :deep(ol),
.markdown-content :deep(blockquote),
.markdown-content :deep(pre),
.markdown-content :deep(table) {
  margin: 0 0 0.85em;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin: 1em 0 0.5em;
  font-weight: 700;
  line-height: 1.35;
}

.markdown-content :deep(h1) {
  font-size: 1.35em;
}

.markdown-content :deep(h2) {
  font-size: 1.22em;
}

.markdown-content :deep(h3) {
  font-size: 1.1em;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  padding-left: 1.4em;
}

.markdown-content :deep(li + li) {
  margin-top: 0.28em;
}

.markdown-content :deep(a) {
  color: #1f7aff;
  text-decoration: underline;
}

.markdown-content :deep(blockquote) {
  padding: 0.2em 0 0.2em 0.9em;
  color: #64748b;
  border-left: 3px solid #cbd5e1;
}

.markdown-content :deep(hr) {
  margin: 1em 0;
  border: 0;
  border-top: 1px solid #e2e8f0;
}

.markdown-content :deep(code) {
  padding: 0.12em 0.35em;
  color: #3f3a36;
  font-family: Consolas, 'SFMono-Regular', Menlo, Monaco, 'Courier New', monospace;
  font-size: 0.92em;
  background: #efece7;
  border-radius: 6px;
}

.markdown-content :deep(pre) {
  overflow: auto;
  padding: 14px 16px;
  color: #3f3a36;
  background: #f3f0eb;
  border: 1px solid #e7e2da;
  border-radius: 12px;
}

.markdown-content :deep(pre code) {
  display: block;
  padding: 0;
  color: inherit;
  font-size: 0.88em;
  line-height: 1.65;
  white-space: pre;
  background: transparent;
  border-radius: 0;
}

.markdown-content :deep(pre.hljs) {
  background: #f3f0eb;
}

.markdown-content :deep(table) {
  width: 100%;
  overflow: hidden;
  border-collapse: collapse;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
}

.markdown-content :deep(th) {
  background: #f8fafc;
  font-weight: 600;
}

.markdown-content :deep(img) {
  max-width: 100%;
  border-radius: 10px;
}
</style>
