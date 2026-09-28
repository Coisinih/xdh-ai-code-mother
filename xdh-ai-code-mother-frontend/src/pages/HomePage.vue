<template>
  <div class="home-page">
    <section class="hero">
      <div aria-hidden="true" class="hero__decor">
        <div class="hero__glow"></div>
        <span class="hero__blob hero__blob--yolk"></span>
        <span class="hero__blob hero__blob--peach"></span>
        <span class="hero__blob hero__blob--mint"></span>
      </div>

      <div class="hero__inner">
        <span class="hero__badge">
          <img alt="" class="hero__badge-logo" src="@/assets/logo.png" />
          咸蛋黄 AI · 零代码应用生成
        </span>

        <h1 class="hero__title">一句话生所想</h1>
        <p class="hero__subtitle">
          用 AI 对话轻松创建应用和网站，生成后即可实时预览、一键部署并下载源码
        </p>

        <div class="composer">
          <a-textarea
            v-model:value="prompt"
            :auto-size="{ minRows: 3, maxRows: 7 }"
            :bordered="false"
            :maxlength="2000"
            class="composer__input"
            placeholder="例如：帮我做一个适配移动端的企业官网，包含首页、产品页和联系表单"
            @press-enter="handleComposerEnter"
          />

          <div class="composer__footer">
            <div class="composer__tools">
              <span class="composer__chip">
                <BulbOutlined />
                支持中文描述
              </span>
              <span class="composer__chip">
                <ThunderboltOutlined />
                实时预览
              </span>
            </div>

            <a-button
              class="composer__submit"
              :loading="creatingApp"
              type="primary"
              @click="handleCreateApp"
            >
              <template v-if="!creatingApp" #icon>
                <ArrowUpOutlined />
              </template>
              生成应用
            </a-button>
          </div>
        </div>

        <div class="hero__examples">
          <span class="hero__examples-label">试试：</span>
          <button
            v-for="example in examplePrompts"
            :key="example"
            class="hero__example"
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
        <SectionCard title="我的作品">
          <template v-if="loginUserStore.loginUser.id" #extra>
            <div class="home-page__section-search">
              <a-input-search
                v-model:value="mySearchKeyword"
                allow-clear
                placeholder="按应用名称搜索"
                @search="handleMySearch"
              />
            </div>
          </template>

          <template v-if="loginUserStore.loginUser.id">
            <a-spin :spinning="myAppsLoading">
              <div v-if="myApps.length" class="app-grid">
                <AppCard
                  v-for="app in myApps"
                  :key="app.id"
                  :app="app"
                  can-delete
                  can-edit
                  @delete="handleDeleteOwnApp"
                  @edit="openOwnAppEdit"
                  @open="openAppDetail"
                  @open-work="openAppWork"
                />
              </div>

              <a-empty v-else description="暂时还没有应用，试试上面的提示词吧。" />
            </a-spin>
          </template>

          <div v-else class="home-page__empty-auth">
            <a-empty description="登录后即可查看自己的应用列表、编辑名称和删除应用。">
              <a-button type="primary" @click="goToLogin">去登录</a-button>
            </a-empty>
          </div>

          <template v-if="loginUserStore.loginUser.id" #footer>
            <a-pagination
              v-model:current="mySearchParams.pageNum"
              v-model:page-size="mySearchParams.pageSize"
              :total="myAppsTotal"
              show-less-items
              @change="handleMyPageChange"
            />
          </template>
        </SectionCard>
      </section>

      <section class="app-section">
        <SectionCard title="精选案例">
          <template #extra>
            <div class="home-page__section-search">
              <a-input-search
                v-model:value="featuredSearchKeyword"
                allow-clear
                placeholder="按应用名称搜索"
                @search="handleFeaturedSearch"
              />
            </div>
          </template>

          <a-spin :spinning="featuredAppsLoading">
            <div v-if="featuredApps.length" class="app-grid">
              <AppCard
                v-for="app in featuredApps"
                :key="app.id"
                :app="app"
                @open="openAppDetail"
                @open-work="openAppWork"
              />
            </div>

            <a-empty v-else description="暂时还没有精选应用。" />
          </a-spin>

          <template #footer>
            <div>共 {{ featuredAppsTotal }} 个案例</div>
            <a-pagination
              v-model:current="featuredSearchParams.pageNum"
              v-model:page-size="featuredSearchParams.pageSize"
              :total="featuredAppsTotal"
              show-less-items
              @change="handleFeaturedPageChange"
            />
          </template>
        </SectionCard>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'HomePage' })

