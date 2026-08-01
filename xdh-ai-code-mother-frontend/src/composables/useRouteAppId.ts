import { computed } from 'vue'
import { useRoute } from 'vue-router'

export const useRouteAppId = () => {
  const route = useRoute()

  return computed(() => {
    const routeId = route.params.id
    if (Array.isArray(routeId)) {
      return routeId[0] ?? ''
    }

    return typeof routeId === 'string' ? routeId : ''
  })
}
