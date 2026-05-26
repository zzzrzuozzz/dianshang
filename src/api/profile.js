import { get, put } from '@/utils/request'

/**
 * GET /api/system/user/profile
 * 用户身份由 Authorization 头解析，禁止前端传 userId
 */
export function getProfile() {
  return get('/api/system/user/profile')
}

/** PUT /api/system/user/profile */
export function updateProfile(data) {
  return put('/api/system/user/profile', data)
}

/** PUT /api/system/user/profile/updatePwd */
export function updatePassword({ oldPassword, newPassword }) {
  return put('/api/system/user/profile/updatePwd', { oldPassword, newPassword })
}
