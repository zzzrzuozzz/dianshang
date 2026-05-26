/** Mock 管理员资料 —— 联调后由 GET /api/system/user/profile 返回 */

let mockProfile = {
  username: 'admin',
  nickname: '暴走管理员',
  phone: '13066660000',
  email: 'admin@baozou.com',
  gender: 'male',
  avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
  roleName: '超级管理员',
  createTime: '2024-01-15 10:30:00',
}

export function getMockProfile() {
  return { ...mockProfile }
}

export function updateMockProfile(payload) {
  mockProfile = { ...mockProfile, ...payload }
  return getMockProfile()
}

/** Mock 密码校验：旧密码 admin123 */
export function updateMockPassword(oldPassword, newPassword) {
  if (oldPassword !== 'admin123') {
    const err = new Error('旧密码不正确')
    err.code = 'OLD_PWD_WRONG'
    throw err
  }
  if (!newPassword || newPassword.length < 6) {
    throw new Error('新密码格式不正确')
  }
  return true
}
