<template>
    <div class="login-container" @mousemove="handleMouseMove">
      <div class="bg-layer"></div>
      
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      
      <div 
        class="mouse-flashlight"
        :style="{ 
          background: `radial-gradient(600px circle at ${mouseX}px ${mouseY}px, rgba(56, 189, 248, 0.15), transparent 40%)` 
        }"
      ></div>
  
      <div 
        class="grid-layer"
        :style="{ 
          maskImage: `radial-gradient(400px circle at ${mouseX}px ${mouseY}px, black, transparent)` 
        }"
      ></div>
  
      <div class="login-content">
        <div class="glass-card">
          <div class="card-header">
            <div class="logo-circle">
              <span class="logo-emoji">🔬</span>
            </div>
            <h2 class="title">Lab201 Monitor</h2>
            <p class="subtitle">高性能计算集群监控系统</p>
          </div>
  
          <el-form 
            ref="loginFormRef"
            :model="loginForm"
            :rules="rules"
            class="login-form"
            size="large"
          >
            <el-form-item prop="username">
              <div class="input-group">
                <el-icon class="input-icon"><User /></el-icon>
                <el-input 
                  v-model="loginForm.username" 
                  placeholder="请输入账号" 
                  class="custom-input" 
                />
              </div>
            </el-form-item>
            
            <el-form-item prop="password">
              <div class="input-group">
                <el-icon class="input-icon"><Lock /></el-icon>
                <el-input 
                  v-model="loginForm.password" 
                  type="password" 
                  placeholder="请输入密码" 
                  show-password
                  class="custom-input"
                  @keyup.enter="handleLogin"
                />
              </div>
            </el-form-item>
  
            <el-button 
              type="primary" 
              :loading="loading" 
              class="submit-btn" 
              @click="handleLogin"
            >
              <span class="btn-text">{{ loading ? '系统接入中...' : '立即登录' }}</span>
              <el-icon class="btn-icon"><ArrowRight /></el-icon>
            </el-button>
  
            <div class="form-footer">
              <span>忘记密码请联系实验室管理员</span>
            </div>
          </el-form>
        </div>
        
        <div class="copyright">
          © 2025 Lab201 High Performance Computing Team
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { reactive, ref, onMounted, onUnmounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { User, Lock, ArrowRight } from '@element-plus/icons-vue'
  import { useUserStore } from '@/stores/user'
  import { ElMessage } from 'element-plus'
  
  const router = useRouter()
  const userStore = useUserStore()
  const loginFormRef = ref()
  const loading = ref(false)
  
  // 鼠标坐标状态
  const mouseX = ref(0)
  const mouseY = ref(0)
  
  const loginForm = reactive({
    username: '',
    password: ''
  })
  
  const rules = {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }
  
  // 鼠标移动事件处理
  const handleMouseMove = (e: MouseEvent) => {
    mouseX.value = e.clientX
    mouseY.value = e.clientY
  }
  
  const handleLogin = () => {
    loginFormRef.value.validate(async (valid: boolean) => {
      if (valid) {
        loading.value = true
        try {
          await userStore.login(loginForm)
          

          ElMessage.success('登录成功，正在前往知识库...')
           setTimeout(() => {
             window.location.href = '/wiki/index.html'; 
           }, 500)
        } catch (e: any) {
          console.error(e)
        } finally {
          loading.value = false
        }
      }
    })
  }
  </script>
  
  <style scoped>
  /* 1. 基础容器：深色渐变背景 */
  .login-container {
    width: 100vw;
    height: 100vh;
    background: linear-gradient(135deg, #020617 0%, #0f172a 100%);
    position: relative;
    overflow: hidden;
    display: flex;
    justify-content: center;
    align-items: center;
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  }
  
  /* 2. 漂浮能量球动画 */
  .blob {
    position: absolute;
    filter: blur(80px);
    z-index: 0;
    opacity: 0.6;
    animation: float 10s infinite ease-in-out alternate;
  }
  .blob-1 {
    top: 10%;
    left: 20%;
    width: 300px;
    height: 300px;
    background: #4f46e5; /* 靛蓝 */
    animation-delay: 0s;
  }
  .blob-2 {
    bottom: 20%;
    right: 20%;
    width: 400px;
    height: 400px;
    background: #0ea5e9; /* 天蓝 */
    animation-delay: -5s;
  }
  
  @keyframes float {
    0% { transform: translate(0, 0) scale(1); }
    100% { transform: translate(20px, -20px) scale(1.1); }
  }
  
  /* 3. 光子探照灯 (跟随鼠标) */
  .mouse-flashlight {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 1;
    pointer-events: none; /* 让鼠标事件穿透 */
    transition: opacity 0.3s;
  }
  
  /* 4. 网格层 */
  .grid-layer {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    /* 细微的网格图案 */
    background-image: linear-gradient(rgba(255, 255, 255, 0.1) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
    background-size: 30px 30px;
    z-index: 1;
    pointer-events: none;
    /* 关键：默认看不见，只在 mask (光照) 区域显示 */
    -webkit-mask-image: radial-gradient(circle at center, black, transparent); 
  }
  
  /* 5. 登录内容区 */
  .login-content {
    z-index: 10;
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
  }
  
  /* 玻璃拟态卡片 */
  .glass-card {
    width: 420px;
    padding: 40px;
    background: rgba(15, 23, 42, 0.6); /* 深色半透明 */
    backdrop-filter: blur(20px);        /* 毛玻璃 */
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-top: 1px solid rgba(255, 255, 255, 0.15); /* 顶部高光 */
    border-radius: 24px;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
    transform: translateY(0);
    transition: transform 0.3s, box-shadow 0.3s;
  }
  
  .glass-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 35px 60px -15px rgba(0, 0, 0, 0.6);
    border-color: rgba(56, 189, 248, 0.3); /* 悬停微发光 */
  }
  
  /* 头部设计 */
  .card-header {
    text-align: center;
    margin-bottom: 32px;
  }
  
  .logo-circle {
    width: 64px;
    height: 64px;
    margin: 0 auto 16px;
    background: linear-gradient(135deg, #1e293b, #0f172a);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid rgba(255,255,255,0.1);
    box-shadow: 0 0 20px rgba(56, 189, 248, 0.2);
  }
  
  .logo-emoji {
    font-size: 32px;
  }
  
  .title {
    color: #fff;
    font-size: 26px;
    font-weight: 700;
    margin: 0;
    letter-spacing: 0.5px;
  }
  
  .subtitle {
    color: #94a3b8;
    font-size: 14px;
    margin-top: 8px;
  }
  
  /* 输入框组设计 */
  .input-group {
    position: relative;
    display: flex;
    align-items: center;
  }
  
  .input-icon {
    position: absolute;
    left: 12px;
    z-index: 2;
    color: #94a3b8;
    font-size: 18px;
  }
  
  :deep(.custom-input .el-input__wrapper) {
    background-color: rgba(30, 41, 59, 0.5);
    box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    padding-left: 35px; /* 让出图标位置 */
    height: 48px;
    transition: all 0.3s;
  }
  
  :deep(.custom-input .el-input__wrapper:hover) {
    box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.2);
  }
  
  :deep(.custom-input .el-input__wrapper.is-focus) {
    background-color: rgba(30, 41, 59, 0.8);
    box-shadow: 0 0 0 2px #38bdf8; /* 聚焦高亮 */
  }
  
  :deep(.el-input__inner) {
    color: #fff;
    height: 48px;
  }
  
  /* 登录按钮 */
  .submit-btn {
    width: 100%;
    height: 50px;
    border-radius: 12px;
    background: linear-gradient(90deg, #0ea5e9, #3b82f6);
    border: none;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 1px;
    margin-top: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    transition: all 0.3s;
  }
  
  .submit-btn:hover {
    background: linear-gradient(90deg, #0284c7, #2563eb);
    transform: scale(1.02);
    box-shadow: 0 0 20px rgba(14, 165, 233, 0.4);
  }
  
  .submit-btn:active {
    transform: scale(0.98);
  }
  
  /* 底部版权 */
  .form-footer {
    margin-top: 20px;
    text-align: center;
    color: #64748b;
    font-size: 13px;
    cursor: pointer;
    transition: color 0.3s;
  }
  .form-footer:hover {
    color: #94a3b8;
  }
  
  .copyright {
    color: #475569;
    font-size: 12px;
    margin-top: 20px;
  }
  </style>