import { get } from '@/utils/request'

export interface RegionItem {
  code: string
  name: string
  level: number
  parentCode?: string
  hasChildren?: boolean
}

/** GET /api/system/region/list-by-parent — 省市区懒加载 */
export function fetchRegionsByParent(parentCode = '0') {
  return get<RegionItem[]>('/api/system/region/list-by-parent', {
    params: { parentCode },
  })
}
