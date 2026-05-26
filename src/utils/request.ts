import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { clearToken, getToken } from '@/utils/auth'

export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

const request = axios.create({
  baseURL: '',
  timeout: 20000,
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult
    if (body.code !== 200) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body.data
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常'
    if (status === 401) {
      clearToken()
      ElMessage.error('登录已过期，请重新登录')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  },
)

export function get<T = unknown>(url: string, config?: AxiosRequestConfig) {
  return request.get<T, T>(url, config)
}

export function post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.post<T, T>(url, data, config)
}

export function put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  return request.put<T, T>(url, data, config)
}

export function del<T = unknown>(url: string, config?: AxiosRequestConfig) {
  return request.delete<T, T>(url, config)
}

export default request
