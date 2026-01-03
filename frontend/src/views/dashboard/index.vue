<template>
  <div class="dashboard-container">
    <div class="toolbar-container">
      <div class="left-panel">
        <span class="page-title">GPU 实时监控</span>
        <el-tag effect="plain" type="info" round class="count-tag">
          共 <span class="num">{{ servers.length }}</span> 台主机在线
        </el-tag>
      </div>
      
      <div class="right-panel">
        <span class="refresh-time" v-if="lastUpdateTime">更新于: {{ lastUpdateTime }}</span>
        
        <el-radio-group v-model="viewMode" size="default" class="view-switch">
          <el-radio-button label="card">
            <el-icon><Grid /></el-icon> 卡片
          </el-radio-button>
          <el-radio-button label="table">
            <el-icon><Menu /></el-icon> 列表
          </el-radio-button>
        </el-radio-group>
        
        <el-button :icon="Refresh" circle @click="handleManualRefresh" :loading="loading" class="refresh-btn" />
      </div>
    </div>

    <div v-loading="loading" class="content-area">
      
      <div v-if="viewMode === 'card'">
        <el-row :gutter="20">
          <el-col
            v-for="server in servers"
            :key="server.id || server.serverName"
            :xs="24" :sm="12" :md="12" :lg="8" :xl="6"
            class="card-col"
          >
            <el-card shadow="hover" class="server-card">
              <template #header>
                <div class="card-header">
                  <div class="header-title">
                    <span class="status-dot" :class="server.status === 1 ? 'online' : 'offline'"></span>
                    <span class="server-name">{{ server.serverName }}</span>
                  </div>
                  <el-button link type="primary" @click="goDetail(server.serverName)">详情</el-button>
                </div>
              </template>

              <div class="card-body">
                <div class="info-row location-row">
                  <span class="label">位置:</span>
                  <span class="value">{{ server.location || '-' }}</span>
                </div>

                <div v-if="!serverLogs[server.serverName!]?.length" class="empty-gpu">
                  暂无 GPU 数据
                </div>

                <div 
                  v-for="(gpu, index) in serverLogs[server.serverName!]" 
                  :key="index"
                  class="gpu-block"
                  :class="{ 'border-top': index > 0 }"
                >
                  <div class="gpu-title-row">
                    <el-tag size="small" type="warning" effect="dark" class="gpu-index-tag">
                      <el-icon><Cpu /></el-icon> {{ gpu.gpuIndex }}
                    </el-tag>
                    <span class="gpu-name">{{ gpu.gpuName || server.gpuModel }}</span>
                  </div>

                  <div class="metric-section">
                    <div class="metric-header">
                      <span class="label">显存</span>
                      <span class="value-sub">
                        {{ formatMemory(gpu.memoryUsed) }} / {{ formatMemory(gpu.memoryTotal) }}
                      </span>
                    </div>
                    <el-progress 
                      :percentage="memoryPercent(gpu)"
                      :status="getMemoryStatus(memoryPercent(gpu))"
                      :stroke-width="8"
                      :show-text="false"
                    />
                  </div>

                  <div class="stats-mini-grid">
                    <div class="mini-stat">
                      <span class="mini-label">温度</span>
                      <span :class="getTempClass(gpu.temperature)">
                        {{ gpu.temperature ?? '-' }}°C
                      </span>
                    </div>
                    <div class="mini-stat">
                      <span class="mini-label">利用率</span>
                      <span class="util-text">{{ gpu.utilization ?? 0 }}%</span>
                    </div>
                    <div class="mini-stat">
                      <span class="mini-label">用户</span>
                      <el-tag v-if="gpu.userName" type="danger" size="small" effect="dark" class="user-tag-mini">
                        {{ gpu.userName }}
                      </el-tag>
                      <el-tag v-else type="info" size="small" effect="plain" class="user-tag-mini">空闲</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <div v-else>
        <el-card shadow="never" class="table-card">
          <el-table 
            :data="flatTableData" 
            style="width: 100%" 
            :span-method="objectSpanMethod"
            border
            stripe
            :header-cell-style="{ background: '#f8fafc', color: '#1e293b', fontWeight: '600' }"
            :row-style="{ height: '60px' }"
          >
            <el-table-column prop="serverName" label="主机名" width="160" fixed>
              <template #default="{ row }">
                <div class="table-server-cell">
                  <span class="status-dot-small" :class="row.status === 1 ? 'online' : 'offline'"></span>
                  <span class="server-name-text">{{ row.serverName }}</span>
                </div>
                <div class="server-ip-text">{{ row.serverIp }}</div>
              </template>
            </el-table-column>

            <el-table-column prop="gpuIndex" label="序号" width="70" align="center">
              <template #default="{ row }">
                <div class="tag-wrapper">
                   <el-icon color="#e6a23c"><PriceTag /></el-icon>
                   <span class="index-text">{{ row.gpuIndex }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="gpuName" label="型号" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">
                <el-tag effect="plain" type="danger" size="small" class="model-tag">
                  {{ row.gpuName }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="utilization" label="利用率" width="100" align="center">
              <template #default="{ row }">
                {{ row.utilization }}%
              </template>
            </el-table-column>

            <el-table-column label="显存占用" min-width="220">
              <template #default="{ row }">
                <div class="table-progress-group">
                  <span class="mem-text">{{ formatMemory(row.memoryUsed) }}</span>
                  <el-progress 
                    :percentage="row.memPercent"
                    :status="getMemoryStatus(row.memPercent)"
                    :stroke-width="8"
                    :show-text="false"
                    class="mem-bar"
                  />
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="temperature" label="温度" width="90" align="center">
              <template #default="{ row }">
                <span :class="getTempClass(row.temperature)" style="font-weight: 600">
                  {{ row.temperature }}°C
                </span>
              </template>
            </el-table-column>

            <el-table-column prop="userName" label="用户" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.userName" type="danger" effect="dark" size="small">
                  {{ row.userName }}
                </el-tag>
                <el-tag v-else type="info" effect="plain" size="small">空闲</el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="updateTime" label="最后更新" width="170" align="center" show-overflow-tooltip />

            <el-table-column label="操作" width="120" fixed="right" align="center">
              <template #default="{ row }">
                <el-button size="small" @click="goDetail(row.serverName)">历史数据</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Grid, Menu, Refresh, Cpu, PriceTag } from '@element-plus/icons-vue'
import { fetchAllServerNodes } from '@/api/serverNode'
import { fetchGpuLogs, type GpuLogQuery } from '@/api/gpu'
import type { ServerNode, GpuMetricLog } from '@/api'

const router = useRouter()
const loading = ref(false)
const viewMode = ref('card') // 默认视图：卡片
const servers = ref<ServerNode[]>([])
// Key: serverName, Value: Array of GPU logs (multi-gpu support)
const serverLogs = ref<Record<string, GpuMetricLog[]>>({})
const timer = ref<number | undefined>()
const lastUpdateTime = ref('')

// 获取单台服务器的所有 GPU 最新状态
const fetchLatestGpuList = async (serverName: string) => {
  // size 设大一点 (比如 8)，确保能覆盖多卡服务器的所有 GPU
  const params: GpuLogQuery = { page: 1, size: 8, serverName }
  const result = await fetchGpuLogs(params)
  const logs = result.records || []
  
  // 数据清洗：API 返回的是日志流，需要对 GPU Index 进行去重，取每个 Index 的最新一条
  const uniqueGpuMap = new Map<number, GpuMetricLog>()
  
  // 假设后端返回是按时间倒序的，那么第一次遇到的 index 就是最新的
  logs.forEach(log => {
    if (!uniqueGpuMap.has(log.gpuIndex)) {
      uniqueGpuMap.set(log.gpuIndex, log)
    }
  })
  
  // 转回数组并按 index 排序 (0, 1, 2...)
  return Array.from(uniqueGpuMap.values()).sort((a, b) => a.gpuIndex - b.gpuIndex)
}

// 加载数据主逻辑
const loadData = async (isManual = false) => {
  // 仅在首次加载或手动刷新时显示全屏 loading，静默轮询时不显示
  if (servers.value.length === 0 || isManual) loading.value = true
  
  try {
    const list = await fetchAllServerNodes()
    servers.value = list || []
    
    // 并行获取每台服务器的显卡列表
    const entries = await Promise.all(
      servers.value.map(async (s) => {
        const logs = await fetchLatestGpuList(s.serverName || '')
        return [s.serverName || '', logs] as const
      })
    )
    serverLogs.value = Object.fromEntries(entries)
    
    const now = new Date()
    lastUpdateTime.value = now.toLocaleTimeString()
    
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 手动刷新按钮
const handleManualRefresh = () => {
  loadData(true)
}

// === 核心逻辑：构造扁平化的表格数据以支持合并 ===
// 每一行代表一个 GPU，如果服务器有 2 个 GPU，就会生成 2 行数据
const flatTableData = computed(() => {
  const result: any[] = []
  
  servers.value.forEach(server => {
    const sName = server.serverName || ''
    const logs = serverLogs.value[sName] || []
    
    // 如果该服务器没有任何显卡日志，也要显示一行（作为占位）
    if (logs.length === 0) {
      result.push({
        id: server.id + '_placeholder',
        serverName: sName,
        serverIp: server.serverIp,
        status: server.status,
        gpuIndex: '-',
        gpuName: server.gpuModel || '-',
        utilization: 0,
        memoryUsed: 0,
        memoryTotal: 0,
        memPercent: 0,
        temperature: '-',
        userName: '-',
        updateTime: '-',
        // 合并标记
        rowSpan: 1, 
        isFirst: true 
      })
    } else {
      // 遍历所有显卡生成行
      logs.forEach((log, index) => {
        result.push({
          id: log.id,
          serverName: sName,
          serverIp: server.serverIp,
          status: server.status,
          // GPU 详情
          gpuIndex: log.gpuIndex,
          gpuName: log.gpuName || server.gpuModel,
          utilization: log.utilization,
          memoryUsed: log.memoryUsed,
          memoryTotal: log.memoryTotal,
          memPercent: memoryPercent(log),
          temperature: log.temperature,
          userName: log.userName,
          updateTime: formatTime(log.createTime),
          // 合并标记：第一行 rowSpan = 总数，其他行 rowSpan = 0
          rowSpan: index === 0 ? logs.length : 0,
          isFirst: index === 0
        })
      })
    }
  })
  return result
})

// === Element Plus 表格合并方法 ===
const objectSpanMethod = ({ row, column, rowIndex, columnIndex }: any) => {
  // 第 0 列 (主机名) 和 最后一列 (操作) 需要合并
  if (columnIndex === 0 || columnIndex === 8) {
    if (row.isFirst) {
      return { rowspan: row.rowSpan, colspan: 1 }
    } else {
      return { rowspan: 0, colspan: 0 }
    }
  }
}

const goDetail = (serverName?: string) => {
  if (!serverName) return
  router.push(`/server/detail/${serverName}`)
}

// === 工具函数 ===
const memoryPercent = (log?: GpuMetricLog) => {
  if (!log || !log.memoryTotal) return 0
  // 防止除以0
  if (log.memoryTotal === 0) return 0
  return Math.round(((log.memoryUsed || 0) / log.memoryTotal) * 100)
}

const getMemoryStatus = (percent: number) => {
  if (percent > 90) return 'exception'
  if (percent > 70) return 'warning'
  return 'success'
}

const getTempClass = (temp?: any) => {
  if (!temp || temp === '-') return ''
  const t = Number(temp)
  return t > 80 ? 'text-danger' : 'text-success'
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

const formatMemory = (value?: number) => {
  if (!value && value !== 0) return '0'
  return `${value} MB`
}

onMounted(() => {
  loadData()
  // 🔥🔥🔥 修改：30分钟刷新一次 (1800000ms) 🔥🔥🔥
  timer.value = window.setInterval(() => {
    loadData(false) // 静默刷新
  }, 30 * 60 * 1000)
})

onUnmounted(() => {
  if (timer.value) clearInterval(timer.value)
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
}

/* --- Toolbar --- */
.toolbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.left-panel { display: flex; align-items: center; gap: 12px; }
.page-title { font-size: 18px; font-weight: 700; color: #1e293b; }
.count-tag { font-weight: 600; }
.num { color: #3b82f6; margin: 0 2px; }

.right-panel { display: flex; align-items: center; gap: 16px; }
.refresh-time { font-size: 12px; color: #94a3b8; }
.refresh-btn { margin-left: 8px; }

/* --- Table View Styles (仿图3) --- */
.table-card { border-radius: 8px; border: none; }

.table-server-cell { display: flex; align-items: center; gap: 6px; }
.status-dot-small { width: 8px; height: 8px; border-radius: 50%; }
.status-dot-small.online { background: #10b981; }
.status-dot-small.offline { background: #cbd5e1; }
.server-name-text { font-weight: 700; color: #334155; font-size: 14px; }
.server-ip-text { font-size: 12px; color: #94a3b8; margin-top: 4px; margin-left: 14px; }

.tag-wrapper { display: flex; align-items: center; justify-content: center; gap: 4px; }
.index-text { font-weight: bold; font-family: monospace; }

.model-tag { font-family: sans-serif; font-weight: 600; background-color: #fff0f6; border-color: #ffadd2; color: #c41d7f; }

.table-progress-group { display: flex; align-items: center; gap: 10px; width: 100%; }
.mem-text { font-size: 12px; width: 65px; text-align: right; color: #64748b; font-family: monospace; }
.mem-bar { flex: 1; }

/* --- Card View Styles (多卡适配) --- */
.card-col { margin-bottom: 20px; }
.server-card { border-radius: 12px; border: none; transition: transform 0.2s; }
.server-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }

.card-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 0; }
.header-title { display: flex; align-items: center; gap: 8px; }
.status-dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }
.status-dot.online { background: #10b981; box-shadow: 0 0 4px #10b981; }
.status-dot.offline { background: #909399; }
.server-name { font-weight: 700; font-size: 16px; color: #303133; }

.card-body { padding-top: 10px; }
.location-row { color: #94a3b8; font-size: 12px; margin-bottom: 12px; }
.empty-gpu { text-align: center; color: #cbd5e1; padding: 20px 0; font-size: 13px; }

/* 多卡分割块 */
.gpu-block { padding: 12px 0; }
.border-top { border-top: 1px dashed #e2e8f0; }

.gpu-title-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.gpu-name { font-weight: 600; font-size: 13px; color: #475569; }

.metric-section { margin-bottom: 10px; }
.metric-header { display: flex; justify-content: space-between; margin-bottom: 4px; font-size: 11px; }
.value-sub { color: #94a3b8; }

.stats-mini-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 4px; }
.mini-stat { background: #f8fafc; padding: 4px; border-radius: 4px; text-align: center; display: flex; flex-direction: column; }
.mini-label { font-size: 10px; color: #94a3b8; }
.util-text { font-weight: 600; color: #303133; font-size: 12px; }
.user-tag-mini { transform: scale(0.9); }

.text-success { color: #10b981; }
.text-danger { color: #ef4444; }
.text-gray { color: #94a3b8; font-size: 12px; }
</style>