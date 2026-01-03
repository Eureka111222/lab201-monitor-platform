import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'

// 👇👇👇 关键点：这里加上 export 关键字，BasicLayout 才能引用到
export const routes: RouteRecordRaw[] = [
    // 在 routes 数组最上面添加
  {
    path: '/login',
    name: 'Login',
    meta: { hidden: true }, // 不在菜单显示
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/',
    component: BasicLayout,
    redirect: '/dashboard',
    children: [
      {
        // 🚨 修改点 1：path 前面加上 /，变成绝对路径
        path: '/dashboard',
        name: 'Dashboard',
        meta: { title: 'GPU 实时监控', icon: '📊' }, 
        component: () => import('@/views/dashboard/index.vue'),
      },
      {
        // 🚨 修改点 2：path 前面加上 /
        path: '/server',
        name: 'ServerList',
        meta: { title: '服务器列表', icon: '🖥️' },
        component: () => import('@/views/server/list.vue'),
      },
      {
        // 🚨 修改点 3：path 前面加上 /
        path: '/server/detail/:serverName',
        name: 'ServerDetail',
        // activeMenu 指定高亮 '/server'，因为现在菜单 index 也是 '/server' 了，正好匹配
        meta: { title: '历史详情', hidden: true, activeMenu: '/server' },
        component: () => import('@/views/server/detail.vue'),
      },
      {
        // 🚨 修改点 4：path 前面加上 /
        path: '/sys/user',
        name: 'SysUser',
        meta: { title: '用户管理', icon: '👥' },
        component: () => import('@/views/sys/user/index.vue'), 
      },
      {
        // 🚨 修改点 5：path 前面加上 /
        path: '/sys/document',
        name: 'SysDocument',
        meta: { title: '知识库', icon: '📚' },
        component: () => import('@/views/sys/document/index.vue'),
      },
      {
        path: '/wiki',  // 这里的路径即为点击后菜单的索引
        name: 'Wiki',
        component: () => import('@/views/server/guide.vue'), // 随便填一个组件，反正不会真的渲染，因为我们会拦截跳转

        meta: { 
          title: '指导手册', 
          icon: 'Guide'   // 图标名称，稍后在 Layout 里映射
        }
      }
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router