<template>
  <article class="app-card" @click="emit('open', app)">
    <div class="app-card__media">
      <img v-if="app.cover" :src="app.cover" :alt="displayName" class="app-card__cover" />

      <div v-else class="app-card__placeholder">
        <img alt="应用占位图" class="app-card__placeholder-logo" src="@/assets/logo.png" />
        <p>等待生成封面</p>
      </div>

      <div class="app-card__badges">
        <a-tag v-if="badge" color="processing">{{ badge }}</a-tag>
        <a-tag v-if="app.codeGenType" color="default">{{ app.codeGenType }}</a-tag>
        <a-tag v-if="app.deployKey" color="success">已部署</a-tag>
      </div>
    </div>

    <div class="app-card__body">
      <div class="app-card__head">
        <h3 class="app-card__title">{{ displayName }}</h3>
        <p class="app-card__time">{{ relativeTime }}</p>
      </div>

      <div v-if="showAuthor" class="app-card__author">
        <a-avatar :size="32" :src="app.user?.userAvatar">
          {{ authorName.slice(0, 1) }}
        </a-avatar>
        <div>
          <div class="app-card__author-name">{{ authorName }}</div>
          <div class="app-card__author-desc">点击查看应用详情和网页效果</div>
        </div>
      </div>

      <div class="app-card__cta-row" @click.stop>
        <a-button type="primary" @click="emit('open', app)">查看对话</a-button>
        <a-button v-if="hasDeployedWork" @click="emit('openWork', app)">查看作品</a-button>
      </div>

      <div v-if="canEdit || canDelete" class="app-card__actions" @click.stop>
        <a-button v-if="canEdit" @click="emit('edit', app)">编辑</a-button>
        <a-button v-if="canDelete" danger @click="emit('delete', app)">删除</a-button>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import {
  formatAppRelativeTime,
  getAppAuthorName,
  getAppDisplayName,
  getAppIdString,
} from '@/utils/app'

const props = withDefaults(
  defineProps<{
    app: API.AppVO
    badge?: string
    showAuthor?: boolean
    canEdit?: boolean
    canDelete?: boolean
  }>(),
  {
    badge: '',
    showAuthor: false,
    canEdit: false,
    canDelete: false,
  }
)

const emit = defineEmits<{
  open: [app: API.AppVO]
  openWork: [app: API.AppVO]
  edit: [app: API.AppVO]
  delete: [app: API.AppVO]
}>()

const displayName = computed(() => getAppDisplayName(props.app))
const authorName = computed(() => getAppAuthorName(props.app))
const relativeTime = computed(() => formatAppRelativeTime(props.app.createTime))
const hasDeployedWork = computed(() => Boolean(getAppIdString(props.app.deployKey)))
</script>

<style scoped>
.app-card {
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(223, 232, 255, 0.9);
  border-radius: 28px;
  cursor: pointer;
  box-shadow: 0 20px 44px rgba(15, 23, 42, 0.08);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 26px 60px rgba(15, 23, 42, 0.12);
}

.app-card__media {
  position: relative;
  height: 240px;
  overflow: hidden;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.2), rgba(229, 243, 255, 0.9)),
    linear-gradient(135deg, rgba(118, 255, 228, 0.26), rgba(90, 152, 255, 0.24));
}

.app-card__cover,
.app-card__iframe {
  width: 100%;
  height: 100%;
  border: 0;
  object-fit: cover;
  background: #ffffff;
}

.app-card__placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  width: 100%;
  height: 100%;
  color: var(--app-text-secondary);
}

.app-card__placeholder-logo {
  width: 72px;
  height: 72px;
  border-radius: 22px;
}

.app-card__badges {
  position: absolute;
  top: 16px;
  left: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.app-card__body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px;
}

.app-card__head {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.app-card__title {
  margin: 0;
  font-size: 1.28rem;
  font-weight: 700;
}

.app-card__time {
  margin: 0;
  color: var(--app-text-secondary);
}

.app-card__author {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 4px;
}

.app-card__author-name {
  font-weight: 600;
}

.app-card__author-desc {
  color: var(--app-text-secondary);
  font-size: 0.92rem;
}

.app-card__cta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.app-card:hover .app-card__cta-row {
  opacity: 1;
}

.app-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.app-card__actions {
  margin-top: auto;
}

@media (max-width: 640px) {
  .app-card__media {
    height: 200px;
  }

  .app-card__body {
    padding: 16px;
  }
}
</style>
