<template>
  <div class="app-manage-page">
    <a-card :bordered="false" class="app-manage-page__search-card">
      <div class="app-manage-page__search-row">
        <div class="app-manage-page__search-grid">
          <div class="app-manage-page__search-item">
            <span class="app-manage-page__search-label">应用名称：</span>
            <a-input
              v-model:value="searchForm.appName"
              allow-clear
              placeholder="按应用名称搜索"
              @press-enter="handleSearch"
            />
          </div>

          <div class="app-manage-page__search-item">
            <span class="app-manage-page__search-label">创建者：</span>
            <a-input
              v-model:value="searchForm.userName"
              allow-clear
              placeholder="按创建者用户名搜索"
              @press-enter="handleSearch"
            />
          </div>

          <div class="app-manage-page__search-item">
            <span class="app-manage-page__search-label">生成类型：</span>
            <a-select
              v-model:value="searchForm.codeGenType"
              allow-clear
              :options="codeGenTypeOptions"
              placeholder="请选择生成类型"
            />
          </div>

          <div class="app-manage-page__search-item app-manage-page__search-item--checkbox">
            <span class="app-manage-page__search-label"></span>
            <a-checkbox v-model:checked="searchForm.onlyFeatured">只看精选应用</a-checkbox>
          </div>
        </div>

        <div class="app-manage-page__search-actions">
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </div>
      </div>
    </a-card>

    <a-card :bordered="false">
      <a-table
        :columns="columns"
        :data-source="records"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1240 }"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'cover'">
            <a-image
              v-if="record.cover"
              :src="record.cover"
              :width="72"
              :preview="false"
              style="border-radius: 10px"
            />
            <span v-else>-</span>
          </template>

          <template v-else-if="column.dataIndex === 'userName'">
            {{ record.user?.userName || '-' }}
          </template>

          <template v-else-if="column.dataIndex === 'codeGenType'">
            <a-tag color="blue">
              {{ getCodeGenTypeText(record.codeGenType) }}
            </a-tag>
          </template>

          <template v-else-if="column.dataIndex === 'featuredStatus'">
            <a-tag :color="isFeaturedApp(record) ? 'gold' : 'default'">
              {{ isFeaturedApp(record) ? '精选应用' : '普通应用' }}
            </a-tag>
          </template>

          <template v-else-if="column.dataIndex === 'deployedTime'">
            {{ record.deployedTime ? formatDateTime(record.deployedTime) : '未部署' }}
          </template>

          <template v-else-if="column.dataIndex === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>

          <template v-else-if="column.dataIndex === 'updateTime'">
            {{ formatDateTime(record.updateTime) }}
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space wrap>
              <a-button size="small" type="primary" @click="openEditPage(record)">编辑</a-button>
              <a-button
                size="small"
                :class="{ 'app-manage-page__unfeature-button': isFeaturedApp(record) }"
                @click="toggleFeaturedStatus(record)"
              >
                {{ isFeaturedApp(record) ? '取消精选' : '精选' }}
              </a-button>
              <a-button size="small" danger @click="deleteRecord(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'

import { adminDeleteApp, adminListAppVoByPage, adminUpdateApp } from '@/api/appController'
import { formatDateTime } from '@/utils/app'

const GOOD_APP_PRIORITY = 99
const DEFAULT_APP_PRIORITY = 0

const CODE_GEN_TYPE_MAP = {
  html: '原生 HTML 模式',
  multi_file: '原生多文件模式',
} as const

type AdminAppSearchForm = {
  appName: string
  userName: string
  codeGenType?: keyof typeof CODE_GEN_TYPE_MAP
  onlyFeatured: boolean
}

type AdminAppListRequest = API.AppQueryRequest & {
  userName?: string
}

const router = useRouter()

const loading = ref(false)
const records = ref<API.AppVO[]>([])
const total = ref(0)

const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'desc',
})

const searchForm = reactive<AdminAppSearchForm>({
  appName: '',
  userName: '',
  codeGenType: undefined,
  onlyFeatured: false,
})

const codeGenTypeOptions = [
  { label: '原生 HTML 模式', value: 'html' },
  { label: '原生多文件模式', value: 'multi_file' },
]

const columns = [
  { title: 'ID', dataIndex: 'id', width: 120 },
  { title: '应用名称', dataIndex: 'appName', width: 220, ellipsis: true },
  { title: '封面', dataIndex: 'cover', width: 110 },
  { title: '创建者', dataIndex: 'userName', width: 160 },
  { title: '生成类型', dataIndex: 'codeGenType', width: 150 },
  { title: '应用类型', dataIndex: 'featuredStatus', width: 130 },
  { title: '部署时间', dataIndex: 'deployedTime', width: 180 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '更新时间', dataIndex: 'updateTime', width: 180 },
  { title: '操作', key: 'action', fixed: 'right', width: 190 },
]

const pagination = computed(() => ({
  current: searchParams.pageNum ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (value: number) => `共 ${value} 条`,
}))

