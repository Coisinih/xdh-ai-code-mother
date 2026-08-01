export const openInNewTab = (url?: string | null) => {
  if (!url) {
    return
  }

  window.open(url, '_blank', 'noopener,noreferrer')
}
