<template>
  <div class="app-chat-page">
    <header class="app-chat-page__header">
      <div class="app-chat-page__header-left">
        <a-button class="app-chat-page__back" size="large" type="text" @click="goHome">
          <template #icon>
            <LeftOutlined />
          </template>
        </a-button>
        <h1 class="app-chat-page__title">{{ appName }}</h1>
      </div>

      <div class="app-chat-page__header-right">
        <a-button v-if="deployUrl" size="large" @click="openDeployUrl">访问已部署地址</a-button>
        <a-button
          :disabled="!canDeployApp"
          :loading="deploying"
          size="large"
          type="primary"
          @click="handleDeploy"
        >
          {{ hasDeployedApp ? '重新部署' : '部署' }}
        </a-button>
      </div>
    </header>

    <main class="app-chat-page__content">
      <section class="app-chat-page__panel app-chat-page__panel--chat">
        <div ref="messageContainerRef" class="app-chat-page__messages">
          <div
            v-for="item in messages"
            :key="item.id"
            :class="[
              'chat-message',
              item.role === 'user' ? 'chat-message--user' : 'chat-message--assistant',
            ]"
          >
            <a-avatar
              v-if="item.role === 'assistant'"
              :size="36"
              :src="aiAssistantAvatar"
              class="chat-message__avatar"
            />

            <div class="chat-message__bubble">
              <div class="chat-message__content">
                <MarkdownContent
                  v-if="item.role === 'assistant' && item.content"
                  :content="item.content"
                />
                <template v-else>
                  {{ item.content || (item.status === 'streaming' ? '正在生成中...' : '') }}
                </template>
              </div>
            </div>

            <a-avatar
              v-if="item.role === 'user'"
              :size="36"
              :src="loginUserStore.loginUser.userAvatar"
              class="chat-message__avatar"
            >
              {{ (loginUserStore.loginUser.userName || 'U').slice(0, 1) }}
            </a-avatar>
          </div>

          <a-empty
            v-if="!messages.length"
            class="app-chat-page__empty"
            description="创建应用后，实时生成内容会显示在这里。"
          />
        </div>

        <div class="app-chat-page__composer">
          <div class="app-chat-page__composer-tools">
            <a-space wrap>
              <a-button :disabled="!canChatOnApp" @click="fillOptimizePrompt">
                优化当前应用
              </a-button>
              <a-button disabled>上传素材（待开放）</a-button>
            </a-space>
          </div>

          <a-tooltip :title="!canChatOnApp ? chatBlockedReason : null">
            <div class="app-chat-page__textarea-wrap">
              <a-textarea
                v-model:value="inputMessage"
                :auto-size="{ minRows: 4, maxRows: 7 }"
                :disabled="!canChatOnApp"
                :maxlength="2000"
                placeholder="描述越详细，页面越具体。比如：请把首页改成深色科技风，并补充产品优势区块。"
                @press-enter="handleTextareaEnter"
              />
            </div>
          </a-tooltip>

          <div class="app-chat-page__composer-footer">
            <span class="app-chat-page__composer-hint">支持连续对话优化同一个应用</span>
            <a-button
              :disabled="!canChatOnApp || !appDetail.id"
              :loading="isStreaming"
              size="large"
              type="primary"
              @click="sendCurrentMessage"
            >
              发送
            </a-button>
          </div>
        </div>
      </section>

      <section class="app-chat-page__panel app-chat-page__panel--preview">
        <div class="preview-panel__header">
          <div>
            <h2 class="preview-panel__title">网页预览</h2>
          </div>
          <a-space v-if="showPreview && previewUrl">
            <a-button @click="openPreviewUrl">新窗口打开</a-button>
            <a-button v-if="canEditAppInfo" type="link" @click="openEditPage">
              编辑应用信息
            </a-button>
          </a-space>
        </div>

        <div class="preview-panel__body">
          <AppPreviewFrame
            :iframe-key="previewFrameKey"
            :loading="previewLoading"
            :preview-url="previewUrl"
            :show-preview="showPreview"
            empty-description="代码生成完成并且静态资源可访问后，这里才会显示网页效果。"
            iframe-title="应用预览"
            loading-text="代码已经生成完成，正在加载右侧静态资源..."
          />
        </div>
      </section>
    </main>

    <a-modal
      v-model:open="deploySuccessModalOpen"
      :footer="null"
      centered
      title="部署成功"
      width="520px"
    >
      <div class="deploy-success-modal">
        <CheckCircleFilled class="deploy-success-modal__icon" />
        <h3 class="deploy-success-modal__headline">网站部署成功!</h3>
        <p class="deploy-success-modal__desc">
          你的网站已经成功部署，可以通过以下链接访问:
        </p>

        <div class="deploy-success-modal__url-box">
          <span class="deploy-success-modal__url-text">{{ deployUrl }}</span>
          <a-button
            :disabled="!deployUrl"
            class="deploy-success-modal__copy-btn"
            type="text"
            @click="copyDeployUrl"
          >
            <template #icon>
              <CopyOutlined />
            </template>
          </a-button>
        </div>

        <div class="deploy-success-modal__actions">
          <a-button type="primary" @click="openDeployUrl">访问网站</a-button>
          <a-button @click="deploySuccessModalOpen = false">关闭</a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { CheckCircleFilled, CopyOutlined, LeftOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'

import AppPreviewFrame from '@/components/app/AppPreviewFrame.vue'
import MarkdownContent from '@/components/chat/MarkdownContent.vue'
import { useRouteAppId } from '@/composables/useRouteAppId'
import { deployApp, getAppVoById } from '@/api/appController'
import aiAssistantAvatar from '@/assets/img_1.png'
import { useLoginUserStore } from '@/stores/loginUser'
import { streamChatToGenCode } from '@/utils/chatStream'
import {
  getAppDisplayName,
  getAppIdString,
  resolveAppDeployUrl,
  resolveAppPreviewUrl,
  toApiRequestId,
  type AppIdentifier,
} from '@/utils/app'
import { openInNewTab } from '@/utils/browser'
import { markHomeRefreshNeeded } from '@/utils/homeRefresh'
import 'highlight.js/styles/github.css'

type ChatMessage = {
  id: string
  role: 'user' | 'assistant'
  content: string
  status?: 'streaming' | 'done' | 'error'
}

type StoredChatPageState = {
  messages: ChatMessage[]
  showPreview: boolean
  deployUrl: string
}

const route = useRoute()
const router = useRouter()
const appId = useRouteAppId()
const loginUserStore = useLoginUserStore()

const appDetail = reactive<API.AppVO>({})
const messages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const isStreaming = ref(false)
const deploying = ref(false)
const deployUrl = ref('')
const deploySuccessModalOpen = ref(false)
const showPreview = ref(false)
const previewLoading = ref(false)
const previewFrameKey = ref(0)
const messageContainerRef = ref<HTMLElement>()
const currentAbortController = ref<AbortController>()

const appName = computed(() => getAppDisplayName(appDetail))
const previewUrl = computed(() => resolveAppPreviewUrl(appDetail))
const isOwnApp = computed(() => {
  if (!loginUserStore.loginUser.id || appDetail.userId === undefined || appDetail.userId === null) {
    return false
  }

  return String(loginUserStore.loginUser.id) === String(appDetail.userId)
})
const canEditAppInfo = computed(() => {
  return isOwnApp.value || loginUserStore.loginUser.userRole === 'admin'
})
const canChatOnApp = computed(() => isOwnApp.value)
const canDeployApp = computed(() => Boolean(appDetail.id) && isOwnApp.value)
const hasDeployedApp = computed(() => Boolean(appDetail.deployKey?.trim()))
const chatBlockedReason = '无法在别人的作品下对话哦~'

const getAutoPromptStorageKey = (id: AppIdentifier) => `app:autoPrompt:${getAppIdString(id)}`
const getChatStateStorageKey = (id: AppIdentifier) => `app:chatState:${getAppIdString(id)}`

const normalizeMessagesForStorage = (source: ChatMessage[]) => {
  return source.map((item) => ({
    ...item,
    status: item.status === 'streaming' ? 'done' : item.status,
  }))
}

const persistChatPageState = () => {
  if (!appId.value) {
    return
  }

  const payload: StoredChatPageState = {
    messages: normalizeMessagesForStorage(messages.value),
    showPreview: showPreview.value,
    deployUrl: deployUrl.value,
  }

  sessionStorage.setItem(getChatStateStorageKey(appId.value), JSON.stringify(payload))
}

const restoreChatPageState = () => {
  if (!appId.value) {
    return
  }

  const rawState = sessionStorage.getItem(getChatStateStorageKey(appId.value))
  if (!rawState) {
    return
  }

  try {
    const parsedState = JSON.parse(rawState) as Partial<StoredChatPageState>
    messages.value = Array.isArray(parsedState.messages)
      ? normalizeMessagesForStorage(parsedState.messages as ChatMessage[])
      : []
    showPreview.value = Boolean(parsedState.showPreview)
    deployUrl.value = parsedState.deployUrl || ''
    previewFrameKey.value = showPreview.value ? 1 : 0
  } catch {
    sessionStorage.removeItem(getChatStateStorageKey(appId.value))
  }
}

const scrollMessagesToBottom = async () => {
  await nextTick()
  const container = messageContainerRef.value
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

const loadAppDetail = async () => {
  if (!appId.value) {
    message.error('应用 id 无效')
    await router.replace('/')
    return false
  }

  const res = await getAppVoById({ id: toApiRequestId(appId.value) })
  if (res.data.code === 0 && res.data.data) {
    Object.assign(appDetail, res.data.data)
    deployUrl.value = resolveAppDeployUrl(appDetail) || deployUrl.value
    return true
  }

  message.error(res.data.message || '获取应用详情失败')
  await router.replace('/')
  return false
}

const appendMessage = (messageItem: ChatMessage) => {
  messages.value.push(messageItem)
  persistChatPageState()
  void scrollMessagesToBottom()
}

const replaceAssistantContent = (
  messageId: string,
  content: string,
  status: ChatMessage['status'],
) => {
  const targetMessage = messages.value.find((item) => item.id === messageId)
  if (!targetMessage) {
    return
  }

  targetMessage.content = content
  targetMessage.status = status
  persistChatPageState()
  void scrollMessagesToBottom()
}

const probeStaticPreview = async (url: string) => {
  const maxAttempts = 8

  for (let attempt = 0; attempt < maxAttempts; attempt += 1) {
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include',
      cache: 'no-store',
    })

    if (response.ok) {
      return true
    }

    await new Promise((resolve) => {
      globalThis.setTimeout(resolve, 600)
    })
  }

  return false
}

