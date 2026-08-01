<template>
  <div class="auth-page">
    <section class="auth-card">
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
  padding-top: 40px;
}

.auth-card {
  width: min(100%, 420px);
  padding: 32px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(220, 230, 255, 0.9);
  border-radius: 28px;
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.08);
}

.auth-card__title {
  margin: 0 0 12px;
  text-align: center;
  font-size: 1.9rem;
  font-weight: 700;
}

.auth-card__desc {
  margin: 0 0 24px;
  color: var(--app-text-secondary);
  text-align: center;
  line-height: 1.7;
}

.auth-card :deep(.auth-card__tips) {
  margin-bottom: 18px;
  color: var(--app-text-secondary);
  text-align: right;
  font-size: 13px;
}
</style>
