<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="hero-section__content">
        <h1 class="hero-section__title">
          <span>一句话</span>
          <img alt="logo" class="hero-section__title-logo" src="@/assets/logo.png" />
          <span>呈所想</span>
        </h1>
        <p class="hero-section__subtitle">与 AI 对话轻松创建应用和网站</p>

        <div class="hero-section__composer">
          <a-textarea
            v-model:value="prompt"
            :auto-size="{ minRows: 4, maxRows: 7 }"
            :maxlength="2000"
            class="hero-section__textarea"
            placeholder="例如：帮我做一个适配移动端的企业官网，包含首页、产品页和联系表单"
            :bordered="false"
            @press-enter="handleComposerEnter"
          />

          <div class="hero-section__composer-footer">
            <div class="hero-section__composer-tools">
              <span class="hero-section__tool-chip">支持中文</span>
              <span class="hero-section__tool-chip">实时预览</span>
            </div>

            <a-button
              type="primary"
              shape="circle"
              size="large"
              :loading="creatingApp"
              class="hero-section__submit"
              @click="handleCreateApp"
            >
              <template v-if="!creatingApp" #icon>
                <ArrowUpOutlined />
              </template>
            </a-button>
          </div>
        </div>

        <div class="hero-section__examples">
          <button
            v-for="example in examplePrompts"
            :key="example"
            class="hero-section__example-pill"
            type="button"
            @click="prompt = example"
          >
            {{ example }}
          </button>
        </div>
      </div>
    </section>

    <div class="home-page__panels">
      <section class="app-section">
        <div class="section-card">
          <div class="section-card__header">
            <div>
              <h2 class="section-card__title">我的应用</h2>
              <p class="section-card__desc">
                {{
                  loginUserStore.loginUser.id
                    ? '继续优化、查看和管理你创建的应用。'
                    : '登录后可查看和管理自己的应用。'
                }}
              </p>
            </div>

            <div v-if="loginUserStore.loginUser.id" class="section-card__search">
              <a-input-search
                v-model:value="mySearchKeyword"
                allow-clear
                placeholder="按应用名称搜索"
                @search="handleMySearch"
              />
            </div>
          </div>

          <template v-if="loginUserStore.loginUser.id">
            <a-spin :spinning="myAppsLoading">
              <div v-if="myApps.length" class="app-grid">
                <AppCard
                  v-for="app in myApps"
                  :key="app.id"
                  :app="app"
                  badge="我的应用"
                  can-delete
                  can-edit
                  @open="openAppDetail"
                  @open-work="openAppWork"
                  @edit="openOwnAppEdit"
                  @delete="handleDeleteOwnApp"
                />
              </div>

              <a-empty v-else description="暂时还没有应用，试试上面的提示词吧。" />
            </a-spin>

            <div class="section-card__pagination">
              <a-pagination
                v-model:current="mySearchParams.pageNum"
                v-model:page-size="mySearchParams.pageSize"
                :page-size-options="pageSizeOptions"
                :total="myAppsTotal"
                show-size-changer
                show-less-items
                @change="handleMyPageChange"
              />
            </div>
          </template>

          <div v-else class="section-card__empty-auth">
            <a-empty description="登录后即可查看自己的应用列表、编辑名称和删除应用。">
              <a-button type="primary" @click="goToLogin">去登录</a-button>
            </a-empty>
          </div>
        </div>
      </section>

      <section class="app-section">
        <div class="section-card">
          <div class="section-card__header">
            <div>
              <h2 class="section-card__title">精选应用</h2>
              <p class="section-card__desc">浏览平台精选案例，查看生成效果和创意方向。</p>
            </div>

            <div class="section-card__search">
              <a-input-search
                v-model:value="featuredSearchKeyword"
                allow-clear
                placeholder="按应用名称搜索"
                @search="handleFeaturedSearch"
              />
            </div>
          </div>

          <a-spin :spinning="featuredAppsLoading">
            <div v-if="featuredApps.length" class="app-grid">
              <AppCard
                v-for="app in featuredApps"
                :key="app.id"
                :app="app"
                badge="精选"
                @open="openAppDetail"
                @open-work="openAppWork"
              />
            </div>

            <a-empty v-else description="暂时还没有精选应用。" />
          </a-spin>

          <div class="section-card__pagination">
            <a-pagination
              v-model:current="featuredSearchParams.pageNum"
              v-model:page-size="featuredSearchParams.pageSize"
              :page-size-options="pageSizeOptions"
              :total="featuredAppsTotal"
              show-size-changer
              show-less-items
              @change="handleFeaturedPageChange"
            />
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'HomePage' })