const loadPreviewAfterDone = async () => {
  await loadAppDetail()
  const url = previewUrl.value
  if (!url) {
    previewLoading.value = false
    message.warning('已收到 done 事件，但未找到静态资源地址')
    return
  }

  const available = await probeStaticPreview(url)
  previewLoading.value = false

  if (!available) {
    showPreview.value = false
    persistChatPageState()
    message.warning('代码生成已完成，但静态资源暂时不可访问，请稍后重试')
    return
  }

  showPreview.value = true
  previewFrameKey.value += 1
  persistChatPageState()
}

const runChat = async (content: string) => {
  if (!appId.value || !content.trim() || isStreaming.value || !canChatOnApp.value) {
    return
  }

  appendMessage({
    id: `user-${Date.now()}`,
    role: 'user',
    content: content.trim(),
    status: 'done',
  })

  const assistantMessageId = `assistant-${Date.now()}`
  appendMessage({
    id: assistantMessageId,
    role: 'assistant',
    content: '',
    status: 'streaming',
  })

  isStreaming.value = true
  showPreview.value = false
  previewLoading.value = false
  previewFrameKey.value += 1
  persistChatPageState()

  const abortController = new AbortController()
  currentAbortController.value = abortController
  let assistantContent = ''
  let receivedDone = false

  try {
    await streamChatToGenCode(
      appId.value,
      content.trim(),
      {
        onChunk(chunk) {
          assistantContent += chunk
          replaceAssistantContent(assistantMessageId, assistantContent, 'streaming')
        },
        onDone() {
          receivedDone = true
          previewLoading.value = true
        },
      },
      abortController.signal,
    )

    replaceAssistantContent(assistantMessageId, assistantContent || '生成完成。', 'done')

    if (receivedDone) {
      await loadPreviewAfterDone()
    }
  } catch (error) {
    previewLoading.value = false
    const errorMessage =
      error instanceof Error && error.name === 'AbortError'
        ? '本次生成已取消'
        : error instanceof Error
          ? error.message
          : '生成失败，请稍后再试'

    replaceAssistantContent(assistantMessageId, assistantContent || errorMessage, 'error')
    if (errorMessage !== '本次生成已取消') {
      message.error(errorMessage)
    }
  } finally {
    currentAbortController.value = undefined
    isStreaming.value = false
  }
}

