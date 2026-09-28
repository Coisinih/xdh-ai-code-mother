<template>
  <article
    :aria-label="`查看应用：${displayName}`"
    class="app-card"
    role="button"
    tabindex="0"
    @click="emit('open', app)"
    @keydown.enter.prevent="emit('open', app)"
    @keydown.space.prevent="emit('open', app)"
  >
    <div class="app-card__media">
      <img
        v-if="showCover"
        :alt="displayName"
        class="app-card__cover"
        :src="app.cover"
        @error="coverFailed = true"
      />

      <div v-else class="app-card__placeholder">
        <img alt="" class="app-card__placeholder-logo" src="@/assets/logo.png" />
        <span class="app-card__placeholder-text">等待生成封面</span>
      </div>

      <span v-if="hasDeployedWork" class="app-card__status">已部署</span>

      <div class="app-card__overlay" @click.stop>
        <div class="app-card__overlay-buttons">
          <a-button size="small" type="primary" @click="emit('open', app)">查看对话</a-button>
          <a-button
            v-if="hasDeployedWork"
            class="app-card__ghost-button"
            size="small"
            @click="emit('openWork', app)"
          >
            查看作品
          </a-button>
        </div>
      </div>
    </div>

    <div class="app-card__footer" @click.stop>
      <a-avatar :size="28" :src="app.user?.userAvatar">
        {{ authorName.slice(0, 1) }}
      </a-avatar>

      <div class="app-card__footer-info">
        <span class="app-card__footer-name">{{ displayName }}</span>
        <span class="app-card__footer-id">{{ authorName }}</span>
      </div>

      <a-dropdown v-if="canEdit || canDelete" trigger="click">
        <a-button aria-label="应用操作" shape="circle" type="text">
          <template #icon><MoreOutlined /></template>
        </a-button>
        <template #overlay>
          <a-menu>
            <a-menu-item v-if="canEdit" @click="emit('edit', app)">
              <EditOutlined /><span class="app-card__menu-label">修改</span>
            </a-menu-item>
            <a-menu-item v-if="canDelete" danger @click="emit('delete', app)">
              <DeleteOutlined /><span class="app-card__menu-label">删除</span>
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { DeleteOutlined, EditOutlined, MoreOutlined } from '@ant-design/icons-vue'

import { getAppAuthorName, getAppDisplayName, getAppIdString } from '@/utils/app'

const props = withDefaults(
  defineProps<{
    app: API.AppVO
    canEdit?: boolean
    canDelete?: boolean
  }>(),
  {
    canEdit: false,
    canDelete: false,
  },
)

const emit = defineEmits<{
  open: [app: API.AppVO]
  openWork: [app: API.AppVO]
  edit: [app: API.AppVO]
  delete: [app: API.AppVO]
}>()

const displayName = computed(() => getAppDisplayName(props.app))
const authorName = computed(() => getAppAuthorName(props.app))
const hasDeployedWork = computed(() => Boolean(getAppIdString(props.app.deployKey)))

// 封面加载失败时回退到占位图，避免出现破图与 alt 文字
const coverFailed = ref(false)
const showCover = computed(() => Boolean(props.app.cover) && !coverFailed.value)

watch(
  () => props.app.cover,
  () => {
    coverFailed.value = false
  },
)
</script>

<style scoped>
.app-card {
  overflow: hidden;
  background: var(--bg-surface);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  box-shadow: var(--shadow-xs);
  transition:
    transform var(--duration-base) var(--ease-out),
    box-shadow var(--duration-base) var(--ease-out),
    border-color var(--duration-base) var(--ease-out);
}

.app-card:hover,
.app-card:focus-visible {
  border-color: var(--color-brand-300);
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.app-card:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.app-card__media {
  position: relative;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: linear-gradient(135deg, var(--color-brand-50), var(--bg-surface-sunken));
  border-bottom: 1px solid var(--border-color);
}

.app-card__cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: var(--bg-surface);
}

.app-card__placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  width: 100%;
  height: 100%;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.app-card__placeholder-logo {
  width: 44px;
  height: 44px;
  object-fit: contain;
  border-radius: var(--radius-md);
  opacity: 0.9;
}

.app-card__status {
  position: absolute;
  top: var(--space-2);
  left: var(--space-2);
  padding: 2px 10px;
  color: var(--color-brand-800);
  font-size: var(--font-size-xs);
  font-weight: 500;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid var(--color-primary-soft-border);
  border-radius: var(--radius-pill);
}

.app-card__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(28, 25, 23, 0.42);
  opacity: 0;
  transition: opacity var(--duration-base) var(--ease-out);
}

.app-card:hover .app-card__overlay,
.app-card:focus-within .app-card__overlay {
  opacity: 1;
}

.app-card__overlay-buttons {
  display: flex;
  gap: var(--space-2);
}

.app-card__ghost-button {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.16);
  border-color: rgba(255, 255, 255, 0.6);
}

.app-card__ghost-button:hover,
.app-card__ghost-button:focus {
  color: #ffffff !important;
  background: rgba(255, 255, 255, 0.28) !important;
  border-color: #ffffff !important;
}

.app-card__footer {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
}

.app-card__footer-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.app-card__footer-name {
  overflow: hidden;
  font-weight: 600;
  font-size: var(--font-size-base);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-card__footer-id {
  overflow: hidden;
  color: var(--text-secondary);
  font-size: var(--font-size-xs);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-card__menu-label {
  margin-left: var(--space-2);
}

@media (max-width: 640px) {
  .app-card__footer {
    padding: var(--space-3);
  }
}
</style>
