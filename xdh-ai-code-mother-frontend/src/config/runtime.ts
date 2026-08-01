const trimTrailingSlash = (value: string) => value.replace(/\/+$/, '')

const resolveRuntimeValue = (value: string | undefined, fallback: string) => {
  return trimTrailingSlash(value?.trim() || fallback)
}

export const API_BASE_URL = resolveRuntimeValue(
  import.meta.env.VITE_API_BASE_URL,
  'http://localhost:8123/api',
)

export const APP_PREVIEW_BASE_URL = resolveRuntimeValue(
  import.meta.env.VITE_APP_PREVIEW_BASE_URL,
  `${API_BASE_URL}/static`,
)

export const APP_DEPLOY_BASE_URL = resolveRuntimeValue(
  import.meta.env.VITE_APP_DEPLOY_BASE_URL,
  'http://localhost',
)