const sendCurrentMessage = async () => {
  if (!canChatOnApp.value) {
    message.warning(chatBlockedReason)
    return
  }

  const content = inputMessage.value.trim()
  if (!content) {
    message.warning('请输入要生成或优化的内容')
    return
  }

  inputMessage.value = ''
  await runChat(content)
}

const handleTextareaEnter = (event: KeyboardEvent) => {
  if (event.shiftKey || !canChatOnApp.value) {
    return
  }
  event.preventDefault()
  void sendCurrentMessage()
}

const handleDeploy = async () => {
  if (!appDetail.id || !canDeployApp.value) {
    return
  }

  deploying.value = true
  try {
    const res = await deployApp({ appId: toApiRequestId(appDetail.id as AppIdentifier) })
    if (res.data.code === 0 && res.data.data) {
      deployUrl.value = res.data.data
      await loadAppDetail()
      persistChatPageState()
      markHomeRefreshNeeded()
      deploySuccessModalOpen.value = true
      return
    }
    message.error(res.data.message || '部署失败')
  } finally {
    deploying.value = false
  }
}

const openPreviewUrl = () => {
  openInNewTab(previewUrl.value)
}

const openDeployUrl = () => {
  openInNewTab(deployUrl.value)
}

const copyDeployUrl = async () => {
  if (!deployUrl.value) {
    return
  }

  try {
    await navigator.clipboard.writeText(deployUrl.value)
    message.success('部署网址已复制')
  } catch {
    message.error('复制失败，请手动复制')
  }
}

