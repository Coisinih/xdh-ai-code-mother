<template>
  <div class="page-shell chat-history-manage-page">
    <header class="page-shell__header">
      <div>
        <h1 class="page-shell__title">对话管理</h1>
        <p class="page-shell__desc">查看全站对话记录，用于排查生成效果与内容质量问题。</p>
      </div>
    </header>

    <a-card :bordered="false" class="chat-history-manage-page__search-card">
      <div class="chat-history-manage-page__search-row">
        <div class="chat-history-manage-page__search-grid">
          <div class="chat-history-manage-page__search-item">
            <span class="chat-history-manage-page__search-label">应用 ID：</span>
            <a-input
              v-model:value="searchForm.appId"
              inputmode="numeric"
              placeholder="按应用ID 搜索"
              style="width: 100%"
            />
          </div>

          <div class="chat-history-manage-page__search-item">
            <span class="chat-history-manage-page__search-label">用户 ID：</span>
            <a-input
              v-model:value="searchForm.userId"
              inputmode="numeric"
              placeholder="按用户ID搜索"
              style="width: 100%"
            />
          </div>

          <div class="chat-history-manage-page__search-item">
            <span class="chat-history-manage-page__search-label">消息类型：</span>
            <a-select
              v-model:value="searchForm.messageType"
              :options="messageTypeOptions"
              allow-clear
              placeholder="请选择消息类型"
            />
          </div>

          <div class="chat-history-manage-page__search-item">
            <span class="chat-history-manage-page__search-label">消息内容：</span>
            <a-input
              v-model:value="searchForm.message"
              allow-clear
              placeholder="按消息内容搜索"
              @press-enter="handleSearch"
            />
          </div>
        </div>

        <div class="chat-history-manage-page__search-actions">
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </div>
      </div>
    </a-card>

    <a-card :bordered="false" class="chat-history-manage-page__table-card">
      <a-table
        :columns="columns"
        :data-source="records"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1320 }"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'messageType'">
            <a-tag :color="getMessageTypeColor(record.messageType)">
              {{ getMessageTypeText(record.messageType) }}
            </a-tag>
          </template>

          <template v-else-if="column.dataIndex === 'message'">
            <div class="chat-history-manage-page__message-cell">
              {{ record.message || '-' }}
            </div>
          </template>

          <template v-else-if="column.dataIndex === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>

          <template v-else-if="column.dataIndex === 'updateTime'">
            {{ formatDateTime(record.updateTime) }}
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'

import { listAllChatHistoryByPageForAdmin } from '@/api/chatHistoryController'
import { formatDateTime } from '@/utils/app'

type ChatHistorySearchForm = {
  appId: string
  userId: string
  messageType?: string
  message: string
}

type ChatHistorySearchPayload = Omit<API.ChatHistoryQueryRequest, 'appId' | 'userId'> & {
  appId?: string
  userId?: string
}

const MESSAGE_TYPE_LABEL_MAP: Record<string, string> = {
  user: '用户',
  human: '用户',
  question: '用户',
  assistant: 'AI',
  ai: 'AI',
  answer: 'AI',
}

const loading = ref(false)
const records = ref<API.ChatHistory[]>([])
const total = ref(0)

const searchParams = reactive<API.ChatHistoryQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'desc',
})

const searchForm = reactive<ChatHistorySearchForm>({
  appId: '',
  userId: '',
  messageType: undefined,
  message: '',
})

const messageTypeOptions = [
  { label: '用户', value: 'user' },
  { label: 'AI', value: 'ai' },
]

const columns = [
  { title: 'ID', dataIndex: 'id', width: 120 },
  { title: '应用 ID', dataIndex: 'appId', width: 120 },
  { title: '用户 ID', dataIndex: 'userId', width: 120 },
  { title: '消息类型', dataIndex: 'messageType', width: 140 },
  { title: '消息内容', dataIndex: 'message', width: 520 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '更新时间', dataIndex: 'updateTime', width: 180 },
]

const pagination = computed(() => ({
  current: searchParams.pageNum ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (value: number) => `共${value} 条`,
}))

const buildSearchPayload = (): ChatHistorySearchPayload => {
  const appId = searchForm.appId.trim()
  const userId = searchForm.userId.trim()
  const messageText = searchForm.message.trim()

  return {
    ...searchParams,
    appId: appId || undefined,
    userId: userId || undefined,
    messageType: searchForm.messageType,
    message: messageText || undefined,
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await listAllChatHistoryByPageForAdmin(buildSearchPayload() as API.ChatHistoryQueryRequest)
    if (res.data.code === 0 && res.data.data) {
      records.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
      return
    }

    message.error(res.data.message || '获取对话记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchParams.pageNum = 1
  void loadRecords()
}

const resetSearch = () => {
  searchForm.appId = ''
  searchForm.userId = ''
  searchForm.messageType = undefined
  searchForm.message = ''
  searchParams.pageNum = 1
  searchParams.pageSize = 10
  void loadRecords()
}

const handleTableChange = (page: { current?: number; pageSize?: number }) => {
  searchParams.pageNum = page.current ?? 1
  searchParams.pageSize = page.pageSize ?? 10
  void loadRecords()
}

const getMessageTypeText = (messageType?: string) => {
  if (!messageType) {
    return '-'
  }

  return MESSAGE_TYPE_LABEL_MAP[messageType.trim().toLowerCase()] || messageType
}

const getMessageTypeColor = (messageType?: string) => {
  const normalized = messageType?.trim().toLowerCase()
  if (!normalized) {
    return 'default'
  }

  if (['user', 'human', 'question'].includes(normalized)) {
    return 'blue'
  }

  return 'green'
}

onMounted(() => {
  void loadRecords()
})
</script>

<style scoped>
.chat-history-manage-page__search-card,
.chat-history-manage-page__table-card {
  border: 1px solid var(--border-color) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-xs);
}

.chat-history-manage-page__search-card :deep(.ant-card-body) {
  padding: var(--space-5);
}

.chat-history-manage-page__table-card :deep(.ant-card-body) {
  padding: var(--space-3) var(--space-5) var(--space-5);
}

.chat-history-manage-page__search-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.chat-history-manage-page__search-grid {
  display: grid;
  flex: 1;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-4);
}

.chat-history-manage-page__search-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.chat-history-manage-page__search-label {
  flex: 0 0 84px;
  color: var(--text-secondary);
  font-size: var(--font-size-base);
  text-align: right;
}

.chat-history-manage-page__search-item :deep(.ant-input),
.chat-history-manage-page__search-item :deep(.ant-select),
.chat-history-manage-page__search-item :deep(.ant-input-number) {
  flex: 1;
}

.chat-history-manage-page__search-item :deep(.ant-select-selector) {
  width: 100%;
}

.chat-history-manage-page__search-actions {
  flex-shrink: 0;
}

.chat-history-manage-page__message-cell {
  display: -webkit-box;
  overflow: hidden;
  color: var(--text-secondary);
  line-height: 1.7;
  word-break: break-word;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

@media (max-width: 1360px) {
  .chat-history-manage-page__search-row {
    align-items: stretch;
    flex-direction: column;
  }

  .chat-history-manage-page__search-actions {
    display: flex;
    justify-content: flex-end;
  }
}

@media (max-width: 960px) {
  .chat-history-manage-page__search-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .chat-history-manage-page__search-grid {
    grid-template-columns: 1fr;
  }

  .chat-history-manage-page__search-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .chat-history-manage-page__search-label {
    flex-basis: auto;
    text-align: left;
  }

  .chat-history-manage-page__search-actions {
    justify-content: stretch;
  }
}
</style>

