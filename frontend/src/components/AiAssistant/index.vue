<template>
  <div class="ai-assistant-wrapper">
    <transition name="el-zoom-in-bottom">
      <div 
        v-show="isVisible" 
        class="chat-window-container"
        :style="{ 
          left: windowPos.x + 'px', 
          top: windowPos.y + 'px',
          width: windowSize.w + 'px',
          height: windowSize.h + 'px'
        }"
      >
        <el-card class="chat-card" shadow="always">
          <template #header>
            <div class="chat-header" @mousedown="startDragWindow">
              <span class="header-title">
                <el-icon><Service /></el-icon> 实验室智能助手
              </span>
              <div class="header-actions">
                <el-button link @click="isVisible = false">
                  <el-icon><Minus /></el-icon>
                </el-button>
              </div>
            </div>
          </template>

          <div class="message-list" ref="msgListRef">
            <div v-if="messages.length === 0" class="empty-tip">
              👋 你好！我是 Lab201 智能助手。<br/>
              我可以回答关于实验室安全、WIFI密码、设备使用等问题。
            </div>
            
            <div 
              v-for="(msg, index) in messages" 
              :key="index" 
              class="message-item"
              :class="msg.role === 'user' ? 'message-user' : 'message-ai'"
            >
              <div class="avatar">
                <el-avatar :size="30" :icon="msg.role === 'user' ? 'UserFilled' : 'Service'" :style="{ backgroundColor: msg.role === 'user' ? '#409EFF' : '#67C23A' }" />
              </div>
              <div class="content-bubble">
                <div v-if="msg.role === 'ai'" class="markdown-body" v-html="renderMarkdown(msg.content)"></div>
                <div v-else style="white-space: pre-wrap;">{{ msg.content }}</div>
                <div v-if="msg.loading" class="typing-cursor"></div>
              </div>
            </div>
          </div>

          <div class="input-area">
            <el-input
              v-model="inputQuery"
              placeholder="请输入问题..."
              @keyup.enter="handleSend"
              :disabled="isGenerating"
            >
              <template #append>
                <el-button @click="handleSend" :loading="isGenerating">
                  <el-icon><Position /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </el-card>

        <div class="resizer top" @mousedown.stop.prevent="startResize('n', $event)"></div>
        <div class="resizer bottom" @mousedown.stop.prevent="startResize('s', $event)"></div>
        <div class="resizer left" @mousedown.stop.prevent="startResize('w', $event)"></div>
        <div class="resizer right" @mousedown.stop.prevent="startResize('e', $event)"></div>
        <div class="resizer top-left" @mousedown.stop.prevent="startResize('nw', $event)"></div>
        <div class="resizer top-right" @mousedown.stop.prevent="startResize('ne', $event)"></div>
        <div class="resizer bottom-left" @mousedown.stop.prevent="startResize('sw', $event)"></div>
        <div class="resizer bottom-right" @mousedown.stop.prevent="startResize('se', $event)">
           <el-icon class="icon-resize"><DCaret /></el-icon>
        </div>
      </div>
    </transition>

    <div 
      v-show="!isVisible" 
      class="floating-ball" 
      :style="{ left: ballPos.x + 'px', top: ballPos.y + 'px' }"
      @mousedown="startDragBall"
      @click="handleBallClick"
    >
      <el-icon size="30" color="#fff"><ChatDotRound /></el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Service, Minus, UserFilled, Position, ChatDotRound, DCaret } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import MarkdownIt from 'markdown-it'

const router = useRouter()
const userStore = useUserStore()
const md = new MarkdownIt({ html: true, linkify: true, breaks: true })

const isVisible = ref(false)
const inputQuery = ref('')
const isGenerating = ref(false)
const msgListRef = ref<HTMLElement>()

interface ChatMessage {
  role: 'user' | 'ai'
  content: string
  loading?: boolean 
}
const messages = ref<ChatMessage[]>([])

// === 1. 悬浮球拖拽 ===
const ballPos = ref({ x: window.innerWidth - 100, y: window.innerHeight - 100 })
let isBallDragging = false
let ballDragStartTime = 0

