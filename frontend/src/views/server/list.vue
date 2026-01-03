<template>
  <div class="app-container">
    <el-card class="box-card" shadow="never">
      <div class="toolbar-container">
        <div class="toolbar-left">
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="搜索主机名 / IP / 显卡型号" 
            clearable 
            class="search-input"
            @keyup.enter="handleQuery"
            @clear="handleQuery"
          >
            <template #prefix>
              <el-icon class="input-icon"><Search /></el-icon>
            </template>
          </el-input>
          
          <el-button type="primary" plain :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleResetQuery">重置</el-button>
        </div>

        <div class="toolbar-right">
          <span class="total-tag">共 <span class="num">{{ total }}</span> 台服务器</span>
          <el-button v-if="userStore.role === 'admin'" type="primary" :icon="Plus" @click="handleAdd">
            新增资产
          </el-button>
        </div>
      </div>

      <el-table 
        v-loading="loading" 
        :data="tableData" 
        style="width: 100%; margin-top: 16px" 
        stripe
        highlight-current-row
        :header-cell-style="{ background: '#f8fafc', color: '#1e293b', fontWeight: '600', height: '50px' }"
        :row-style="{ height: '65px' }"
      >
        <el-table-column label="服务器信息" min-width="180" fixed="left">
          <template #default="{ row }">
            <div class="info-cell">
              <span class="server-name">{{ row.serverName }}</span>
              <div class="location-row">
                <el-icon><LocationInformation /></el-icon>
                <span>{{ row.location || '未知位置' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="网络配置" min-width="220">
          <template #default="{ row }">
            <div class="network-cell">
              <div class="ip-row">
                <span class="label">内网:</span>
                <el-tag size="small" effect="light" class="ip-tag">{{ row.serverIp }}</el-tag>
              </div>
              <div class="ip-row" v-if="row.publicIp">
                <span class="label">公网:</span>
                <el-tag size="small" type="success" effect="plain" class="ip-tag">{{ row.publicIp }}</el-tag>
              </div>
              <div class="port-row">
                <span class="label">SSH:</span> 
                <span class="port-num">{{ row.sshPort || 22 }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="显卡规格" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
             <div class="gpu-cell">
               <el-tag effect="plain" type="warning" round>
                 {{ row.gpuModel || '无显卡' }}
               </el-tag>
             </div>
          </template>
        </el-table-column>

        <el-table-column label="环境依赖" min-width="260">
          <template #default="{ row }">
            <div class="env-container">
              <div class="env-row">
                <span class="env-label">Conda:</span>
                <el-icon v-if="row.hasAnaconda === 1" color="#67C23A" size="16"><CircleCheckFilled /></el-icon>
                <el-icon v-else color="#909399" size="16"><CircleClose /></el-icon>
                
                <span class="env-label ml-2">NCCL:</span>
                <span class="env-value">{{ row.ncclInfo || '-' }}</span>
              </div>

              <div class="env-row mt-1">
                <span class="env-label">CUDA:</span>
                <div class="cuda-tags">
                    <template v-if="row.cudaInfo && row.cudaInfo.length">
                       <el-tag 
                         v-for="(cuda, idx) in row.cudaInfo" 
                         :key="idx" 
                         size="small"
                         :type="cuda.isDefault ? 'danger' : 'info'"
                         :effect="cuda.isDefault ? 'light' : 'plain'"
                         class="cuda-tag"
                       >
                         {{ cuda.version }}
                       </el-tag>
                    </template>
                    <span v-else class="text-placeholder">-</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" effect="light" round>
              {{ scope.row.status === 1 ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column v-if="userStore.role === 'admin'" label="操作" width="160" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="680px" append-to-body destroy-on-close :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="dialog-form" status-icon>
        <div class="form-section-title">基础信息</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="主机名" prop="serverName">
              <el-input v-model="form.serverName" placeholder="如: ServerH1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
             <el-form-item label="物理位置" prop="location">
              <el-input v-model="form.location" placeholder="如: 新科技楼lab201" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-section-title">网络配置</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="内网 IP" prop="serverIp">
              <el-input v-model="form.serverIp" placeholder="192.168.x.x" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="SSH 端口" prop="sshPort">
                <el-input-number v-model="form.sshPort" :min="1" :max="65535" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="公网 IP" prop="publicIp">
              <el-input v-model="form.publicIp" placeholder="选填，无则留空" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-section-title">环境规格</div>
        <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="显卡型号" prop="gpuModel">
               <el-input v-model="form.gpuModel" placeholder="如: NVIDIA RTX 4090" />
             </el-form-item>
            </el-col>
            <el-col :span="12">
               <el-form-item label="Anaconda">
                 <el-switch 
                   v-model="form.hasAnaconda" 
                   :active-value="1" 
                   :inactive-value="0" 
                   active-text="已安装"
                 />
               </el-form-item>
            </el-col>
            <el-col :span="12">
               <el-form-item label="NCCL 版本">
                 <el-input v-model="form.ncclInfo" placeholder="如: 2.8.4" />
               </el-form-item>
            </el-col>
        </el-row>

      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Monitor, Search, Refresh, Plus, Edit, Delete, 
  LocationInformation, CircleCheckFilled, CircleClose 
} from '@element-plus/icons-vue'
import { 
  fetchServerNodePage as pageList, 
  createServerNode as addServer, 
  updateServerNode as updateServer, 
  deleteServerNode as deleteServer 
} from '@/api/serverNode' 
import type { ServerNode } from '@/api/types'
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()

const loading = ref(false)
const total = ref(0)
const tableData = ref<ServerNode[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

// 默认表单数据
const form = reactive<Partial<ServerNode>>({
  id: undefined,
  serverName: '',
  serverIp: '',
  publicIp: '',
  sshPort: 22, // 默认端口
  gpuModel: '',
  location: '',
  hasAnaconda: 0,
  ncclInfo: '',
  cudaInfo: [] 
})

const rules = {
  serverName: [{ required: true, message: '必填', trigger: 'blur' }],
  serverIp: [{ required: true, message: '必填', trigger: 'blur' }]
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res: any = await pageList(queryParams)
    // 兼容处理数据结构
    const pageData = res.records ? res : (res.data || {})
    tableData.value = pageData.records || []

    const realTotal = Number(pageData.total)
    if (realTotal === 0 && tableData.value.length > 0) {
      total.value = tableData.value.length
    } else {
      total.value = realTotal || 0
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.page = 1; getList() }
const handleResetQuery = () => { queryParams.keyword = ''; getList() }

const resetForm = () => {
  form.id = undefined
  form.serverName = ''
  form.serverIp = ''
  form.publicIp = ''
  form.sshPort = 22
  form.gpuModel = ''
  form.location = ''
  form.hasAnaconda = 0
  form.ncclInfo = ''
  form.cudaInfo = []
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增服务器'
  dialogVisible.value = true
}

const handleEdit = (row: ServerNode) => {
  resetForm()
  Object.assign(form, JSON.parse(JSON.stringify(row)))
  dialogTitle.value = '编辑服务器'
  dialogVisible.value = true
}

const handleDelete = (row: ServerNode) => {
  ElMessageBox.confirm(`确认删除服务器 "${row.serverName}" 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteServer(row.id!)
    ElMessage.success('删除成功')
    getList()
  })
}

const submitForm = () => {
  formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      if (form.id) {
        await updateServer(form as ServerNode)
        ElMessage.success('修改成功')
      } else {
        await addServer(form as ServerNode)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      getList()
    }
  })
}

onMounted(() => { getList() })
</script>

<style scoped>
.app-container {
  padding: 24px;
  background-color: #f1f5f9;
  min-height: calc(100vh - 60px);
}

.box-card {
  border: none;
  border-radius: 12px;
}

/* --- Toolbar (Flex布局) --- */
.toolbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
  background: #fff;
  height: 50px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input { width: 280px; }
.input-icon { margin-top: 1px; }

.toolbar-right { display: flex; align-items: center; }
.total-tag { font-size: 14px; color: #64748b; margin-right: 16px; display: inline-flex; align-items: center; }
.num { color: #3b82f6; font-weight: bold; margin: 0 4px; font-size: 16px; }

/* --- 表格样式 --- */
.info-cell {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  padding: 4px 0;
}
.server-name {
  font-weight: 700;
  font-size: 15px;
  color: #334155;
}
.location-row {
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 4px;
}

.network-cell {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
}
.ip-row, .port-row {
  display: flex;
  align-items: center;
  gap: 6px;
}
.label {
  color: #94a3b8;
  width: 40px;
  text-align: right;
  flex-shrink: 0;
}
.ip-tag { font-family: monospace; }
.port-num {
  font-family: monospace;
  background: #f1f5f9;
  padding: 0 6px;
  border-radius: 4px;
  color: #475569;
}

.gpu-cell {
  font-weight: 600;
  color: #e6a23c;
}

/* 环境信息优化 */
.env-container {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
}
.env-row {
  display: flex;
  align-items: center;
  gap: 4px;
}
.env-label {
  color: #94a3b8;
  font-weight: 500;
}
.env-value {
  font-family: monospace;
  color: #334155;
}
.ml-2 { margin-left: 12px; }
.mt-1 { margin-top: 4px; }

.cuda-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.cuda-tag { font-family: monospace; }
.text-placeholder { color: #cbd5e1; }

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

/* 弹窗样式 */
.form-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid #3b82f6;
}
.dialog-form {
  padding: 0 10px;
}
</style>