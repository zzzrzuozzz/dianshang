import { reactive, ref, type Ref } from 'vue'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import {
  createEmptyPushForm,
  fetchNotificationDetail,
  type PushForm,
} from '@/api/ops'

export function applyNotificationDetail(form: PushForm, detail: PushForm & { notifyCode?: string }) {
  form.notifyCode = detail.notifyCode
  form.category = detail.category || ''
  form.title = detail.title || ''
  form.intro = detail.intro || ''
  form.jumpType = detail.jumpType || 'TEXT'
  form.jumpUrl = detail.jumpUrl || ''
  form.innerLinkType = detail.innerLinkType || 'custom'
  form.detail = detail.detail || ''
  form.coverImages = detail.coverImages ? [...detail.coverImages] : []
  form.memberLevels = detail.memberLevels?.length ? [...detail.memberLevels] : ['all']
  form.regions = detail.regions ? [...detail.regions] : []
  form.tags = detail.tags ? { ...detail.tags } : { ...createEmptyPushForm().tags }
  form.sendType = detail.sendType ?? 1
  form.publishTime = detail.publishTime || ''
  form.generateTypes = detail.generateTypes ? [...detail.generateTypes] : []
  form.appPush = Boolean(detail.appPush)
  form.estimatedUsers = detail.estimatedUsers ?? 0
  form.pushMethod = detail.pushMethod?.length ? [...detail.pushMethod] : ['tag']
  form.stationContent = detail.stationContent || ''
  form.smsContent = detail.smsContent || ''
}

export function applyRoutePreset(form: PushForm, route: RouteLocationNormalizedLoaded) {
  const { jumpType, innerLinkType, pushMethod } = route.query
  if (jumpType && typeof jumpType === 'string') {
    form.jumpType = jumpType
  }
  if (innerLinkType && typeof innerLinkType === 'string') {
    form.innerLinkType = innerLinkType
    form.jumpType = 'INNER'
  }
  if (pushMethod && typeof pushMethod === 'string') {
    form.pushMethod = [pushMethod]
  }
}

export function useNotificationEditor(notifyCode: Ref<string | undefined>) {
  const form = reactive(createEmptyPushForm())
  const loading = ref(false)

  const loadForEdit = async () => {
    const code = notifyCode.value
    if (!code) return
    loading.value = true
    try {
      const detail = await fetchNotificationDetail(code)
      applyNotificationDetail(form, detail)
    } finally {
      loading.value = false
    }
  }

  return { form, loading, loadForEdit }
}