import { nextTick, onActivated, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ArrowUpOutlined, BulbOutlined, ThunderboltOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'

import AppCard from '@/components/app/AppCard.vue'
import SectionCard from '@/components/common/SectionCard.vue'
import { addApp, deleteApp, listGoodAppVoByPage, listMyAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import { getAppIdString, resolveAppDeployUrl } from '@/utils/app'
import { openInNewTab } from '@/utils/browser'
import { confirmDangerAction } from '@/utils/confirm'
import { consumeHomeRefreshNeeded } from '@/utils/homeRefresh'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const examplePrompts = ['波普风电商页面', '企业网站', '电商运营后台', '暗黑主题社区']

const HOME_PAGE_SIZE = 6

const HOME_SCROLL_KEY = 'home:scrollY'
const HOME_LIST_STATE_KEY = 'home:listState'

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
    await router.push(
      `/user/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`,
    )
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
  openInNewTab(resolveAppDeployUrl(app))
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

  confirmDangerAction({
    title: '确认删除该应用吗？',
    content: '删除后无法恢复。',
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
  await router.push(
    `/user/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`,
  )
}

watch(
  () => loginUserStore.loginUser.id,
  () => {
    void loadMyApps()
  },
)

const refreshHomeDataIfNeeded = async () => {
  if (!consumeHomeRefreshNeeded()) {
    return
  }

  await Promise.allSettled([loadMyApps(), loadFeaturedApps()])
  await nextTick()
}

const saveHomeScroll = () => {
  const top = window.scrollY || document.documentElement.scrollTop || 0
  sessionStorage.setItem(HOME_SCROLL_KEY, String(top))
}

const restoreHomeScroll = () => {
  const saved = Number(sessionStorage.getItem(HOME_SCROLL_KEY))
  if (Number.isFinite(saved) && saved > 0) {
    sessionStorage.removeItem(HOME_SCROLL_KEY)
    window.scrollTo({ top: saved })
  }
}

const saveHomeListState = () => {
  sessionStorage.setItem(
    HOME_LIST_STATE_KEY,
    JSON.stringify({
      mySearchParams,
      featuredSearchParams,
      mySearchKeyword: mySearchKeyword.value,
      featuredSearchKeyword: featuredSearchKeyword.value,
    }),
  )
}

const restoreHomeListState = () => {
  const raw = sessionStorage.getItem(HOME_LIST_STATE_KEY)
  if (!raw) {
    return
  }

  sessionStorage.removeItem(HOME_LIST_STATE_KEY)
  try {
    const state = JSON.parse(raw) as {
      mySearchParams?: API.AppQueryRequest
      featuredSearchParams?: API.AppQueryRequest
      mySearchKeyword?: string
      featuredSearchKeyword?: string
    }

    if (state.mySearchParams) {
      Object.assign(mySearchParams, state.mySearchParams)
    }
    if (state.featuredSearchParams) {
      Object.assign(featuredSearchParams, state.featuredSearchParams)
    }
    mySearchKeyword.value = state.mySearchKeyword ?? ''
    featuredSearchKeyword.value = state.featuredSearchKeyword ?? ''
  } catch {
    // 忽略损坏的状态数据
  }
}

const loadHomeDataAndRestoreScroll = async () => {
  restoreHomeListState()
  await Promise.allSettled([loadMyApps(), loadFeaturedApps()])
  await nextTick()
  restoreHomeScroll()
}

onActivated(async () => {
  await refreshHomeDataIfNeeded()
})

onMounted(() => {
  void loadHomeDataAndRestoreScroll()
})

onBeforeUnmount(() => {
  saveHomeScroll()
  saveHomeListState()
})
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: var(--space-10);
  width: 100%;
}

.hero {
  position: relative;
  display: flex;
  justify-content: center;
  padding: var(--space-10) 0 var(--space-6);
  isolation: isolate;
}

.hero::before {
  content: '';
  position: absolute;
  inset: -8% -4% 0;
  z-index: -1;
  background-image: radial-gradient(rgba(194, 65, 12, 0.1) 1.4px, transparent 1.4px);
  background-size: 26px 26px;
  -webkit-mask-image: radial-gradient(closest-side at 50% 42%, #000 28%, transparent 78%);
  mask-image: radial-gradient(closest-side at 50% 42%, #000 28%, transparent 78%);
  pointer-events: none;
}

.hero__decor {
  position: absolute;
  inset: -140px 0 auto;
  height: 680px;
  z-index: -1;
  overflow: hidden;
  pointer-events: none;
  /* 底部渐隐，避免装饰层与页面底色之间出现生硬的分界线 */
  -webkit-mask-image: linear-gradient(to bottom, #000 0%, #000 52%, transparent 100%);
  mask-image: linear-gradient(to bottom, #000 0%, #000 52%, transparent 100%);
}

.hero__glow {
  position: absolute;
  top: 0;
  left: 50%;
  width: min(1080px, 130%);
  height: 520px;
  background:
    radial-gradient(closest-side, rgba(255, 205, 92, 0.44), transparent 72%),
    radial-gradient(closest-side at 63% 42%, rgba(255, 170, 120, 0.34), transparent 70%);
  transform: translateX(-50%);
}

.hero__blob {
  position: absolute;
  display: block;
  border-radius: 50%;
  filter: blur(34px);
  opacity: 0.6;
  animation: hero-float 10s var(--ease-out) infinite;
}

.hero__blob--yolk {
  top: 18%;
  left: 5%;
  width: 190px;
  height: 190px;
  background: #ffd666;
  opacity: 0.55;
}

.hero__blob--peach {
  top: 26%;
  right: 6%;
  width: 160px;
  height: 160px;
  background: #ffb37a;
  opacity: 0.5;
  animation-duration: 12s;
  animation-direction: reverse;
}

.hero__blob--mint {
  right: 20%;
  bottom: 6%;
  width: 140px;
  height: 140px;
  background: #bfe0f5;
  opacity: 0.4;
  animation-duration: 14s;
}

@keyframes hero-float {
  0%,
  100% {
    transform: translate3d(0, 0, 0);
  }

  50% {
    transform: translate3d(0, -16px, 0);
  }
}

.hero__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  width: min(100%, 820px);
  text-align: center;
}

.hero__badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: 5px 14px 5px 5px;
  color: var(--color-brand-800);
  font-size: var(--font-size-sm);
  font-weight: 500;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid var(--color-primary-soft-border);
  border-radius: var(--radius-pill);
  box-shadow: var(--shadow-xs);
}

.hero__badge-logo {
  width: 26px;
  height: 26px;
  object-fit: contain;
  border-radius: var(--radius-pill);
}

.hero__title {
  margin: 0;
  font-size: clamp(2.2rem, 4.6vw, 3.2rem);
  font-weight: 700;
  line-height: 1.18;
  letter-spacing: -0.02em;
}

.hero__subtitle {
  margin: 0;
  max-width: 36em;
  color: var(--text-secondary);
  font-size: var(--font-size-md);
  line-height: 1.7;
}

.composer {
  width: 100%;
  margin-top: var(--space-2);
  padding: var(--space-5);
  text-align: left;
  background: var(--bg-surface);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  transition:
    border-color var(--duration-base) var(--ease-out),
    box-shadow var(--duration-base) var(--ease-out);
}

.composer:focus-within {
  border-color: var(--color-brand-300);
  box-shadow: var(--shadow-lg);
}

.composer__input :deep(textarea) {
  padding: 0;
  color: var(--text-primary);
  font-size: var(--font-size-md);
  line-height: 1.7;
  background: transparent;
  box-shadow: none;
  resize: none;
}

.composer__input :deep(textarea::placeholder) {
  color: var(--text-tertiary);
}

.composer__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-top: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-color);
}

.composer__tools {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.composer__chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  background: var(--bg-surface-muted);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-pill);
}

.composer__chip :deep(.anticon) {
  color: var(--color-brand-600);
}

.composer__submit {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex-shrink: 0;
  height: 46px;
  padding: 0 var(--space-6);
  font-size: var(--font-size-md);
  border-radius: var(--radius-sm);
}

.hero__examples {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
}

.hero__examples-label {
  color: var(--text-tertiary);
  font-size: var(--font-size-sm);
}

.hero__example {
  padding: 8px 16px;
  color: var(--text-secondary);
  font-size: var(--font-size-base);
  font-family: inherit;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-pill);
  cursor: pointer;
  transition:
    color var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-out),
    background var(--duration-fast) var(--ease-out),
    transform var(--duration-fast) var(--ease-out),
    box-shadow var(--duration-fast) var(--ease-out);
}

