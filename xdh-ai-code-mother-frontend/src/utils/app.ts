import dayjs from 'dayjs'

import { APP_DEPLOY_BASE_URL, APP_PREVIEW_BASE_URL } from '@/config/runtime'

export const DEFAULT_CODE_GEN_TYPE = 'vue'
export type AppIdentifier = string | number

export const getAppIdString = (appId?: AppIdentifier | null) => {
  if (appId === undefined || appId === null) {
    return ''
  }

  return String(appId)
}

export const toApiRequestId = (appId: AppIdentifier) => {
  return appId as unknown as number
}

export const resolveAppPreviewUrl = (app?: Partial<API.AppVO>) => {
  const appId = getAppIdString(app?.id)
  const codeGenType = app?.codeGenType
  const deployKey = app?.deployKey

  if (!appId && !deployKey) {
    return ''
  }

  if (appId && codeGenType) {
    return `${APP_PREVIEW_BASE_URL}/${codeGenType}_${appId}/`
  }

  if (deployKey) {
    return `${APP_PREVIEW_BASE_URL}/${deployKey}/`
  }

  if (appId) {
    return `${APP_PREVIEW_BASE_URL}/${DEFAULT_CODE_GEN_TYPE}_${appId}/`
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

  return `${APP_DEPLOY_BASE_URL}/${deployKey}`
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
