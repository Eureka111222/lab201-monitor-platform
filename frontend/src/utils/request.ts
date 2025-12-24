import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios'
import type { ApiResponse } from '@/api/types'

const instance = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
})

instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const payload = response.data
    if (payload && typeof payload.code === 'number') {
      if (payload.code === 200) {
        return payload.data as unknown
      }
      const message = payload.message || '请求失败'
      return Promise.reject(new Error(message))
    }
    return response.data as any
  },
  (error) => {
    return Promise.reject(error)
  }
)

export const request = <T = unknown>(config: AxiosRequestConfig) => {
  return instance.request<any, T>(config)
}

export default instance

