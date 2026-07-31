import dayjs from 'dayjs'

export const API_BASE_URL = 'http://localhost:8123/api'
export const APP_PAGE_SIZE = 20
export const DEFAULT_CODE_GEN_TYPE = 'vue'
export type AppIdentifier = string | number

export const getAppIdString = (appId?: AppIdentifier | null) => {
  if (appId === undefined || appId === null) {
    return ''
  }

  return String(appId)
}

export const resolveAppPreviewUrl = (
  app?: Partial<API.AppVO>,
  options?: { preferDeployKey?: boolean }
) => {
  const appId = getAppIdString(app?.id)
  const codeGenType = app?.codeGenType
  const deployKey = app?.deployKey

  if (!appId && !deployKey) {
    return ''
  }

  if (options?.preferDeployKey && deployKey) {
    return `${API_BASE_URL}/static/${deployKey}/`
  }

  if (appId && codeGenType) {
    return `${API_BASE_URL}/static/${codeGenType}_${appId}/`
  }

  if (deployKey) {
    return `${API_BASE_URL}/static/${deployKey}/`
  }

  if (appId) {
    return `${API_BASE_URL}/static/${DEFAULT_CODE_GEN_TYPE}_${appId}/`
  }

  return ''
}

export const resolveAppDeployUrl = (app?: Partial<API.AppVO>) => {
  const deployKey = getAppIdString(app?.deployKey)
  if (!deployKey) {
    return ''
  }

  if (/^https?:\/\//i.test(deployKey)) {
    return deployKey
  }

  return `http://localhost/${deployKey}`
}

export const formatAppRelativeTime = (time?: string) => {
  if (!time) {
    return '刚刚创建'
  }

  return `创建于 ${dayjs(time).fromNow()}`
}

export const formatDateTime = (time?: string) => {
  if (!time) {
    return '-'
  }

  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

export const getAppDisplayName = (app?: Partial<API.AppVO>) => {
  return app?.appName?.trim() || '未命名应用'
}

export const getAppAuthorName = (app?: Partial<API.AppVO>) => {
  return app?.user?.userName?.trim() || '无名'
}
