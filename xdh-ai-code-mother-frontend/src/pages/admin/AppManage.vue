<template>
  <div class="app-manage-page">
    <a-card :bordered="false" class="app-manage-page__search-card">
      <a-form layout="vertical">
        <div class="app-manage-page__search-grid">
          <a-form-item label="应用 ID">
            <a-input-number v-model:value="searchParams.id" :min="1" style="width: 100%" />
          </a-form-item>

          <a-form-item label="应用名称">
            <a-input v-model:value="searchParams.appName" allow-clear placeholder="按名称搜索" />
          </a-form-item>

          <a-form-item label="用户 ID">
            <a-input-number v-model:value="searchParams.userId" :min="1" style="width: 100%" />
          </a-form-item>

          <a-form-item label="代码生成类型">
            <a-input
              v-model:value="searchParams.codeGenType"
              allow-clear
              placeholder="例如：vue"
            />
          </a-form-item>

          <a-form-item label="部署标识">
            <a-input
              v-model:value="searchParams.deployKey"
              allow-clear
              placeholder="按 deployKey 搜索"
            />
          </a-form-item>

          <a-form-item label="优先级">
            <a-input-number
              v-model:value="searchParams.priority"
              :min="0"
              :max="999"
              style="width: 100%"
            />
          </a-form-item>

          <a-form-item label="封面地址">
            <a-input v-model:value="searchParams.cover" allow-clear placeholder="按封面地址搜索" />
          </a-form-item>

          <a-form-item label="初始提示词">
            <a-input
              v-model:value="searchParams.initPrompt"
              allow-clear
              placeholder="按初始提示词搜索"
            />
          </a-form-item>
        </div>

        <div class="app-manage-page__search-actions">
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>

    <a-card :bordered="false">
      <a-table
        :columns="columns"
        :data-source="records"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1280 }"
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

          <template v-else-if="column.dataIndex === 'priority'">
            <a-tag :color="(record.priority ?? 0) >= 99 ? 'gold' : 'default'">
              {{ record.priority ?? 0 }}
            </a-tag>
          </template>

          <template v-else-if="column.dataIndex === 'deployKey'">
            <a-typography-text :copyable="Boolean(record.deployKey)">
              {{ record.deployKey || '-' }}
            </a-typography-text>
          </template>

          <template v-else-if="column.dataIndex === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>

          <template v-else-if="column.dataIndex === 'updateTime'">
            {{ formatDateTime(record.updateTime) }}
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space wrap>
              <a-button type="link" @click="openEditPage(record)">编辑</a-button>
              <a-button type="link" @click="setAsFeatured(record)">精选</a-button>
              <a-button danger type="link" @click="deleteRecord(record)">删除</a-button>
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

const router = useRouter()

const loading = ref(false)
const records = ref<API.AppVO[]>([])
const total = ref(0)

const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'desc',
  id: undefined,
  appName: '',
  cover: '',
  initPrompt: '',
  codeGenType: '',
  deployKey: '',
  priority: undefined,
  userId: undefined,
})

const columns = [
  { title: 'ID', dataIndex: 'id', width: 90 },
  { title: '应用名称', dataIndex: 'appName', width: 200 },
  { title: '封面', dataIndex: 'cover', width: 110 },
  { title: '初始提示词', dataIndex: 'initPrompt', ellipsis: true, width: 240 },
  { title: '代码类型', dataIndex: 'codeGenType', width: 120 },
  { title: '部署标识', dataIndex: 'deployKey', width: 180 },
  { title: '优先级', dataIndex: 'priority', width: 100 },
  { title: '用户 ID', dataIndex: 'userId', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '更新时间', dataIndex: 'updateTime', width: 180 },
  { title: '操作', key: 'action', fixed: 'right', width: 180 },
]

const pagination = computed(() => ({
  current: searchParams.pageNum ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (value: number) => `共 ${value} 条`,
}))

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await adminListAppVoByPage({ ...searchParams })
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
  searchParams.id = undefined
  searchParams.appName = ''
  searchParams.cover = ''
  searchParams.initPrompt = ''
  searchParams.codeGenType = ''
  searchParams.deployKey = ''
  searchParams.priority = undefined
  searchParams.userId = undefined
  searchParams.pageNum = 1
  searchParams.pageSize = 10
  void loadRecords()
}

const handleTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  void loadRecords()
}

const openEditPage = (record: API.AppVO) => {
  if (!record.id) {
    return
  }
  const targetUrl = router.resolve({ path: `/app/edit/${record.id}` }).href
  window.open(targetUrl, '_blank', 'noopener,noreferrer')
}

const setAsFeatured = async (record: API.AppVO) => {
  if (!record.id) {
    return
  }

  const res = await adminUpdateApp({
    id: record.id,
    appName: record.appName,
    cover: record.cover,
    priority: 99,
  })
  if (res.data.code === 0) {
    message.success('已设为精选')
    await loadRecords()
    return
  }
  message.error(res.data.message || '设置精选失败')
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

.app-manage-page__search-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0 16px;
}

.app-manage-page__search-actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1200px) {
  .app-manage-page__search-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .app-manage-page__search-grid {
    grid-template-columns: 1fr;
  }

  .app-manage-page__search-actions {
    justify-content: stretch;
  }
}
</style>
