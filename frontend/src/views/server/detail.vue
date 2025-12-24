<template>
  <div class="detail-container">
    <el-card class="info-card" shadow="hover">
      <div class="header-section">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/dashboard' }">监控大屏</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/server' }">服务器列表</el-breadcrumb-item>
          <el-breadcrumb-item>历史详情</el-breadcrumb-item>
          <el-breadcrumb-item>{{ serverName }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="server-tags">
           <el-tag size="large" effect="dark" type="primary">{{ serverName }}</el-tag>
           <el-tag size="large" type="info">{{ serverIp || '加载中...' }}</el-tag>
           <el-tag size="large" type="warning" effect="plain">{{ gpuModel || '加载中...' }}</el-tag>
        </div>
      </div>
    </el-card>

    <div v-loading="loading" class="gpu-list">
      <div v-if="gpuIndices.length === 0 && !loading" class="empty-data">
        <el-empty description="暂无监控数据" />
      </div>

      <div v-for="index in gpuIndices" :key="index" class="gpu-row">
        <el-row :gutter="20">
          <el-col :span="18" :xs="24">
            <el-card class="chart-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span class="card-title">
                    <el-icon><DataLine /></el-icon>
                    {{ getGpuDisplayName(index) }} - 资源趋势
                  </span>
                </div>
              </template>
              <div :ref="(el) => setLineChartRef(el, index)" style="width: 100%; height: 400px;"></div>
            </el-card>
          </el-col>

          <el-col :span="6" :xs="24">
            <el-card class="chart-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span class="card-title">
                    <el-icon><PieChart /></el-icon>
                    用户占比 (7天)
                  </span>
                </div>
              </template>
              <div :ref="(el) => setPieChartRef(el, index)" style="width: 100%; height: 400px;"></div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { DataLine, PieChart } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

// 请确认这里的 import 路径和名字与你 api 文件一致
import { fetchGpuLogs as getGpuList } from '@/api/gpu' 
import { fetchServerNodePage as getServerList } from '@/api/serverNode'
import type { GpuMetricLog } from '@/api/types'

// --- 状态定义 ---
const route = useRoute()
const serverName = ref(route.params.serverName as string)
const serverIp = ref('')
const gpuModel = ref('')
const loading = ref(false)
const groupedData = ref<Record<number, GpuMetricLog[]>>({})
const chartInstances = new Map<string, echarts.ECharts>()

// --- 1. 获取服务器基础信息 ---
const fetchServerInfo = async () => {
  try {
    const res = await getServerList({ keyword: serverName.value, size: 10 } as any)
    const list = (res as any).records || (res as any).data?.records || []
    
    // 精确匹配
    const targetServer = list.find((item: any) => item.serverName === serverName.value)

    if (targetServer) {
      serverIp.value = targetServer.serverIp
      gpuModel.value = targetServer.gpuModel
    } else {
      console.warn('未找到精确匹配的服务器信息')
    }
  } catch (e) {
    console.error('获取服务器信息失败', e)
  }
}

// --- 2. 核心：获取并处理 GPU 数据 ---
const fetchGpuData = async () => {
  loading.value = true
  try {
    const res = await getGpuList({
      serverName: serverName.value,
      page: 1,
      size: 2000 
    })
    
    const allLogs: GpuMetricLog[] = (res as any).records || (res as any).data?.records || []
    const groups: Record<number, GpuMetricLog[]> = {}
    const sortedLogs = [...allLogs].reverse() // 时间正序

    sortedLogs.forEach(log => {
      const idx = log.gpuIndex ?? 0 
      if (!groups[idx]) {
        groups[idx] = []
      }
      groups[idx].push(log)
    })

    groupedData.value = groups

  } catch (error) {
    console.error('获取监控数据失败', error)
    ElMessage.error('获取历史数据失败')
  } finally {
    loading.value = false
  }
}

const gpuIndices = computed(() => {
  return Object.keys(groupedData.value).map(Number).sort((a, b) => a - b)
})

const getGpuDisplayName = (index: number) => {
  const logs = groupedData.value[index]
  const specificName = logs && logs.length > 0 ? logs[0].gpuName : ''
  return `${specificName || gpuModel.value || 'GPU'} (Index: ${index})`
}

// --- 3. 图表渲染逻辑 ---

const setLineChartRef = (el: any, index: number) => {
  if (el) {
    setTimeout(() => {
        initLineChart(el, index)
    }, 0)
  }
}

const setPieChartRef = (el: any, index: number) => {
  if (el) {
    setTimeout(() => {
        initPieChart(el, index)
    }, 0)
  }
}

// 初始化折线图 (优化版：图例右上角 + 风扇转速)
const initLineChart = (dom: HTMLElement, index: number) => {
  if (dom.clientWidth === 0 || dom.clientHeight === 0) return;

  const key = `line-${index}`
  if (chartInstances.has(key)) {
    chartInstances.get(key)?.dispose()
  }
  
  const chart = echarts.init(dom)
  const data = groupedData.value[index] || []

  // 数据提取
  const times = data.map(item => formatTime(item.createTime))
  const utils = data.map(item => item.utilization ?? 0)
  const temps = data.map(item => item.temperature ?? 0)
  const fans = data.map(item => item.fanSpeed ?? 0) // 风扇转速
  
  const memUtils = data.map(item => {
    if (item.memoryTotal && item.memoryTotal > 0) {
      return Number(((item.memoryUsed || 0) / item.memoryTotal * 100).toFixed(1))
    }
    return 0
  })

  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: { 
      data: ['GPU利用率', '显存占比', '风扇转速', '温度'], 
      top: 0, 
      right: 10, 
      icon: 'circle',
      itemGap: 15
    },
    grid: { top: '12%', left: '3%', right: '3%', bottom: '10%', containLabel: true },
    dataZoom: [{ type: 'inside', start: 80, end: 100 }, { type: 'slider', bottom: 0 }],
    xAxis: { type: 'category', boundaryGap: false, data: times },
    yAxis: [
      { type: 'value', name: '百分比', min: 0, max: 100, position: 'left', axisLabel: { formatter: '{value} %' } },
      { type: 'value', name: '温度', min: 0, position: 'right', axisLabel: { formatter: '{value} °C' }, splitLine: { show: false } }
    ],
    series: [
      { 
        name: 'GPU利用率', type: 'line', smooth: true, showSymbol: false, data: utils, 
        areaStyle: { opacity: 0.15, color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#409EFF' }, { offset: 1, color: 'rgba(64,158,255,0.01)' }]) }, 
        itemStyle: { color: '#409EFF' }, z: 3 
      },
      { name: '显存占比', type: 'line', smooth: true, showSymbol: false, data: memUtils, itemStyle: { color: '#67C23A' }, z: 2 },
      { name: '风扇转速', type: 'line', smooth: true, showSymbol: false, data: fans, itemStyle: { color: '#8e44ad' }, lineStyle: { type: 'dashed', width: 1.5 }, z: 1 },
      { 
        name: '温度', type: 'line', smooth: true, showSymbol: false, yAxisIndex: 1, data: temps, itemStyle: { color: '#E6A23C' }, 
        markLine: { data: [{ yAxis: 80, name: '高温预警' }], itemStyle: { color: '#F56C6C' }, label: { position: 'insideEndTop', formatter: '{b}' }, symbol: 'none' } 
      }
    ]
  }

  chart.setOption(option)
  chartInstances.set(key, chart)
}

// 初始化饼图 (之前被注释掉了，现在恢复)
const initPieChart = (dom: HTMLElement, index: number) => {
  if (dom.clientWidth === 0 || dom.clientHeight === 0) return;

  const key = `pie-${index}`
  if (chartInstances.has(key)) {
    chartInstances.get(key)?.dispose()
  }

  const chart = echarts.init(dom)
  const data = groupedData.value[index] || []

  const userMap: Record<string, number> = {}
  data.forEach(item => {
    const user = item.userName ? item.userName : '空闲'
    userMap[user] = (userMap[user] || 0) + 1
  })

  const pieData = Object.keys(userMap).map(key => ({
    name: key,
    value: userMap[key]
  })).sort((a, b) => b.value - a.value)

  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%', left: 'center' },
    series: [
      {
        name: '用户活跃度', type: 'pie', radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
        data: pieData
      }
    ]
  }

  chart.setOption(option)
  chartInstances.set(key, chart)
}

const formatTime = (timeStr: any) => {
  if (!timeStr) return ''
  if (Array.isArray(timeStr)) {
     const h = (timeStr[3] || 0).toString().padStart(2, '0')
     const m = (timeStr[4] || 0).toString().padStart(2, '0')
     return `${h}:${m}`
  }
  if (typeof timeStr === 'string' && timeStr.includes('T')) {
      const parts = timeStr.split('T')
      if (parts.length > 1) return parts[1].substring(0, 5) 
  }
  if (typeof timeStr === 'string' && timeStr.length > 16) {
      return timeStr.substring(11, 16)
  }
  return timeStr
}

onMounted(() => {
  fetchServerInfo()
  fetchGpuData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstances.forEach(chart => chart.dispose())
  chartInstances.clear()
})

const handleResize = () => {
  chartInstances.forEach(chart => chart.resize())
}
</script>

<style scoped>
.detail-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.info-card {
  margin-bottom: 20px;
}
.server-tags {
  margin-top: 15px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.gpu-row {
  margin-bottom: 24px;
}

.chart-card {
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.empty-data {
  margin-top: 50px;
  background: #fff;
  border-radius: 8px;
  padding: 40px;
}
</style>