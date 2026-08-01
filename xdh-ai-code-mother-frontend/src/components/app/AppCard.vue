<template>
  <article class="app-card" @click="emit('open', app)">
    <div class="app-card__media">
      <img v-if="app.cover" :src="app.cover" :alt="displayName" class="app-card__cover" />

      <div v-else class="app-card__placeholder">
        <img alt="应用占位图" class="app-card__placeholder-logo" src="@/assets/logo.png" />
        <p>等待生成封面</p>
      </div>

      <div class="app-card__overlay" @click.stop>
        <div class="app-card__overlay-buttons">
          <a-button type="primary" @click="emit('open', app)">查看对话</a-button>
          <a-button v-if="hasDeployedWork" @click="emit('openWork', app)">查看作品</a-button>
        </div>
      </div>
    </div>

    <div class="app-card__footer" @click.stop>
      <a-avatar :size="36" :src="app.user?.userAvatar">
        {{ authorName.slice(0, 1) }}
      </a-avatar>

      <div class="app-card__footer-info">
        <span class="app-card__footer-name">{{ displayName }}</span>
        <span class="app-card__footer-id">{{ app.user?.userName }}</span>
      </div>

      <a-dropdown v-if="canEdit || canDelete" trigger="click">
        <a-button type="text" shape="circle">
          <template #icon><MoreOutlined /></template>
        </a-button>
        <template #overlay>
          <a-menu>
            <a-menu-item v-if="canEdit" @click="emit('edit', app)">
              <EditOutlined /><span style="margin-left: 8px">修改</span>
            </a-menu-item>
            <a-menu-item v-if="canDelete" danger @click="emit('delete', app)">
              <DeleteOutlined /><span style="margin-left: 8px">删除</span>
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
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
</script>

<style scoped>
.app-card {
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(223, 232, 255, 0.9);
  border-radius: 12px;
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
  height: 220px;
  overflow: hidden;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.2), rgba(229, 243, 255, 0.9)),
    linear-gradient(135deg, rgba(118, 255, 228, 0.26), rgba(90, 152, 255, 0.24));
}

.app-card__cover {
  width: 100%;
  height: 100%;
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
  border-radius: 12px;
}

.app-card__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.35);
  opacity: 0;
  transition: opacity 0.25s ease;
}

.app-card:hover .app-card__overlay {
  opacity: 1;
}

.app-card__overlay-buttons {
  display: flex;
  gap: 12px;
}

.app-card__footer {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
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
  font-size: 1rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-card__footer-id {
  color: var(--app-text-secondary);
  font-size: 0.82rem;
}

@media (max-width: 640px) {
  .app-card__media {
    height: 180px;
  }

  .app-card__footer {
    padding: 12px 14px;
  }
}
</style>