const openEditPage = async () => {
  if (!appDetail.id || !canEditAppInfo.value) {
    return
  }
  await router.push(`/app/edit/${appDetail.id}`)
}

const goHome = async () => {
  await router.push('/')
}

const fillOptimizePrompt = () => {
  if (!canChatOnApp.value) {
    return
  }
  inputMessage.value = '请继续优化当前应用的视觉层次、排版细节和交互体验。'
}

const handleInitialPrompt = async () => {
  if (String(route.query.view) === '1') {
    if (previewUrl.value) {
      showPreview.value = true
      previewFrameKey.value += 1
      persistChatPageState()
    }
    return
  }

  if (String(route.query.autoPrompt) !== '1' || !appId.value) {
    return
  }

  const storageKey = getAutoPromptStorageKey(appId.value)
  const storedPrompt = sessionStorage.getItem(storageKey)?.trim()
  const initialPrompt = storedPrompt || appDetail.initPrompt?.trim() || ''

  sessionStorage.removeItem(storageKey)

  const nextQuery = { ...route.query }
  delete nextQuery.autoPrompt
  await router.replace({ query: nextQuery })

  if (!initialPrompt) {
    return
  }

  await runChat(initialPrompt)
}

onMounted(async () => {
  restoreChatPageState()
  const loaded = await loadAppDetail()
  if (loaded) {
    if (showPreview.value && previewUrl.value) {
      previewFrameKey.value += 1
    }
    await handleInitialPrompt()
    persistChatPageState()
  }
})

onBeforeUnmount(() => {
  currentAbortController.value?.abort()
  persistChatPageState()
})
</script>

<style scoped>
.app-chat-page {
  box-sizing: border-box;
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 0;
  height: 100%;
  max-height: 100%;
  min-height: 0;
  padding: 0;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(114, 255, 227, 0.2), transparent 28%),
    radial-gradient(circle at right top, rgba(118, 149, 255, 0.18), transparent 24%),
    #f7fbff;
}

.app-chat-page__header {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 22px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(220, 230, 255, 0.9);
  border-radius: 0;
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.06);
}