const isFeaturedApp = (record: API.AppVO) => {
  return (record.priority ?? DEFAULT_APP_PRIORITY) >= GOOD_APP_PRIORITY
}

const getCodeGenTypeText = (codeGenType?: string) => {
  if (!codeGenType) {
    return '-'
  }

  return CODE_GEN_TYPE_MAP[codeGenType as keyof typeof CODE_GEN_TYPE_MAP] || codeGenType
}

const buildSearchPayload = (): AdminAppListRequest => {
  const appName = searchForm.appName.trim()
  const userName = searchForm.userName.trim()

  return {
    ...searchParams,
    appName: appName || undefined,
    codeGenType: searchForm.codeGenType || undefined,
    priority: searchForm.onlyFeatured ? GOOD_APP_PRIORITY : undefined,
    userName: userName || undefined,
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await adminListAppVoByPage(buildSearchPayload() as API.AppQueryRequest)
    if (res.data.code === 0 && res.data.data) {
      records.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
      return
    }
    message.error(res.data.message || '获取应用列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchParams.pageNum = 1
  void loadRecords()
}

const resetSearch = () => {
  searchForm.appName = ''
  searchForm.userName = ''
  searchForm.codeGenType = undefined
  searchForm.onlyFeatured = false
  searchParams.pageNum = 1
  searchParams.pageSize = 10
  void loadRecords()
}

const handleTableChange = (page: { current?: number; pageSize?: number }) => {
  searchParams.pageNum = page.current ?? 1
  searchParams.pageSize = page.pageSize ?? 10
  void loadRecords()
}

const openEditPage = (record: API.AppVO) => {
  if (!record.id) {
    return
  }

  const targetUrl = router.resolve({ path: `/app/edit/${record.id}` }).href
  window.open(targetUrl, '_blank', 'noopener,noreferrer')
}

const toggleFeaturedStatus = async (record: API.AppVO) => {
  if (!record.id) {
    return
  }

  const nextPriority = isFeaturedApp(record) ? DEFAULT_APP_PRIORITY : GOOD_APP_PRIORITY
  const actionText = nextPriority === GOOD_APP_PRIORITY ? '设为精选' : '取消精选'
  const successText = nextPriority === GOOD_APP_PRIORITY ? '已设为精选' : '已取消精选'

  const res = await adminUpdateApp({
    id: record.id,
    appName: record.appName,
    cover: record.cover,
    priority: nextPriority,
  })

  if (res.data.code === 0) {
    message.success(successText)
    await loadRecords()
    return
  }

  message.error(res.data.message || `${actionText}失败`)
}

const deleteRecord = (record: API.AppVO) => {
  if (!record.id) {
    return
  }

  Modal.confirm({
    title: `确认删除应用「${record.appName || record.id}」吗？`,
    content: '删除后无法恢复。',
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      const res = await adminDeleteApp({ id: record.id })
      if (res.data.code === 0) {
        message.success('删除成功')
        if (records.value.length === 1 && (searchParams.pageNum ?? 1) > 1) {
          searchParams.pageNum = (searchParams.pageNum ?? 1) - 1
        }
        await loadRecords()
        return
      }
      message.error(res.data.message || '删除失败')
    },
  })
}

onMounted(() => {
  void loadRecords()
})
</script>

<style scoped>
.app-manage-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.app-manage-page__search-card {
  border-radius: 24px;
}

.app-manage-page__search-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.app-manage-page__search-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.app-manage-page__search-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.app-manage-page__search-item--checkbox {
  min-height: 32px;
}

.app-manage-page__search-label {
  flex: 0 0 84px;
  color: rgba(0, 0, 0, 0.88);
  text-align: right;
}

.app-manage-page__search-item :deep(.ant-input),
.app-manage-page__search-item :deep(.ant-select) {
  flex: 1;
}

.app-manage-page__search-item :deep(.ant-select-selector) {
  width: 100%;
}

.app-manage-page__search-actions {
  flex-shrink: 0;
}

.app-manage-page__unfeature-button {
  color: #d46b08;
  background: #fff7e6;
  border-color: #ffd591;
}

.app-manage-page__unfeature-button:hover,
.app-manage-page__unfeature-button:focus {
  color: #ad4e00;
  background: #ffe7ba;
  border-color: #ffc069;
}

@media (max-width: 1360px) {
  .app-manage-page__search-row {
    align-items: stretch;
    flex-direction: column;
  }

  .app-manage-page__search-actions {
    display: flex;
    justify-content: flex-end;
  }
}

@media (max-width: 960px) {
  .app-manage-page__search-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .app-manage-page__search-grid {
    grid-template-columns: 1fr;
  }

  .app-manage-page__search-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .app-manage-page__search-label {
    flex-basis: auto;
    text-align: left;
  }

  .app-manage-page__search-actions {
    justify-content: stretch;
  }
}
</style>
