const BASE_URL = '' // 联调时填写 Spring Boot 网关地址，如 https://api.example.com

/**
 * 封装 uni.request
 * @param {object} options
 */
export function request(options) {
  const token = uni.getStorageSync('token') || ''
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
        ...options.header,
      },
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          reject(res)
        }
      },
      fail: reject,
    })
  })
}