.app-chat-page__header-left,
.app-chat-page__header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.app-chat-page__back {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  color: var(--app-text);
  border-radius: 999px;
  background: rgba(241, 245, 249, 0.9);
}

.app-chat-page__title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
}

.app-chat-page__content {
  box-sizing: border-box;
  display: grid;
  flex: 1;
  grid-template-columns: minmax(340px, 0.95fr) minmax(420px, 1.35fr);
  gap: 2px;
  height: 0;
  max-height: 100%;
  min-height: 0;
  padding: 0 0;
  overflow: hidden;
}

.app-chat-page__panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(220, 230, 255, 0.9);
  border-radius: 0;
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.06);
}

.app-chat-page__messages {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 18px;
  min-height: 0;
  padding: 22px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.app-chat-page__empty {
  margin: auto 0;
}

.chat-message {
  display: flex;
  gap: 12px;
}

.chat-message--user {
  justify-content: flex-end;
}

.chat-message__avatar {
  flex-shrink: 0;
}

.chat-message__bubble {
  max-width: min(86%, 640px);
  padding: 10px 18px;
  word-break: break-word;
  border-radius: 20px;
}

.chat-message--assistant .chat-message__bubble {
  background: #f5f8ff;
  border: 1px solid #dce7ff;
}

.chat-message--user .chat-message__bubble {
  color: #ffffff;
  background: linear-gradient(135deg, #1f7aff, #14b8a6);
}

.chat-message__content {
  font-size: 1rem;
  line-height: 1.8;
}

.chat-message--user .chat-message__content {
  white-space: pre-wrap;
}

.chat-message--assistant .chat-message__content {
  white-space: normal;
}

.app-chat-page__composer {
  flex-shrink: 0;
  padding: 20px 22px 22px;
  border-top: 1px solid rgba(220, 230, 255, 0.9);
}

.app-chat-page__composer-tools {
  margin-bottom: 14px;
}

.app-chat-page__textarea-wrap {
  width: 100%;
}

.app-chat-page__composer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 14px;
}

.app-chat-page__composer-hint {
  color: var(--app-text-secondary);
  font-size: 0.92rem;
}

.preview-panel__header {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 24px 16px;
}

.preview-panel__title {
  margin: 0;
  font-size: 1.3rem;
  font-weight: 700;
}

.preview-panel__body {
  display: flex;
  flex: 1;
  min-height: 0;
  padding: 0 20px 20px;
  overflow: hidden;
}

.deploy-success-modal {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 4px 0;
  text-align: center;
}

.deploy-success-modal__icon {
  margin-top: 8px;
  color: #52c41a;
  font-size: 48px;
}

.deploy-success-modal__headline {
  margin: 18px 0 10px;
  color: #1f2937;
  font-size: 1.5rem;
  font-weight: 700;
}

.deploy-success-modal__desc {
  margin: 0 0 20px;
  color: #6b7280;
  line-height: 1.7;
}

.deploy-success-modal__url-box {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: 48px;
  padding: 0 10px 0 14px;
  background: #ffffff;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
}

.deploy-success-modal__url-text {
  flex: 1;
  overflow: hidden;
  color: #1f2937;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.deploy-success-modal__copy-btn {
  flex-shrink: 0;
}

.deploy-success-modal__actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 22px;
}

@media (max-width: 1280px) {
  .app-chat-page__content {
    grid-template-columns: 1fr;
    grid-template-rows: minmax(0, 1fr) minmax(0, 1fr);
  }
}

@media (max-width: 768px) {
  .app-chat-page {
    padding: 0;
  }

  .app-chat-page__header,
  .app-chat-page__header-left,
  .app-chat-page__header-right,
  .preview-panel__header,
  .app-chat-page__composer-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .app-chat-page__messages,
  .app-chat-page__composer,
  .preview-panel__header,
  .preview-panel__body {
    padding-right: 16px;
    padding-left: 16px;
  }

  .preview-panel__body {
    padding-bottom: 16px;
  }

  .chat-message__bubble {
    max-width: 100%;
  }

  .deploy-success-modal__actions {
    width: 100%;
    flex-direction: column;
  }
}
</style>
