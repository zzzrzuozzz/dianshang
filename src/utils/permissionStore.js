import { ref, computed } from 'vue'

const MENUS_KEY = 'admin_menus'
const PERMS_KEY = 'admin_perms'
const ROLES_KEY = 'admin_roles'

/** 无需菜单授权即可访问的路由 */
export const PUBLIC_ROUTE_PREFIXES = ['/dashboard', '/profile', '/redirect', '/message']

/** 详情页映射到列表菜单路径，便于权限继承 */
const DETAIL_ROUTE_BASE = [
  { prefix: '/order/detail', base: '/order/list' },
  { prefix: '/user/detail', base: '/user/list' },
  { prefix: '/user/edit', base: '/user/list' },
  { prefix: '/product/add', base: '/product/list' },
  { prefix: '/promotion/', base: '/promotion/seckill' },
  { prefix: '/content/topic/', base: '/content/topic' },
  { prefix: '/content/help/', base: '/content/help' },
  { prefix: '/ops/', base: '/ops/advertisement' },
]

const menus = ref([])
const perms = ref([])
const roleKeys = ref([])

function loadFromStorage() {
  try {
    menus.value = JSON.parse(localStorage.getItem(MENUS_KEY) || '[]')
    perms.value = JSON.parse(localStorage.getItem(PERMS_KEY) || '[]')
    roleKeys.value = JSON.parse(localStorage.getItem(ROLES_KEY) || '[]')
  } catch {
    menus.value = []
    perms.value = []
    roleKeys.value = []
  }
}

loadFromStorage()

export function setPermissionSession({ menus: m, perms: p, roleKeys: r }) {
  menus.value = m || []
  perms.value = p || []
  roleKeys.value = r || []
  localStorage.setItem(MENUS_KEY, JSON.stringify(menus.value))
  localStorage.setItem(PERMS_KEY, JSON.stringify(perms.value))
  localStorage.setItem(ROLES_KEY, JSON.stringify(roleKeys.value))
}

export function clearPermissionSession() {
  menus.value = []
  perms.value = []
  roleKeys.value = []
  localStorage.removeItem(MENUS_KEY)
  localStorage.removeItem(PERMS_KEY)
  localStorage.removeItem(ROLES_KEY)
}

export function flattenMenuPaths(nodes, paths = []) {
  ;(nodes || []).forEach((n) => {
    if (n.path) paths.push(normalizePath(n.path))
    if (n.children?.length) flattenMenuPaths(n.children, paths)
  })
  return paths
}

export function normalizePath(path) {
  if (!path) return ''
  const p = path.startsWith('/') ? path : `/${path}`
  return p.length > 1 && p.endsWith('/') ? p.slice(0, -1) : p
}

export function canAccessPath(path, { isSuperAdmin, allowedPaths }) {
  const target = normalizePath(path)
  if (isSuperAdmin) return true
  if (PUBLIC_ROUTE_PREFIXES.some((p) => target === p || target.startsWith(`${p}/`))) {
    return true
  }
  const allowed = allowedPaths || []
  if (allowed.some((p) => target === p || target.startsWith(`${p}/`))) {
    return true
  }
  for (const rule of DETAIL_ROUTE_BASE) {
    if (target.startsWith(rule.prefix)) {
      const bases = Array.isArray(rule.base) ? rule.base : [rule.base]
      if (bases.some((b) => allowed.includes(normalizePath(b)))) return true
    }
  }
  return false
}

export function usePermission() {
  const isSuperAdmin = computed(() => roleKeys.value.includes('admin'))

  const allowedPaths = computed(() => {
    if (isSuperAdmin.value) return null
    return flattenMenuPaths(menus.value)
  })

  const hasPerm = (perm) => {
    if (!perm) return true
    if (isSuperAdmin.value) return true
    return perms.value.includes(perm)
  }

  const hasAnyPerm = (list) => {
    if (!list?.length) return true
    return list.some((p) => hasPerm(p))
  }

  const checkPath = (path) =>
    canAccessPath(path, {
      isSuperAdmin: isSuperAdmin.value,
      allowedPaths: allowedPaths.value,
    })

  return {
    menus,
    perms,
    roleKeys,
    isSuperAdmin,
    allowedPaths,
    hasPerm,
    hasAnyPerm,
    canAccessPath: checkPath,
    setPermissionSession,
    clearPermissionSession,
    flattenMenuPaths,
  }
}