const startDragBall = (e: MouseEvent) => {
  isBallDragging = false
  ballDragStartTime = Date.now()
  const startX = e.clientX
  const startY = e.clientY
  const initialLeft = ballPos.value.x
  const initialTop = ballPos.value.y

  const onMouseMove = (moveEvent: MouseEvent) => {
    isBallDragging = true
    const dx = moveEvent.clientX - startX
    const dy = moveEvent.clientY - startY
    let newX = initialLeft + dx
    let newY = initialTop + dy
    newX = Math.max(0, Math.min(window.innerWidth - 60, newX))
    newY = Math.max(0, Math.min(window.innerHeight - 60, newY))
    ballPos.value = { x: newX, y: newY }
  }

  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

const handleBallClick = () => {
  const dragDuration = Date.now() - ballDragStartTime
  if (!isBallDragging || dragDuration < 200) {
    isVisible.value = true
    if (windowPos.value.x === 0 && windowPos.value.y === 0) {
        windowPos.value = { 
            x: window.innerWidth - windowSize.value.w - 50, 
            y: window.innerHeight - windowSize.value.h - 50 
        }
    }
  }
}

// === 2. 窗口位置 & 大小状态 ===
const windowPos = ref({ x: 0, y: 0 })
const windowSize = ref({ w: 400, h: 550 })
const MIN_W = 300
const MIN_H = 400

// === 3. 窗口移动逻辑 (Header) ===
const startDragWindow = (e: MouseEvent) => {
  const startX = e.clientX
  const startY = e.clientY
  const initialLeft = windowPos.value.x
  const initialTop = windowPos.value.y

  const onMouseMove = (moveEvent: MouseEvent) => {
    const dx = moveEvent.clientX - startX
    const dy = moveEvent.clientY - startY
    windowPos.value = { x: initialLeft + dx, y: initialTop + dy }
  }

  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

// === 4. 全方位缩放逻辑 (8个方向) ===
// dir: 'n', 's', 'w', 'e', 'nw', 'ne', 'sw', 'se'
const startResize = (dir: string, e: MouseEvent) => {
  const startX = e.clientX
  const startY = e.clientY
  const startW = windowSize.value.w
  const startH = windowSize.value.h
  const startLeft = windowPos.value.x
  const startTop = windowPos.value.y

  const onMouseMove = (moveEvent: MouseEvent) => {
    const dx = moveEvent.clientX - startX
    const dy = moveEvent.clientY - startY

    let newW = startW
    let newH = startH
    let newLeft = startLeft
    let newTop = startTop

    // 东西方向 (调整宽度)
    if (dir.includes('e')) { // 向右拉：改宽
      newW = Math.max(MIN_W, startW + dx)
    } else if (dir.includes('w')) { // 向左拉：改宽 + 改left
      const potentialW = startW - dx
      if (potentialW >= MIN_W) {
        newW = potentialW
        newLeft = startLeft + dx
      }
    }

    // 南北方向 (调整高度)
    if (dir.includes('s')) { // 向下拉：改高
      newH = Math.max(MIN_H, startH + dy)
    } else if (dir.includes('n')) { // 向上拉：改高 + 改top
      const potentialH = startH - dy
      if (potentialH >= MIN_H) {
        newH = potentialH
        newTop = startTop + dy
      }
    }

    windowSize.value = { w: newW, h: newH }
    windowPos.value = { x: newLeft, y: newTop }
  }

  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }

  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

const renderMarkdown = (text: string) => md.render(text)
const scrollToBottom = () => {
  nextTick(() => {
    if (msgListRef.value) {
      msgListRef.value.scrollTop = msgListRef.value.scrollHeight
    }
  })
}

const handleSend = async () => {
  const query = inputQuery.value.trim()
  if (!query || isGenerating.value) return

  messages.value.push({ role: 'user', content: query })
  inputQuery.value = ''
  scrollToBottom()

  const aiMsgIndex = messages.value.push({ role: 'ai', content: '', loading: true }) - 1
  isGenerating.value = true

  try {
    const token = userStore.token
    if (!token) {
      messages.value[aiMsgIndex].content = "请先登录后再使用助手。"
      return
    }

    const response = await fetch(`/api/v1/rag/stream?query=${encodeURIComponent(query)}`, {
      method: 'GET',
      headers: { 'access-token': token, 'Accept': 'text/event-stream' }
    })

    if (response.status === 401 || response.status === 406) {
       userStore.logout()
       router.push(`/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`)
       throw new Error("登录过期")
    }

    if (!response.ok) throw new Error(response.statusText)
    if (!response.body) throw new Error('No response body')

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      const chunk = decoder.decode(value, { stream: true })
      buffer += chunk
      let lines = buffer.split('\n')
      buffer = lines.pop() || '' 

      for (const line of lines) {
        const trimmedLine = line.trim()
        if (!trimmedLine) continue 
        if (trimmedLine.startsWith('data:')) {
            const data = trimmedLine.substring(5)
            const content = data.startsWith(' ') ? data.substring(1) : data;
            messages.value[aiMsgIndex].content += content
            scrollToBottom()
        }
      }
    }
  } catch (error: any) {
    if (error.message === "登录过期") {
        messages.value[aiMsgIndex].content = "登录已过期，正在跳转..."
        return 
    }
    messages.value[aiMsgIndex].content += "\n[网络异常，请稍后再试]"
  } finally {
    messages.value[aiMsgIndex].loading = false
    isGenerating.value = false
  }
}
</script>

<style scoped>
.ai-assistant-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 0;
  height: 0;
  z-index: 9999;
}

