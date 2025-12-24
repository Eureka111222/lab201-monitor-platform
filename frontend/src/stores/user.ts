import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi } from '@/api/sysUser' // 引入 API

export const useUserStore = defineStore('user', () => {
  const token = ref<string>('')
  const userInfo = ref<any>(null)
  const role = ref<'admin' | 'user'>('user') // 默认 user

  // 真实登录逻辑
  const login = async (form: any) => {
    // 1. 发送请求给后端
    const res: any = await loginApi(form)
    
    // 2. 后端返回了 user 对象 (包含 role)
    // 注意：具体结构取决于你后端的 Result 封装
    const user = res // 或者 res.data
    
    // 3. 存入状态
    userInfo.value = user
    role.value = user.role || 'user' // 获取数据库里的 role
    
    // (可选) 存 token
    // token.value = user.token 
  }

  const logout = () => {
    userInfo.value = null
    role.value = 'user'
  }

  return { userInfo, role, login, logout }
}, {
  persist: true // 👈👈👈 只需要加这一行，神奇的事情就发生了！
})