import { onActivated, onDeactivated, onMounted, reactive, ref, watch } from 'vue'
import { ArrowUpOutlined } from '@ant-design/icons-vue'
import { Modal, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'

import AppCard from '@/components/app/AppCard.vue'
import { addApp, deleteApp, listGoodAppVoByPage, listMyAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { getAppIdString, resolveAppDeployUrl } from '@/utils/app'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const examplePrompts = [
  '波普风电商页面',
  '企业网站',
  '电商运营后台',
  '暗黑话题社区',
]

const HOME_PAGE_SIZE = 10
const pageSizeOptions = ['10', '20']

const prompt = ref('')
const creatingApp = ref(false)

const myApps = ref<API.AppVO[]>([])
const myAppsLoading = ref(false)
const myAppsTotal = ref(0)
const mySearchKeyword = ref('')
const mySearchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: HOME_PAGE_SIZE,
  sortField: 'createTime',
  sortOrder: 'desc',
  appName: '',
})

const featuredApps = ref<API.AppVO[]>([])
const featuredAppsLoading = ref(false)
const featuredAppsTotal = ref(0)
const featuredSearchKeyword = ref('')
const featuredSearchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: HOME_PAGE_SIZE,
  sortField: 'priority',
  sortOrder: 'desc',
  appName: '',
})

const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    myApps.value = []
    myAppsTotal.value = 0
    return
  }

  myAppsLoading.value = true
  try {
    const res = await listMyAppVoByPage({ ...mySearchParams })
    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records ?? []
      myAppsTotal.value = res.data.data.totalRow ?? 0
      return
    }
    message.error(res.data.message || '获取我的应用失败')
  } finally {
    myAppsLoading.value = false
  }
}

const loadFeaturedApps = async () => {
  featuredAppsLoading.value = true
  try {
    const res = await listGoodAppVoByPage({ ...featuredSearchParams })
    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records ?? []
      featuredAppsTotal.value = res.data.data.totalRow ?? 0
      return
    }
    message.error(res.data.message || '获取精选应用失败')
  } finally {
    featuredAppsLoading.value = false
  }
}

const handleCreateApp = async () => {
  const trimmedPrompt = prompt.value.trim()
  if (!trimmedPrompt) {
    message.warning('请先输入应用需求')
    return
  }
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录后再创建应用')
    await router.push(`/user/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`)
    return
  }

  creatingApp.value = true
  try {
    const res = await addApp({ initPrompt: trimmedPrompt })
    if (res.data.code === 0 && res.data.data) {
      const appId = getAppIdString(res.data.data)
      sessionStorage.setItem(`app:autoPrompt:${appId}`, trimmedPrompt)
      message.success('应用创建成功，正在进入对话页')
      await router.push({
        path: `/app/chat/${appId}`,
        query: { autoPrompt: '1' },
      })
      return
    }
    message.error(res.data.message || '创建应用失败')
  } finally {
    creatingApp.value = false
  }
}

const handleComposerEnter = (event: KeyboardEvent) => {
  if (event.shiftKey) {
    return
  }
  event.preventDefault()
  void handleCreateApp()
}

const openAppDetail = async (app: API.AppVO) => {
  if (!app.id) {
    return
  }

  await router.push({
    path: `/app/chat/${app.id}`,
    query: { view: '1' },
  })
}

const openAppWork = (app: API.AppVO) => {
  const deployUrl = resolveAppDeployUrl(app)
  if (!deployUrl) {
    return
  }

  window.open(deployUrl, '_blank', 'noopener,noreferrer')
}

const openOwnAppEdit = async (app: API.AppVO) => {
  if (!app.id) {
    return
  }
  await router.push(`/app/edit/${app.id}`)
}

const handleDeleteOwnApp = (app: API.AppVO) => {
  if (!app.id) {
    return
  }

  Modal.confirm({
    title: '确认删除该应用吗？',
    content: '删除后无法恢复。',
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      const res = await deleteApp({ id: app.id })
      if (res.data.code === 0) {
        message.success('删除成功')
        if (myApps.value.length === 1 && (mySearchParams.pageNum ?? 1) > 1) {
          mySearchParams.pageNum = (mySearchParams.pageNum ?? 1) - 1
        }
        await loadMyApps()
        return
      }
      message.error(res.data.message || '删除失败')
    },
  })
}

