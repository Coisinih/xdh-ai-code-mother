const HOME_REFRESH_FLAG_KEY = 'home:refreshNeeded'

export const markHomeRefreshNeeded = () => {
  sessionStorage.setItem(HOME_REFRESH_FLAG_KEY, '1')
}

export const consumeHomeRefreshNeeded = () => {
  const refreshNeeded = sessionStorage.getItem(HOME_REFRESH_FLAG_KEY) === '1'
  if (refreshNeeded) {
    sessionStorage.removeItem(HOME_REFRESH_FLAG_KEY)
  }
  return refreshNeeded
}
