import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi } from '@/api/sysUser'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>('')
  const userInfo = ref<any>(null)
  const role = ref<'admin' | 'user'>('user')

  const login = async (form: any) => {
    // 1. 调用后端登录接口
    // 后端返回 Result<Map<String, Object>>
    // res.data = { token: "eyJhb...", userInfo: { ... } }
    const res: any = await loginApi(form)
    console.log("【调试】后端返回的真实数据:", res)
    
    // 2. 解析数据
    // 注意：request.ts 里的拦截器如果已经把 response.data.data 提取出来了，
    // 这里 res 就是那个 Map 对象
    const data = res 
    
    if (data.token) {
      token.value = data.token          // 存 Token
      userInfo.value = data.userInfo    // 存 UserInfo
      role.value = data.userInfo.role   // 存 Role
      localStorage.setItem('user', JSON.stringify({
        token: data.token,
        userInfo: data.userInfo,
        role: role.value
      }))
    } else {
      // 兼容旧逻辑或者报错
      console.error("登录返回格式错误", data)
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    role.value = 'user'
    localStorage.removeItem('user') // 清理持久化
  }

  return { token, userInfo, role, login, logout }
}, {
  persist: true // 开启持久化
})