.hero__example:hover {
  color: var(--color-brand-700);
  background: var(--bg-surface);
  border-color: var(--color-brand-300);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.home-page__panels {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  /* 只占屏幕中间约 3/4，避免两大模块铺满整屏 */
  width: var(--home-panels-width);
  max-width: 100%;
  margin: 0 auto;
}

.app-section {
  width: 100%;
}

.home-page__section-search {
  width: 100%;
}

.home-page__empty-auth {
  padding: var(--space-5) 0 var(--space-2);
}

.home-page :deep(.ant-empty) {
  padding: var(--space-6) 0;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-4);
}

@media (max-width: 1280px) {
  .app-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .home-page__panels {
    width: 100%;
  }

  .app-grid {
    grid-template-columns: 1fr;
  }

  .hero {
    padding: var(--space-6) 0 var(--space-2);
  }
}

@media (max-width: 640px) {
  .home-page {
    gap: var(--space-8);
  }

  .hero__inner {
    gap: var(--space-3);
  }

  .hero__subtitle {
    font-size: var(--font-size-base);
  }

  .composer {
    padding: var(--space-3);
    border-radius: var(--radius-md);
  }

  .composer__footer {
    align-items: stretch;
    flex-direction: column;
  }

  .composer__submit {
    width: 100%;
  }
}
</style>
