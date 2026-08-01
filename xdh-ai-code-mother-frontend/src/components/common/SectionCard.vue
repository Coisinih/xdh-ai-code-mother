<template>
  <section class="section-card">
    <header class="section-card__header">
      <div>
        <h2 class="section-card__title">{{ title }}</h2>
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
}>()
</script>

<style scoped>
.section-card {
  padding: 28px 30px 24px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.06);
}

.section-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}

.section-card__title {
  margin: 0;
  color: #0f172a;
  font-size: 1.7rem;
  font-weight: 700;
}

.section-card__extra {
  width: min(100%, 300px);
}

.section-card__footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 28px;
}

@media (max-width: 960px) {
  .section-card {
    padding: 22px;
  }

  .section-card__header {
    flex-direction: column;
    align-items: stretch;
  }

  .section-card__extra {
    width: 100%;
  }
}
</style>
