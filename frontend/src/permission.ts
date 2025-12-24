import router from '@/router'
import { useUserStore } from '@/stores/user'
import NProgress from 'nprogress' // 进度条支持
import 'nprogress/nprogress.css'

// 进度条配置
NProgress.configure({ showSpinner: false })

const whiteList = ['/login'] // 白名单：不需要登录就能访问的页面

router.beforeEach((to, from, next) => {
  // 开启进度条
  NProgress.start()
  
  // ⚠️ 注意：这里必须在 beforeEach 内部获取 store，否则会报错
  const userStore = useUserStore()
  
  // 判断是否有 Token (或者判断 userInfo 是否存在)
  // 假设你的 store 里登录成功后会设置 token 或 role
  const hasLogin = userStore.userInfo && userStore.userInfo.username

  if (hasLogin) {
    if (to.path === '/login') {
      // 如果已登录，又去访问登录页，强制跳到首页
      next({ path: '/' })
      NProgress.done()
    } else {
      // 已登录，去其他页面 -> 放行
      next()
    }
  } else {
    // 未登录
    if (whiteList.indexOf(to.path) !== -1) {
      // 如果去的是白名单（登录页），放行
      next()
    } else {
      // 否则全部踢回登录页，并带上 redirect 参数，以便登录后跳回来
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  // 关闭进度条
  NProgress.done()
})