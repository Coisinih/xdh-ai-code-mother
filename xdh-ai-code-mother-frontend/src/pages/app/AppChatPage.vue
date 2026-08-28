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
        <a-tag v-if="appDetail?.codeGenType" color="blue" class="code-gen-type-tag">
          {{ getCodeGenTypeText(appDetail.codeGenType) }}
        </a-tag>
      </div>

      <div class="app-chat-page__header-right">
        <a-button
          :disabled="!appDetail.id"
          class="app-chat-page__detail-trigger"
          size="large"
          @click="openAppDetailModal"
        >
          <template #icon>
            <InfoCircleOutlined />
          </template>
          应用详情
        </a-button>
        <a-button v-if="deployUrl" size="large" @click="openDeployUrl">访问已部署地址</a-button>
        <a-button
          :disabled="!canDeployApp"
          class="app-chat-page__download-btn"
          :loading="downloading"
          size="large"
          @click="handleDownloadCode"
        >
          <template #icon>
            <DownloadOutlined />
          </template>
          下载代码
        </a-button>
        <a-button
          :disabled="!canDeployApp"
          class="app-chat-page__deploy-btn"
          :loading="deploying"
          size="large"
          type="primary"
          @click="handleDeploy"
        >
          <template #icon>
            <RocketOutlined />
          </template>
          {{ hasDeployedApp ? '重新部署' : '部署' }}
        </a-button>
      </div>
    </header>

    <main class="app-chat-page__content">
      <section class="app-chat-page__panel app-chat-page__panel--chat">
        <div ref="messageContainerRef" class="app-chat-page__messages">
          <div class="app-chat-page__load-more">
            <a-button
              v-if="historyHasMore"
              :loading="loadingMoreHistory"
              :disabled="historyLoading || loadingMoreHistory"
              type="link"
              @click="loadMoreHistory"
            >
              加载更多
            </a-button>
          </div>

          <a-spin :spinning="historyLoading && !messages.length">
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
          </a-spin>

          <a-empty
            v-if="!historyLoading && !messages.length"
            class="app-chat-page__empty"
            description="创建应用后，实时生成内容会显示在这里。"
          />
        </div>

        <div class="app-chat-page__composer">
          <div class="app-chat-page__composer-tools">
            <a-space wrap>
              <a-button :disabled="!canChatOnApp" @click="fillOptimizePrompt"
                >优化当前应用</a-button
              >
              <a-button disabled>上传素材（待开放）</a-button>
            </a-space>
          </div>

          <a-alert
            v-if="selectedElement"
            class="app-chat-page__selected-element-alert"
            closable
            message="已选中页面元素"
            type="info"
            @close="clearSelectedVisualElement"
          >
            <template #description>
              <div class="app-chat-page__selected-element-alert-desc">
                {{ selectedElementSummary }}
              </div>
            </template>
          </a-alert>

          <a-tooltip :title="!canChatOnApp ? chatBlockedReason : null">
            <div class="app-chat-page__textarea-wrap">
              <a-textarea
                v-model:value="inputMessage"
                :auto-size="{ minRows: 4, maxRows: 7 }"
                :disabled="!canChatOnApp"
                :maxlength="2000"
                placeholder="描述越详细，页面越具体。例如：请把首页改成深色科技风，并补充产品优势区块。"
                @press-enter="handleTextareaEnter"
              />
              <a-button
                aria-label="发送消息"
                class="app-chat-page__send-button"
                :disabled="!canChatOnApp || !appDetail.id"
                :loading="isStreaming"
                type="primary"
                @click="sendCurrentMessage"
              >
                <template #icon>
                  <SendOutlined />
                </template>
              </a-button>
            </div>
          </a-tooltip>
        </div>
      </section>

      <section class="app-chat-page__panel app-chat-page__panel--preview">
        <div class="preview-panel__header">
          <div>
            <h2 class="preview-panel__title">网站预览</h2>
          </div>
          <a-space v-if="showPreview && previewUrl">
            <a-button
              class="preview-panel__edit-button"
              :disabled="!canChatOnApp"
              type="link"
              :class="{ 'preview-panel__edit-button--active': isVisualEditing }"
              @click="toggleVisualEditing"
            >
              <template #icon>
                <EditOutlined />
              </template>
              {{ isVisualEditing ? '退出编辑' : '可视化编辑' }}
            </a-button>
            <a-button class="preview-panel__link-button" type="link" @click="openPreviewUrl">
              <template #icon>
                <ExportOutlined />
              </template>
              新窗口打开
            </a-button>
          </a-space>
        </div>

        <div class="preview-panel__body">
          <div v-if="isStreaming || previewLoading" class="preview-panel__loading">
            <a-spin size="large" />
            <p>
              {{
                isStreaming ? '正在生成代码，请稍候...' : '代码已经生成完成，正在加载静态资源...'
              }}
            </p>
          </div>
          <div v-else-if="previewLoadFailed" class="preview-panel__load-error">
            <p>静态资源加载失败，请刷新重试或重新生成</p>
            <a-button type="link" @click="retryPreview">
              <template #icon>
                <ReloadOutlined />
              </template>
              刷新资源
            </a-button>
          </div>
          <iframe
            v-else-if="showPreview && previewUrl"
            ref="previewIframeRef"
            :key="previewFrameKey"
            :src="previewUrl"
            class="preview-panel__iframe"
            title="应用预览"
            @load="onIframeLoad"
            @error="onIframeError"
          />
          <a-empty v-else description="对话生成完成后，这里会展示对应的网站效果。" />
        </div>
      </section>
    </main>

    <a-modal
      v-model:open="appDetailModalOpen"
      :footer="null"
      centered
      title="应用详情"
      width="480px"
    >
      <a-descriptions :column="1" class="app-chat-page__detail-descriptions" size="small">
        <a-descriptions-item label="应用名称">{{ appName }}</a-descriptions-item>
        <a-descriptions-item label="更新时间">{{ appUpdatedAt }}</a-descriptions-item>
        <a-descriptions-item label="应用类型">
          <a-tag color="blue" class="code-gen-type-tag">
            {{ appTypeText }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <div class="app-chat-page__detail-actions">
        <a-button type="primary" @click="openEditPageFromModal">
          <template #icon>
            <EditOutlined />
          </template>
          编辑
        </a-button>
        <a-button @click="appDetailModalOpen = false">关闭</a-button>
      </div>
    </a-modal>

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
        <p class="deploy-success-modal__desc">你的网站已经成功部署，可以通过以下链接访问:</p>

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
import {
  CheckCircleFilled,
  CopyOutlined,
  DownloadOutlined,
  EditOutlined,
  ExportOutlined,
  InfoCircleOutlined,
  LeftOutlined,
  ReloadOutlined,
  RocketOutlined,
  SendOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'

import MarkdownContent from '@/components/chat/MarkdownContent.vue'
import { useRouteAppId } from '@/composables/useRouteAppId'
import { deployApp, downloadAppCode, getAppVoById } from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import aiAssistantAvatar from '@/assets/img_1.png'
import { useLoginUserStore } from '@/stores/loginUser'
import { streamChatToGenCode } from '@/utils/chatStream'
import {
  formatDateTime,
  getAppDisplayName,
  resolveAppDeployUrl,
  resolveAppPreviewUrl,
  toApiRequestId,
} from '@/utils/app'
import { openInNewTab } from '@/utils/browser'
import { markHomeRefreshNeeded } from '@/utils/homeRefresh'
import { type ElementInfo, VisualEditor } from '@/utils/visualEditor'
import 'highlight.js/styles/github.css'
import { getCodeGenTypeText } from '@/constants/codeGenType.ts'

type ChatRole = 'user' | 'assistant'

type ChatMessage = {
  id: string
  role: ChatRole
  content: string
  status?: 'streaming' | 'done' | 'error'
  createTime?: string
}

const HISTORY_PAGE_SIZE = 10

const route = useRoute()
const router = useRouter()
const appId = useRouteAppId()
const loginUserStore = useLoginUserStore()

const appDetail = reactive<API.AppVO>({})
const messages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const isStreaming = ref(false)
const deploying = ref(false)
const downloading = ref(false)
const appDetailModalOpen = ref(false)
const deployUrl = ref('')
const deploySuccessModalOpen = ref(false)
const showPreview = ref(false)
const previewLoading = ref(false)
const previewLoadFailed = ref(false)
const previewReady = ref(false)
const previewFrameKey = ref(0)
const previewIframeRef = ref<HTMLIFrameElement>()
const selectedElement = ref<ElementInfo | null>(null)
const isVisualEditing = ref(false)
const historyLoading = ref(false)
const loadingMoreHistory = ref(false)
const historyHasMore = ref(false)
const historyTotal = ref(0)
const messageContainerRef = ref<HTMLElement>()
const currentAbortController = ref<AbortController>()
const scrollToBottomFrame = ref<number | null>(null)
const scrollToBottomPending = ref(false)
const visualEditor = new VisualEditor({
  onElementSelected(elementInfo) {
    selectedElement.value = elementInfo
  },
})

const appName = computed(() => getAppDisplayName(appDetail))
const appUpdatedAt = computed(() => formatDateTime(appDetail.updateTime))
const appTypeText = computed(() => getCodeGenTypeText(appDetail.codeGenType))
const selectedElementSummary = computed(() => {
  if (!selectedElement.value) {
    return ''
  }

  const { tagName, selector, textContent, id, className, pagePath } = selectedElement.value
  return [
    `标签：${tagName || '无'}`,
    `选择器：${selector || '无'}`,
    `文本：${textContent || '无'}`,
    `ID：${id || '无'}`,
    `类名：${className || '无'}`,
    `页面路径：${pagePath || '当前页'}`,
  ].join('\n')
})
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

const buildPromptContext = () => {
  if (!selectedElement.value) {
    return ''
  }

  const { tagName, selector, textContent, id, className, pagePath, rect } = selectedElement.value
  return [
    '[当前选中的页面元素]',
    `- 标签：${tagName || '无'}`,
    `- 选择器：${selector || '无'}`,
    `- 文本：${textContent || '无'}`,
    `- ID：${id || '无'}`,
    `- 类名：${className || '无'}`,
    `- 页面路径：${pagePath || '当前页'}`,
    `- 位置尺寸：top=${Math.round(rect.top)}, left=${Math.round(rect.left)}, width=${Math.round(rect.width)}, height=${Math.round(rect.height)}`,
    '请优先围绕这个元素及其直接相关区域进行修改。',
  ].join('\n')
}

const getMessageKey = (item: ChatMessage) => {
  return item.id
}

const getChatRole = (messageType?: string): ChatRole => {
  const normalized = messageType?.trim().toLowerCase()
  if (!normalized) {
    return 'assistant'
  }

  if (normalized.includes('user') || normalized.includes('human') || normalized === 'question') {
    return 'user'
  }

  return 'assistant'
}

const toChatMessage = (record: API.ChatHistory): ChatMessage => {
  const recordId = record.id ?? `${record.createTime || Date.now()}-${record.messageType || 'chat'}`

  return {
    id: `history-${recordId}`,
    role: getChatRole(record.messageType),
    content: record.message?.trim() || '',
    status: 'done',
    createTime: record.createTime,
  }
}

const compareByCreateTime = (left?: string, right?: string) => {
  const leftTime = left ? dayjs(left).valueOf() : 0
  const rightTime = right ? dayjs(right).valueOf() : 0
  return leftTime - rightTime
}

const sortMessagesAscending = (source: ChatMessage[]) => {
  return [...source].sort((left, right) => {
    const byTime = compareByCreateTime(left.createTime, right.createTime)
    if (byTime !== 0) {
      return byTime
    }
    return left.id.localeCompare(right.id)
  })
}

const dedupeMessages = (source: ChatMessage[]) => {
  const map = new Map<string, ChatMessage>()
  source.forEach((item) => {
    map.set(getMessageKey(item), item)
  })
  return Array.from(map.values())
}

const replaceMessages = (source: ChatMessage[]) => {
  messages.value = sortMessagesAscending(dedupeMessages(source))
}

const scrollMessagesToBottom = async () => {
  if (scrollToBottomPending.value) {
    return
  }

  scrollToBottomPending.value = true
  await nextTick()
  scrollToBottomFrame.value = window.requestAnimationFrame(() => {
    const container = messageContainerRef.value
    if (container) {
      container.scrollTo({ top: container.scrollHeight, behavior: 'auto' })
    }

    scrollToBottomFrame.value = null
    scrollToBottomPending.value = false
  })
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
    deployUrl.value = resolveAppDeployUrl(appDetail)
    return true
  }

  message.error(res.data.message || '获取应用详情失败')
  await router.replace('/')
  return false
}

const loadHistoryPage = async (cursor?: string) => {
  if (!appDetail.id) {
    return { records: [] as ChatMessage[], total: 0, pageSize: HISTORY_PAGE_SIZE }
  }

  const res = await listAppChatHistory({
    appId: toApiRequestId(appDetail.id),
    pageSize: HISTORY_PAGE_SIZE,
    ...(cursor ? { lastCreateTime: cursor } : {}),
  })

  if (res.data.code !== 0 || !res.data.data) {
    throw new Error(res.data.message || '获取对话历史失败')
  }

  const pageRecords = res.data.data.records ?? []
  return {
    records: pageRecords.map(toChatMessage),
    total: res.data.data.totalRow ?? 0,
    pageSize: res.data.data.pageSize ?? HISTORY_PAGE_SIZE,
  }
}

const loadInitialHistory = async () => {
  if (!appDetail.id) {
    return
  }

  historyLoading.value = true
  try {
    const page = await loadHistoryPage()
    replaceMessages(page.records)
    historyTotal.value = page.total || page.records.length
    historyHasMore.value =
      page.records.length === HISTORY_PAGE_SIZE && historyTotal.value > page.records.length

    const shouldShowPreview = String(route.query.view) === '1' || historyTotal.value >= 2
    showPreview.value = false
    previewLoadFailed.value = false

    if (shouldShowPreview && previewUrl.value) {
      previewLoading.value = true
      const available = await probeStaticPreview(previewUrl.value)
      previewLoading.value = false

      if (available) {
        previewReady.value = false
        showPreview.value = true
        previewFrameKey.value += 1
      } else {
        previewLoadFailed.value = true
        message.warning('静态资源加载失败，请刷新重试或重新生成')
      }
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : '获取对话历史失败')
  } finally {
    historyLoading.value = false
  }
}

const loadMoreHistory = async () => {
  if (!historyHasMore.value || loadingMoreHistory.value || historyLoading.value) {
    return
  }

  const oldestMessage = messages.value[0]
  if (!oldestMessage?.createTime) {
    historyHasMore.value = false
    return
  }

  const container = messageContainerRef.value
  const previousScrollHeight = container?.scrollHeight ?? 0
  const previousScrollTop = container?.scrollTop ?? 0

  loadingMoreHistory.value = true
  try {
    const page = await loadHistoryPage(oldestMessage.createTime)
    const merged = dedupeMessages([...page.records, ...messages.value])
    replaceMessages(merged)

    const receivedCount = page.records.length
    historyTotal.value = Math.max(historyTotal.value, page.total || messages.value.length)
    historyHasMore.value = receivedCount === HISTORY_PAGE_SIZE

    await nextTick()
    if (container) {
      container.scrollTop = container.scrollHeight - previousScrollHeight + previousScrollTop
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载更多失败')
  } finally {
    loadingMoreHistory.value = false
  }
}

const appendMessage = (messageItem: ChatMessage) => {
  messages.value.push(messageItem)
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
}

const probeStaticPreview = async (url: string) => {
  const maxAttempts = 8

  for (let attempt = 0; attempt < maxAttempts; attempt += 1) {
    try {
      const response = await fetch(url, {
        method: 'GET',
        credentials: 'include',
        cache: 'no-store',
      })

      if (response.ok) {
        return true
      }
    } catch {
      // Retry transient network failures before showing the refresh action.
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
    previewLoadFailed.value = true
    message.warning('未找到静态资源地址')
    return
  }

  const available = await probeStaticPreview(url)
  previewLoading.value = false

  if (!available) {
    showPreview.value = false
    previewLoadFailed.value = true
    message.warning('已生成完成，但静态资源暂时不可访问，请稍后重试')
    return
  }

  previewLoadFailed.value = false
  showPreview.value = true
  previewReady.value = false
  previewFrameKey.value += 1
}

const retryPreview = async () => {
  if (previewLoading.value) {
    return
  }

  previewLoading.value = true
  previewLoadFailed.value = false
  showPreview.value = false
  previewReady.value = false

  try {
    const loaded = await loadAppDetail()
    const url = loaded ? previewUrl.value : ''
    const available = url ? await probeStaticPreview(url) : false

    if (!available) {
      previewLoadFailed.value = true
      message.warning('静态资源仍不可用，请稍后再试')
      return
    }

    showPreview.value = true
    previewFrameKey.value += 1
  } catch (error) {
    previewLoadFailed.value = true
    message.warning(error instanceof Error ? error.message : '静态资源获取失败，请刷新重试')
  } finally {
    previewLoading.value = false
  }
}

const onIframeError = () => {
  previewLoading.value = false
  previewReady.value = false
  showPreview.value = false
  previewLoadFailed.value = true
  message.warning('静态资源加载失败，请刷新重试或重新生成')
}

const runChat = async (displayContent: string, requestContent = displayContent) => {
  const normalizedDisplayContent = displayContent.trim()
  const normalizedRequestContent = requestContent.trim()

  if (
    !appId.value ||
    !normalizedDisplayContent ||
    !normalizedRequestContent ||
    isStreaming.value ||
    !canChatOnApp.value
  ) {
    return
  }

  visualEditor.disableEditMode()
  visualEditor.clearSelection()
  isVisualEditing.value = false
  selectedElement.value = null
  appendMessage({
    id: `user-${Date.now()}`,
    role: 'user',
    content: normalizedDisplayContent,
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
  previewReady.value = false
  previewLoading.value = false
  previewLoadFailed.value = false
  previewFrameKey.value += 1

  const abortController = new AbortController()
  currentAbortController.value = abortController
  let assistantContent = ''
  let receivedDone = false
  let receivedBusinessError = false

  try {
    await streamChatToGenCode(
      appId.value,
      normalizedRequestContent,
      {
        onChunk(chunk) {
          if (receivedBusinessError) {
            return
          }

          assistantContent += chunk
          replaceAssistantContent(assistantMessageId, assistantContent, 'streaming')
          void scrollMessagesToBottom()
        },
        onDone() {
          if (receivedBusinessError) {
            return
          }

          receivedDone = true
          previewLoading.value = true
        },
        onBusinessError(data) {
          if (receivedDone || receivedBusinessError) {
            return
          }

          receivedBusinessError = true
          previewLoading.value = false

          let errorMessage = '生成过程中出现错误'

          try {
            const errorData = JSON.parse(data) as { message?: string }
            if (typeof errorData.message === 'string' && errorData.message.trim()) {
              errorMessage = errorData.message.trim()
            }
          } catch (parseError) {
            console.error('解析业务错误事件失败:', parseError, '原始数据:', data)
          }

          replaceAssistantContent(assistantMessageId, `❌ ${errorMessage}`, 'error')
          message.error(errorMessage)
        },
      },
      abortController.signal,
    )

    if (receivedBusinessError) {
      return
    }

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

  const selectedPromptContext = buildPromptContext()
  const requestContent = selectedPromptContext ? `${content}\n\n${selectedPromptContext}` : content

  inputMessage.value = ''
  await runChat(content, requestContent)
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
    const res = await deployApp({ appId: toApiRequestId(appDetail.id) })
    if (res.data.code === 0 && res.data.data) {
      deployUrl.value = res.data.data
      await loadAppDetail()
      markHomeRefreshNeeded()
      deploySuccessModalOpen.value = true
      return
    }
    message.error(res.data.message || '部署失败')
  } finally {
    deploying.value = false
  }
}

const getResponseHeader = (headers: unknown, name: string) => {
  if (!headers || typeof headers !== 'object') {
    return ''
  }

  const record = headers as Record<string, unknown>
  const value = record[name] ?? record[name.toLowerCase()]
  return typeof value === 'string' ? value : ''
}

const parseDownloadFileName = (contentDisposition?: string) => {
  if (!contentDisposition) {
    return ''
  }

  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1])
  }

  const filenameMatch = contentDisposition.match(/filename="?([^";]+)"?/i)
  return filenameMatch?.[1]?.trim() || ''
}

const triggerDownload = (blob: Blob, filename: string) => {
  const objectUrl = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = objectUrl
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(objectUrl)
}

const handleDownloadCode = async () => {
  if (!appDetail.id || !canDeployApp.value) {
    return
  }

  downloading.value = true
  try {
    const res = await downloadAppCode(
      { appId: toApiRequestId(appDetail.id) },
      { responseType: 'blob' },
    )

    const blob = res.data as Blob
    // 后端异常时返回的是 JSON，而不是 zip 文件，需要解析并提示错误
    if (blob.type?.includes('application/json')) {
      const errorData = JSON.parse(await blob.text()) as { message?: string }
      message.error(errorData.message || '下载失败，请稍后再试')
      return
    }

    const contentDisposition = getResponseHeader(res.headers, 'content-disposition')
    const filename = parseDownloadFileName(contentDisposition) || `${appName.value}.zip`

    triggerDownload(blob, filename)
  } catch (error) {
    message.error(error instanceof Error ? error.message : '下载失败，请稍后再试')
  } finally {
    downloading.value = false
  }
}

const openPreviewUrl = () => {
  openInNewTab(previewUrl.value)
}

const onIframeLoad = () => {
  const iframe = previewIframeRef.value
  if (!iframe) {
    return
  }

  previewLoadFailed.value = false
  previewReady.value = true
  visualEditor.init(iframe)
  visualEditor.onIframeLoad()
}

const toggleVisualEditing = () => {
  const iframe = previewIframeRef.value
  if (!iframe || !previewReady.value) {
    message.warning('请等待页面加载完成')
    return
  }

  visualEditor.init(iframe)
  isVisualEditing.value = visualEditor.toggleEditMode()
}

const clearSelectedVisualElement = () => {
  selectedElement.value = null
  visualEditor.clearSelection()
}

const openAppDetailModal = () => {
  appDetailModalOpen.value = true
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

const openEditPageFromModal = async () => {
  appDetailModalOpen.value = false
  await openEditPage()
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

const getAutoPromptStorageKey = (id: string | number) => `app:autoPrompt:${id}`

const handleInitialPrompt = async () => {
  if (!isOwnApp.value || messages.value.length > 0) {
    return
  }

  const initialPrompt =
    sessionStorage.getItem(appId.value ? getAutoPromptStorageKey(appId.value) : '')?.trim() ||
    appDetail.initPrompt?.trim() ||
    ''

  if (appId.value) {
    sessionStorage.removeItem(getAutoPromptStorageKey(appId.value))
  }

  const nextQuery = { ...route.query }
  delete nextQuery.autoPrompt
  if (Object.keys(nextQuery).length !== Object.keys(route.query).length) {
    await router.replace({ query: nextQuery })
  }

  if (!initialPrompt) {
    return
  }

  await runChat(initialPrompt)
}

onMounted(async () => {
  window.addEventListener('message', visualEditor.handleIframeMessage)

  const loaded = await loadAppDetail()
  if (!loaded) {
    return
  }

  await loadInitialHistory()
  await scrollMessagesToBottom()
  await handleInitialPrompt()
})

onBeforeUnmount(() => {
  window.removeEventListener('message', visualEditor.handleIframeMessage)
  visualEditor.destroy()

  if (scrollToBottomFrame.value !== null) {
    window.cancelAnimationFrame(scrollToBottomFrame.value)
  }
  currentAbortController.value?.abort()
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
    radial-gradient(circle at right top, rgba(118, 149, 255, 0.18), transparent 24%), #f7fbff;
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
  padding: 0;
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
  gap: 1px;
  min-height: 0;
  padding: 22px;
  overflow-y: auto;
  scroll-behavior: auto;
}

.app-chat-page__load-more {
  display: flex;
  justify-content: center;
}

.app-chat-page__empty {
  margin: auto 0;
}

.chat-message {
  display: flex;
  gap: 12px;
}

.chat-message + .chat-message {
  margin-top: 8px;
}

.chat-message--user {
  justify-content: flex-end;
}

.chat-message__avatar {
  flex-shrink: 0;
}

.chat-message__bubble {
  max-width: min(86%, 640px);
  padding: 6px 14px;
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
  line-height: 1.6;
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

.app-chat-page__selected-element-alert {
  margin-bottom: 14px;
}

.app-chat-page__selected-element-alert-desc {
  color: var(--app-text);
  line-height: 1.7;
  white-space: pre-line;
}

.app-chat-page__textarea-wrap {
  position: relative;
  width: 100%;
}

.app-chat-page__textarea-wrap :deep(.ant-input) {
  padding-right: 52px;
  padding-bottom: 48px;
}

.app-chat-page__send-button {
  position: absolute;
  right: 10px;
  bottom: 10px;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
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

.preview-panel__loading {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  min-height: 320px;
  color: var(--app-text-secondary);
}

.preview-panel__load-error {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 320px;
  color: var(--app-text-secondary);
}

.preview-panel__load-error p {
  margin: 0;
}

.preview-panel__iframe {
  display: block;
  flex: 1;
  width: 100%;
  height: 100%;
  min-height: 0;
  background: #ffffff;
  border: 1px solid rgba(220, 230, 255, 0.9);
  border-radius: 22px;
}

.preview-panel__body :deep(.ant-empty) {
  margin: auto;
}

.preview-panel__link-button {
  padding-left: 0;
}

.preview-panel__edit-button,
.preview-panel__link-button {
  padding: 0;
  color: #1677ff;
}

.preview-panel__edit-button:hover,
.preview-panel__edit-button:focus,
.preview-panel__link-button:hover,
.preview-panel__link-button:focus {
  color: #4096ff;
}

.preview-panel__edit-button--active,
.preview-panel__edit-button--active:hover,
.preview-panel__edit-button--active:focus {
  color: #ff4d4f;
}

.app-chat-page__download-btn {
  color: #1677ff;
  border-color: #1677ff;
  background: #ffffff;
}

.app-chat-page__download-btn:hover,
.app-chat-page__download-btn:focus {
  color: #4096ff;
  border-color: #4096ff;
  background: #ffffff;
}

.app-chat-page__detail-descriptions :deep(.ant-descriptions-view) {
  background: transparent;
  border: 0;
}

.app-chat-page__detail-descriptions :deep(.ant-descriptions-row > th),
.app-chat-page__detail-descriptions :deep(.ant-descriptions-row > td) {
  padding-right: 0;
  padding-left: 0;
  border-bottom: 0;
}

.app-chat-page__detail-descriptions :deep(.ant-descriptions-item-label) {
  width: 88px;
  color: var(--app-text-secondary);
}

.app-chat-page__detail-descriptions :deep(.ant-descriptions-item-content) {
  color: var(--app-text);
}

.app-chat-page__detail-actions {
  display: flex;
  width: 100%;
  gap: 16px;
  margin-top: 20px;
}

.app-chat-page__detail-actions :deep(.ant-btn) {
  flex: 1;
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
  .preview-panel__header {
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
