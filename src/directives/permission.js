import { usePermission } from '@/utils/permissionStore'

/**
 * v-perm="'finance:withdraw:verify'" — 无权限时移除 DOM
 * v-perm="['order:ship', 'order:refund']" — 任一权限即可显示
 */
export const permDirective = {
  mounted(el, binding) {
    updatePerm(el, binding)
  },
  updated(el, binding) {
    updatePerm(el, binding)
  },
}

function updatePerm(el, binding) {
  const { hasPerm, hasAnyPerm } = usePermission()
  const value = binding.value
  let visible = true
  if (Array.isArray(value)) {
    visible = hasAnyPerm(value)
  } else if (value) {
    visible = hasPerm(value)
  }
  el.style.display = visible ? '' : 'none'
  el.dataset.permHidden = visible ? '0' : '1'
}
