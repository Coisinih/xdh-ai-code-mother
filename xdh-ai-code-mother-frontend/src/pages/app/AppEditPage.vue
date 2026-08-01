<template>
  <div class="app-edit-page">
    <section class="app-edit-page__form-panel">
      <div class="app-edit-page__header">
        <div>
          <h1 class="app-edit-page__title">应用信息编辑</h1>
          <p class="app-edit-page__desc">
            {{
              isAdmin
                ? '管理员可修改应用名称、封面和优先级。'
                : '普通用户目前仅可修改自己应用的名称。'
            }}
          </p>
        </div>
        <a-space>
          <a-button @click="goBack">返回首页</a-button>
          <a-button :loading="saving" type="primary" @click="handleSubmit">保存修改</a-button>
        </a-space>
      </div>

      <a-skeleton active :loading="loading" paragraph>
        <a-form layout="vertical">
          <a-form-item label="应用名称" required>
            <a-input
              v-model:value="formState.appName"
              :maxlength="100"
              placeholder="请输入应用名称"
            />
          </a-form-item>

          <a-form-item label="应用封面">
            <a-input
              v-model:value="formState.cover"
              :disabled="!isAdmin"
              placeholder="请输入封面图片 URL"
            />
            <div class="app-edit-page__field-tip">
              {{
                isAdmin
                  ? '管理员可填写封面图片地址，用于首页和管理页展示。'
                  : '普通用户暂不支持修改封面。'
              }}
            </div>
          </a-form-item>

          <a-form-item label="优先级">
            <a-input-number
              v-model:value="formState.priority"
              :disabled="!isAdmin"
              :max="999"
              :min="0"
              style="width: 100%"
            />
            <div class="app-edit-page__field-tip">
              {{
                isAdmin ? '精选应用建议设置为 99。' : '普通用户暂不支持修改优先级。'
              }}
            </div>
          </a-form-item>

          <a-descriptions bordered :column="1" size="small" title="应用信息">
            <a-descriptions-item label="应用 ID">{{ appDetail.id || '-' }}</a-descriptions-item>
            <a-descriptions-item label="创建用户">
              {{ appDetail.user?.userName || '未知用户' }}
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">{{ createdAt }}</a-descriptions-item>
            <a-descriptions-item label="更新时间">{{ updatedAt }}</a-descriptions-item>
          </a-descriptions>
        </a-form>
      </a-skeleton>
    </section>

    <section class="app-edit-page__preview-panel">
      <div class="app-edit-page__preview-header">
        <div>
          <h2 class="app-edit-page__preview-title">效果预览</h2>
          <p class="app-edit-page__preview-desc">
            如果应用已经生成网页，这里会展示当前预览。
          </p>
        </div>
        <a-button v-if="previewUrl" type="default" @click="openPreview">新窗口查看</a-button>
      </div>

      <div class="app-edit-page__preview-body">
        <AppPreviewFrame
          :preview-url="previewUrl"
          :show-preview="Boolean(previewUrl)"
          empty-description="当前应用还没有可展示的网页。"
          iframe-title="应用预览"
          min-height="600px"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'

import AppPreviewFrame from '@/components/app/AppPreviewFrame.vue'
import { useRouteAppId } from '@/composables/useRouteAppId'
import { adminGetAppVoById, adminUpdateApp, getAppVoById, updateApp } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { formatDateTime, resolveAppPreviewUrl, toApiRequestId, type AppIdentifier } from '@/utils/app'
import { openInNewTab } from '@/utils/browser'
import { markHomeRefreshNeeded } from '@/utils/homeRefresh'

type AppEditFormState = Omit<API.AppAdminUpdateRequest, 'id'> & {
  id?: AppIdentifier
}

const route = useRoute()
const router = useRouter()
const appId = useRouteAppId()
const loginUserStore = useLoginUserStore()

const loading = ref(false)
const saving = ref(false)
const appDetail = reactive<API.AppVO>({})
const formState = reactive<AppEditFormState>({
  id: undefined,
  appName: '',
  cover: '',
  priority: 0,
})

