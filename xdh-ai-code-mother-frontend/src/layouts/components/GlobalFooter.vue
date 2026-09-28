<template>
  <div class="global-footer">
    <div class="global-footer__inner">
      <span class="global-footer__brand">一句话生所想 · AI 应用生成平台</span>
      <span class="global-footer__hint">用自然语言对话，生成可部署的网站应用</span>
    </div>
  </div>
</template>

<script setup lang="ts"></script>

<style scoped>
.global-footer {
  width: 100%;
  padding: var(--space-6) var(--space-6);
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  background: rgba(255, 255, 255, 0.6);
  border-top: 1px solid var(--border-color);
}

.global-footer__inner {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: var(--space-2) var(--space-3);
  width: 100%;
  max-width: var(--layout-max-width);
  margin: 0 auto;
}

.global-footer__brand {
  color: var(--text-primary);
  font-weight: 500;
}

.global-footer__hint {
  color: var(--text-tertiary);
}

@media (max-width: 640px) {
  .global-footer {
    padding: var(--space-5) var(--space-4);
  }

  .global-footer__inner {
    flex-direction: column;
    gap: var(--space-1);
  }
}
</style>
