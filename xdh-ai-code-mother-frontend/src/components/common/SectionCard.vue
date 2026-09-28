<template>
  <section class="section-card">
    <header class="section-card__header">
      <div class="section-card__heading">
        <h2 class="section-card__title">{{ title }}</h2>
        <p v-if="description" class="section-card__desc">{{ description }}</p>
      </div>

      <div v-if="$slots.extra" class="section-card__extra">
        <slot name="extra" />
      </div>
    </header>

    <div class="section-card__body">
      <slot />
    </div>

    <footer v-if="$slots.footer" class="section-card__footer">
      <slot name="footer" />
    </footer>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<style scoped>
.section-card {
  padding: var(--space-5);
  background: var(--bg-surface);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.section-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-5);
  margin-bottom: var(--space-4);
}

.section-card__heading {
  min-width: 0;
}

.section-card__title {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: 600;
  letter-spacing: -0.01em;
}

.section-card__desc {
  margin: var(--space-1) 0 0;
  color: var(--text-secondary);
  font-size: var(--font-size-base);
}

.section-card__extra {
  flex-shrink: 0;
  width: min(100%, 260px);
}

.section-card__footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  margin-top: var(--space-4);
  padding-top: var(--space-3);
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  border-top: 1px solid var(--border-color);
}

@media (max-width: 960px) {
  .section-card {
    padding: var(--space-5);
  }

  .section-card__header {
    flex-direction: column;
    align-items: stretch;
    gap: var(--space-3);
  }

  .section-card__extra {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .section-card {
    padding: var(--space-4);
  }
}
</style>
