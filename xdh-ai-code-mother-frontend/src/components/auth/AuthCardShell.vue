<template>
  <div class="auth-page">
    <section class="auth-card">
      <span class="auth-card__brand">
        <img alt="" class="auth-card__logo" src="@/assets/logo.png" />
        一句话生所想
      </span>
      <h1 class="auth-card__title">{{ title }}</h1>
      <p class="auth-card__desc">{{ description }}</p>
      <slot />
    </section>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description: string
}>()
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: var(--space-10) 0 var(--space-12);
}

.auth-card {
  width: min(100%, 420px);
  padding: var(--space-8);
  background: var(--bg-surface);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}

.auth-card__brand {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-5);
  color: var(--color-brand-800);
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.auth-card__logo {
  width: 28px;
  height: 28px;
  object-fit: contain;
  border-radius: var(--radius-pill);
}

.auth-card__title {
  margin: 0 0 var(--space-2);
  font-size: var(--font-size-xl);
  font-weight: 600;
  letter-spacing: -0.01em;
}

.auth-card__desc {
  margin: 0 0 var(--space-6);
  color: var(--text-secondary);
  line-height: 1.7;
}

.auth-card :deep(.auth-card__tips) {
  margin-bottom: var(--space-5);
  color: var(--text-secondary);
  text-align: right;
  font-size: var(--font-size-sm);
}

@media (max-width: 640px) {
  .auth-page {
    padding: var(--space-6) 0 var(--space-8);
  }

  .auth-card {
    padding: var(--space-6);
  }
}
</style>
