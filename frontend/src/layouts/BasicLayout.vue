<script setup lang="ts">
// 引入 AI 助手组件
import AiAssistant from '@/components/AiAssistant/index.vue'

import { computed } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import { routes } from '@/router'
import { useUserStore } from '@/stores/user'
// 引入图标
import { Monitor, DataBoard, User, Document, SwitchButton, Reading } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 1. 图标映射表
const iconMap: Record<string, any> = {
  Monitor: Monitor,
  DataBoard: DataBoard,
  User: User,
  Document: Document,
  Guide: Reading,
  '📊': Monitor,
  '🖥️': DataBoard,
  '👥': User,
  '📚': Document
}

// 2. 菜单权限过滤
const menuRoutes = computed(() => {
  const root = routes.find((item) => item.path === '/')
  const allRoutes = root?.children || []
  return allRoutes.filter((child) => {
    if (child.meta?.hidden) return false
    if (child.name === 'SysUser') return userStore.role === 'admin'
    return true
  })
})

const activeMenu = computed(() => {
  if (route.meta?.activeMenu) return route.meta.activeMenu as string
  if (route.meta?.hidden && route.path.startsWith('/server/detail')) return '/server'
  return route.path
})

// 3. 退出登录逻辑
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  })
}

const handleMenuClick = (item: any) => {
  if (item.path === '/wiki') {
    window.location.href = '/wiki/index.html'
  }
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="240px" class="layout__aside">
      <div class="layout__logo">
        <span class="logo-text">🔬 Lab201 Monitor</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        router
        class="layout__menu"
        text-color="#94a3b8"
        active-text-color="#fff"
        background-color="transparent"
      >
        <el-menu-item v-for="item in menuRoutes" :key="item.path" :index="item.path" @click="handleMenuClick(item)">
          <el-icon class="menu-icon">
            <component :is="iconMap[item.meta?.icon as string] || Document" />
          </el-icon>
          <span>{{ item.meta?.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout__header">
        <div class="header-left">
          <span class="page-title">{{ route.meta.title }}</span>
        </div>
        <div class="header-right">
          <el-tag 
            :type="userStore.role === 'admin' ? 'danger' : 'success'" 
            effect="dark" 
            round 
            class="role-tag"
          >
            {{ userStore.role === 'admin' ? '管理员' : '普通用户' }}
          </el-tag>
          
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" :icon="User" class="user-avatar" />
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username || '用户' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout__content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>

  <AiAssistant />
</template>

<style scoped>
/* 你的样式保持不变，不需要修改 */
.layout {
  min-height: 100vh;
}
.layout__aside {
  background-color: #0f172a;
  border-right: 1px solid #1e293b;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  box-shadow: 2px 0 8px rgba(0,0,0,0.1);
}
.layout__logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0b1120;
  border-bottom: 1px solid #1e293b;
}
.logo-text {
  font-size: 18px;
  font-weight: 800;
  background: linear-gradient(to right, #38bdf8, #818cf8);
  -webkit-background-clip: text;
  color: transparent;
  letter-spacing: 1px;
}
.layout__menu {
  border-right: none;
  padding-top: 10px;
}
:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: 8px;
  border: 1px solid transparent;
}
:deep(.el-menu-item:hover) {
  background-color: rgba(30, 41, 59, 0.8);
  color: #fff;
}
:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(56, 189, 248, 0.15) 0%, rgba(56, 189, 248, 0) 100%);
  color: #38bdf8;
  font-weight: 600;
  border-left: 3px solid #38bdf8;
  border-top-left-radius: 2px;
  border-bottom-left-radius: 2px;
}
.menu-icon {
  margin-right: 10px;
  font-size: 18px;
}
.layout__header {
  height: 64px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}
.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #334155;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}
.user-info:hover {
  background: #f1f5f9;
}
.username {
  font-size: 14px;
  color: #475569;
  font-weight: 500;
}
.role-tag {
  font-weight: bold;
}
.layout__content {
  padding: 24px;
  background: #f8fafc;
  overflow-x: hidden;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>