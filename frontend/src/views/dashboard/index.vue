<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchAllServerNodes } from '@/api/serverNode'
import { fetchGpuLogs, type GpuLogQuery } from '@/api/gpu'
import type { ServerNode, GpuMetricLog } from '@/api'

const router = useRouter()
const loading = ref(false)
const servers = ref<ServerNode[]>([])
const latestLogs = ref<Record<string, GpuMetricLog | null>>({})
const timer = ref<number | undefined>()

const fetchLatestGpu = async (serverName: string) => {
  const params: GpuLogQuery = { page: 1, size: 1, serverName }
  const result = await fetchGpuLogs(params)
  const log = result.records?.[0] ?? null
  return log
}

const loadData = async () => {
  loading.value = true
  try {
    const list = await fetchAllServerNodes()
    servers.value = list || []
    const entries = await Promise.all(
      servers.value.map(async (s) => {
        const log = await fetchLatestGpu(s.serverName || '')
        return [s.serverName || '', log] as const
      })
    )
    latestLogs.value = Object.fromEntries(entries)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const goDetail = (serverName?: string) => {
  if (!serverName) return
  router.push(`/server/detail/${serverName}`)
}

const memoryPercent = (log?: GpuMetricLog | null) => {
  if (!log || !log.memoryTotal) return 0
  return Math.round(((log.memoryUsed || 0) / log.memoryTotal) * 100)
}

const tempTagType = (temp?: number) => {
  if (!temp) return 'info'
  return temp > 80 ? 'danger' : 'success'
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return time
  return date.toLocaleString()
}

const formatMemory = (value?: number) => {
  if (!value && value !== 0) return '-'
  return `${value} MB`
}

onMounted(() => {
  loadData()
  timer.value = window.setInterval(loadData, 5000)
})

onUnmounted(() => {
  if (timer.value) {
    window.clearInterval(timer.value)
  }
})
</script>

<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col
        v-for="server in servers"
        :key="server.id || server.serverName"
        :xs="24"
        :sm="12"
        :lg="8"
        class="card-col"
      >
        <el-card shadow="hover" :body-style="{ padding: '12px 16px' }" v-loading="loading">
          <div class="card-header">
            <div class="card-title">
              <span class="dot" :class="server.status === 1 ? 'dot--on' : 'dot--off'"></span>
              <span class="name">{{ server.serverName || '-' }}</span>
            </div>
            <el-button size="small" type="primary" plain @click="goDetail(server.serverName)">
              历史数据
            </el-button>
          </div>

          <div class="meta">
            <span>GPU：{{ latestLogs[server.serverName || '']?.gpuName || server.gpuModel || '-' }}</span>
            <span>位置：{{ server.location || '-' }}</span>
          </div>

          <div class="section">
            <div class="label">显存使用</div>
            <el-progress
              :percentage="memoryPercent(latestLogs[server.serverName || ''])"
              :status="memoryPercent(latestLogs[server.serverName || '']) > 90 ? 'exception' : undefined"
              :stroke-width="12"
              text-inside
            />
            <div class="sub">
              {{ formatMemory(latestLogs[server.serverName || '']?.memoryUsed) }} /
              {{ formatMemory(latestLogs[server.serverName || '']?.memoryTotal) }}
            </div>
          </div>

          <div class="section inline">
            <div>
              <div class="label">温度</div>
              <el-tag :type="tempTagType(latestLogs[server.serverName || '']?.temperature)">
                {{ latestLogs[server.serverName || '']?.temperature ?? '-' }}℃
              </el-tag>
            </div>
            <div>
              <div class="label">当前用户</div>
              <el-tag type="info">
                {{ latestLogs[server.serverName || '']?.userName || '空闲' }}
              </el-tag>
            </div>
            <div>
              <div class="label">更新时间</div>
              <div class="text">
                {{ formatTime(latestLogs[server.serverName || '']?.createTime || server.lastHeartbeat) }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page {
  padding: 4px;
}

.card-col {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.dot--on {
  background: #67c23a;
}

.dot--off {
  background: #c0c4cc;
}

.meta {
  display: flex;
  justify-content: space-between;
  color: #606266;
  font-size: 13px;
  margin-bottom: 8px;
}

.section {
  margin-bottom: 12px;
}

.section.inline {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  align-items: center;
}

.label {
  color: #909399;
  font-size: 12px;
  margin-bottom: 4px;
}

.sub {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.text {
  color: #303133;
  font-size: 13px;
}
</style>

