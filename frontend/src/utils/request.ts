import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios'
import type { ApiResponse } from '@/api/types'
import { useUserStore } from '@/stores/user'
import router from '@/router' // 🔥 1. 引入路由实例
import { ElMessage } from 'element-plus'

const instance = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
})

// 🔥 封装统一的过期处理函数
const handleLoginExpired = () => {
  const userStore = useUserStore()
  // 1. 提示用户
  ElMessage.error('登录状态已过期，请重新登录')
  // 2. 清理 Token
  userStore.logout()
  // 3. 强制跳转回登录页 (带上 redirect 参数，登录后跳回当前页)
  // 注意：router.currentRoute.value.fullPath 获取当前页面路径
  const currentPath = router.currentRoute.value.fullPath
  router.push(`/login?redirect=${encodeURIComponent(currentPath)}`)
}

instance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const payload = response.data
    // 处理业务状态码
    if (payload && typeof payload.code === 'number') {
      // 成功
      if (payload.code === 200) {
        return payload.data as unknown
      }
      
      // 🔥🔥🔥 核心修复 1：拦截业务逻辑层面的 401 🔥🔥🔥
      if (payload.code === 401) {
        handleLoginExpired()
        // 返回一个 pending 的 promise 中断后续链式调用
        return Promise.reject(new Error('Token Expired'))
      }

      const message = payload.message || '请求失败'
      // 这里的 ElMessage.error 可以根据需要决定是否要弹窗
      ElMessage.error(message) 
      return Promise.reject(new Error(message))
    }
    return response.data as any
  },
  (error) => {
    // 🔥🔥🔥 核心修复 2：拦截 HTTP 协议层面的 401 🔥🔥🔥
    if (error.response && error.response.status === 401) {
        handleLoginExpired()
    } else {
        ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

// ... request 拦截器保持不变 ...
instance.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['access-token'] = userStore.token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

export const request = <T = unknown>(config: AxiosRequestConfig) => {
  return instance.request<any, T>(config)
}

export default instance