.floating-ball {
  position: fixed;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #337ecc);
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  user-select: none;
  transition: transform 0.1s;
  z-index: 10000;
}
.floating-ball:active { cursor: grabbing; transform: scale(0.95); }

.chat-window-container {
  position: fixed;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  overflow: hidden; /* 保证内部元素不溢出 */
  z-index: 10001;
  /* 关键：为四周手柄留出位置 */
  padding: 5px; 
  box-sizing: border-box; /* 边框计算 */
  background: transparent; /* 背景透明，只给card加白色 */
}

.chat-card {
  width: 100%;
  height: 100%;
  border: none;
  display: flex;
  flex-direction: column;
  background: #fff; /* 实际背景 */
  border-radius: 8px; /* 内部圆角 */
  overflow: hidden;
}

:deep(.el-card__body) { padding: 0; flex: 1; display: flex; flex-direction: column; overflow: hidden; }

/* 头部 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  color: #303133;
  cursor: move;
  user-select: none;
  padding: 15px 20px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.header-title { display: flex; align-items: center; gap: 8px; }

/* 列表与输入 */
.message-list { flex: 1; padding: 15px; overflow-y: auto; background-color: #f5f7fa; }
.input-area { padding: 12px; background-color: #fff; border-top: 1px solid #ebeef5; flex-shrink: 0; }

/* 🔥🔥🔥 8方向缩放手柄样式 🔥🔥🔥 */
.resizer { position: absolute; z-index: 100; }

/* 四边 */
.resizer.top { top: 0; left: 0; right: 0; height: 5px; cursor: ns-resize; }
.resizer.bottom { bottom: 0; left: 0; right: 0; height: 5px; cursor: ns-resize; }
.resizer.left { top: 0; left: 0; bottom: 0; width: 5px; cursor: ew-resize; }
.resizer.right { top: 0; right: 0; bottom: 0; width: 5px; cursor: ew-resize; }

/* 四角 */
.resizer.top-left { top: 0; left: 0; width: 10px; height: 10px; cursor: nwse-resize; }
.resizer.top-right { top: 0; right: 0; width: 10px; height: 10px; cursor: nesw-resize; }
.resizer.bottom-left { bottom: 0; left: 0; width: 10px; height: 10px; cursor: nesw-resize; }
.resizer.bottom-right { 
  bottom: 0; right: 0; width: 15px; height: 15px; 
  cursor: nwse-resize; 
  display: flex; align-items: flex-end; justify-content: flex-end;
}

.icon-resize { color: #909399; font-size: 14px; pointer-events: none; }

/* Markdown & 气泡 */
.empty-tip { text-align: center; color: #909399; font-size: 13px; margin-top: 30%; line-height: 1.6; }
.message-item { display: flex; margin-bottom: 15px; align-items: flex-start; }
.message-user { flex-direction: row-reverse; }
.avatar { margin: 0 8px; flex-shrink: 0; }
.content-bubble { max-width: 85%; padding: 10px 14px; border-radius: 8px; font-size: 14px; line-height: 1.5; position: relative; word-break: break-all; }
.message-user .content-bubble { background-color: #409EFF; color: #fff; border-bottom-right-radius: 2px; }
.message-ai .content-bubble { background-color: #fff; color: #303133; border-bottom-left-radius: 2px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.typing-cursor::after { content: '▋'; display: inline-block; animation: blink 1s infinite; color: #409EFF; margin-left: 2px; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

:deep(.markdown-body) { font-size: 14px; line-height: 1.6; }
:deep(.markdown-body p) { margin-bottom: 8px; }
:deep(.markdown-body h1), :deep(.markdown-body h2) { margin-top: 10px; margin-bottom: 6px; font-weight: 600; }
:deep(.markdown-body code) { background-color: rgba(27, 31, 35, 0.05); padding: 2px 4px; border-radius: 3px; font-family: monospace; }
:deep(.markdown-body pre) { background-color: #f6f8fa; padding: 10px; border-radius: 6px; overflow: auto; margin-bottom: 8px; }
:deep(.markdown-body ul) { padding-left: 20px; margin-bottom: 8px; }
</style>