const handleMySearch = () => {
  mySearchParams.appName = mySearchKeyword.value.trim()
  mySearchParams.pageNum = 1
  void loadMyApps()
}

const handleFeaturedSearch = () => {
  featuredSearchParams.appName = featuredSearchKeyword.value.trim()
  featuredSearchParams.pageNum = 1
  void loadFeaturedApps()
}

const handleMyPageChange = (page: number, pageSize: number) => {
  mySearchParams.pageNum = page
  mySearchParams.pageSize = pageSize
  void loadMyApps()
}

const handleFeaturedPageChange = (page: number, pageSize: number) => {
  featuredSearchParams.pageNum = page
  featuredSearchParams.pageSize = pageSize
  void loadFeaturedApps()
}

const goToLogin = async () => {
  await router.push(`/user/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`)
}

watch(
  () => loginUserStore.loginUser.id,
  () => {
    void loadMyApps()
  }
)

let savedScrollY = 0

onDeactivated(() => {
  savedScrollY = window.scrollY || document.documentElement.scrollTop
})

onActivated(() => {
  if (savedScrollY > 0) {
    setTimeout(() => {
      window.scrollTo(0, savedScrollY)
    }, 0)
  }
})

onMounted(() => {
  void loadMyApps()
  void loadFeaturedApps()
})
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 72px);
}

.hero-section {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 58vh;
  padding: 72px 24px 48px;
}

.hero-section__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
  width: min(100%, 860px);
  margin: 0 auto;
  text-align: center;
}

.hero-section__title {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin: 0;
  color: #0f172a;
  font-size: clamp(2.2rem, 5vw, 3.2rem);
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: 0.02em;
}

.hero-section__title-logo {
  width: 52px;
  height: 52px;
  object-fit: contain;
  border-radius: 999px;
  box-shadow: 0 10px 24px rgba(20, 184, 166, 0.28);
}

.hero-section__subtitle {
  margin: 0;
  color: #64748b;
  font-size: 1rem;
  line-height: 1.7;
}

.hero-section__composer {
  width: 100%;
  padding: 20px 22px 16px;
  text-align: left;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 28px;
  box-shadow:
    0 18px 50px rgba(15, 23, 42, 0.08),
    0 2px 0 rgba(255, 255, 255, 0.8) inset;
}

.hero-section__textarea :deep(textarea) {
  padding: 0;
  color: #334155;
  font-size: 1rem;
  line-height: 1.75;
  background: transparent;
  box-shadow: none;
  resize: none;
}

.hero-section__textarea :deep(textarea::placeholder) {
  color: #94a3b8;
}

.hero-section__composer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 12px;
}

.hero-section__composer-tools {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-section__tool-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  color: #64748b;
  font-size: 0.86rem;
  background: #f1f5f9;
  border-radius: 999px;
}

.hero-section__submit {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  min-width: 46px;
  border: 0;
  background: #1e293b;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.22);
}

.hero-section__submit:hover,
.hero-section__submit:focus {
  background: #0f172a !important;
}

.hero-section__examples {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-top: 4px;
}

.hero-section__example-pill {
  padding: 8px 16px;
  color: #475569;
  font-size: 0.92rem;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 999px;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    background 0.2s ease;
}

.hero-section__example-pill:hover {
  background: #ffffff;
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.08);
}

.home-page__panels {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: min(100%, 1280px);
  margin: 0 auto;
  padding: 0 20px 28px;
}

.app-section {
  width: 100%;
}

.section-card {
  padding: 28px 30px 24px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 36px 36px 28px 28px;
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

.section-card__desc {
  margin: 8px 0 0;
  color: var(--app-text-secondary);
}

.section-card__search {
  width: min(100%, 300px);
}

.section-card__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 28px;
}

.section-card__empty-auth {
  padding: 18px 0 10px;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

@media (max-width: 1280px) {
  .app-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .hero-section {
    min-height: auto;
    padding: 48px 18px 36px;
  }

  .hero-section__composer-footer,
  .section-card__header {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-section__composer-footer {
    align-items: flex-end;
  }

  .section-card__search {
    width: 100%;
  }

  .section-card {
    padding: 22px;
    border-radius: 28px;
  }

  .app-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .hero-section__title {
    gap: 10px;
    font-size: 1.8rem;
  }

  .hero-section__title-logo {
    width: 40px;
    height: 40px;
  }

  .section-card__title {
    font-size: 1.45rem;
  }

  .hero-section__composer {
    padding: 16px;
    border-radius: 22px;
  }

  .home-page__panels {
    padding: 0 14px 20px;
  }
}
</style>