const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')
const previewUrl = computed(() => resolveAppPreviewUrl(appDetail))
const createdAt = computed(() => formatDateTime(appDetail.createTime))
const updatedAt = computed(() => formatDateTime(appDetail.updateTime))

const ensureEditable = async () => {
  if (isAdmin.value) {
    return true
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.replace(`/user/login?redirect=${encodeURIComponent(route.fullPath)}`)
    return false
  }

  if (String(appDetail.userId) !== String(loginUserStore.loginUser.id)) {
    message.error('你只能编辑自己的应用')
    await router.replace('/')
    return false
  }

  return true
}

const fillFormState = () => {
  formState.id = appDetail.id
  formState.appName = appDetail.appName || ''
  formState.cover = appDetail.cover || ''
  formState.priority = appDetail.priority ?? 0
}

const loadAppDetail = async () => {
  if (!appId.value) {
    message.error('应用 id 无效')
    await router.replace('/')
    return
  }

  loading.value = true
  try {
    const res = isAdmin.value
      ? await adminGetAppVoById({ id: toApiRequestId(appId.value) })
      : await getAppVoById({ id: toApiRequestId(appId.value) })

    if (res.data.code === 0 && res.data.data) {
      Object.assign(appDetail, res.data.data)
      if (!(await ensureEditable())) {
        return
      }
      fillFormState()
      return
    }

    message.error(res.data.message || '获取应用详情失败')
    await router.replace('/')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formState.id) {
    return
  }

  const appName = formState.appName?.trim()
  if (!appName) {
    message.warning('请输入应用名称')
    return
  }

  saving.value = true
  try {
    if (isAdmin.value) {
      const res = await adminUpdateApp({
        id: toApiRequestId(formState.id),
        appName,
        cover: formState.cover?.trim(),
        priority: formState.priority ?? 0,
      })
      if (res.data.code === 0) {
        markHomeRefreshNeeded()
        message.success('更新成功')
        await loadAppDetail()
        return
      }
      message.error(res.data.message || '更新失败')
      return
    }

    const res = await updateApp({
      id: toApiRequestId(formState.id),
      appName,
    })
    if (res.data.code === 0) {
      markHomeRefreshNeeded()
      message.success('更新成功')
      await loadAppDetail()
      return
    }
    message.error(res.data.message || '更新失败')
  } finally {
    saving.value = false
  }
}

const openPreview = () => {
  openInNewTab(previewUrl.value)
}

const goBack = () => {
  void router.push('/')
}

onMounted(() => {
  void loadAppDetail()
})
</script>

<style scoped>
.app-edit-page {
  display: grid;
  grid-template-columns: minmax(360px, 520px) minmax(0, 1fr);
  gap: 24px;
}

.app-edit-page__form-panel,
.app-edit-page__preview-panel {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(220, 230, 255, 0.9);
  border-radius: 28px;
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.06);
}

.app-edit-page__form-panel {
  padding: 24px;
}

.app-edit-page__header,
.app-edit-page__preview-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.app-edit-page__title,
.app-edit-page__preview-title {
  margin: 0;
  font-size: 1.7rem;
  font-weight: 700;
}

.app-edit-page__desc,
.app-edit-page__preview-desc,
.app-edit-page__field-tip {
  margin-top: 8px;
  color: var(--app-text-secondary);
}

.app-edit-page__preview-panel {
  display: flex;
  flex-direction: column;
  min-height: 680px;
  padding: 24px;
}

.app-edit-page__preview-body {
  display: flex;
  flex: 1;
  min-height: 0;
}

@media (max-width: 1180px) {
  .app-edit-page {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .app-edit-page__header,
  .app-edit-page__preview-header {
    flex-direction: column;
    align-items: stretch;
  }

  .app-edit-page__form-panel,
  .app-edit-page__preview-panel {
    padding: 18px;
  }
}
</style>
