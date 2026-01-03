<template>
  <div class="app-container">
    <el-card class="box-card" shadow="never">
      <div class="toolbar-container">
        <div class="toolbar-left">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索文档名称..."
            clearable
            class="search-input"
            @keyup.enter="handleQuery"
            @clear="handleQuery"
          >
            <template #prefix>
              <el-icon class="input-icon"><Search /></el-icon>
            </template>
          </el-input>
          
          <el-select 
            v-model="queryParams.viewScope" 
            placeholder="可见范围" 
            clearable 
            class="scope-select"
            @change="handleQuery"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="全员公开" :value="1" />
            <el-option label="仅限私有" :value="0" />
          </el-select>

          <el-button type="primary" plain :icon="Search" class="action-btn" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" class="action-btn" @click="handleResetQuery">重置</el-button>
        </div>

        <div class="toolbar-right">
          <span class="total-tag">共 <span class="num">{{ total }}</span> 份文档</span>
          <el-button type="primary" :icon="Upload" class="action-btn" @click="handleUploadClick">
            上传文档
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
        :row-style="{ height: '60px' }"
      >
        <el-table-column label="格式" width="100" align="center">
          <template #default="{ row }">
            <div class="cell-center">
              <el-icon :size="24" :color="getFileIconColor(row.fileExt)">
                <component :is="getFileIcon(row.fileExt)" />
              </el-icon>
              <span class="ext-text">{{ row.fileExt ? row.fileExt.toUpperCase() : 'FILE' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="title" label="文档标题" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="title-wrapper">
              <span class="doc-title" @click="handlePreview(row)">{{ row.title }}</span>
              <el-tag v-if="row.isRag === 1" type="warning" size="small" effect="plain" class="rag-tag">
                AI 知识库
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="fileSize" label="大小" width="120" align="center">
          <template #default="{ row }">
            <span class="cell-text">{{ formatFileSize(row.fileSize) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="解析状态" width="180" align="center">
          <template #default="{ row }">
             <div v-if="row.isRag === 1" class="cell-center">
               <el-tag v-if="row.parseStatus === 2" type="success" effect="light" class="status-tag">
                  <el-icon class="tag-icon"><Check /></el-icon>
                  <span>解析成功</span>
               </el-tag>
               <el-tag v-else-if="row.parseStatus === 3" type="danger" effect="plain" class="status-tag">
                  <el-icon class="tag-icon"><Close /></el-icon>
                  <span>解析失败</span>
               </el-tag>
               <el-tag v-else-if="row.parseStatus === 1" type="warning" effect="light" class="status-tag">
                  <el-icon class="tag-icon is-loading"><Loading /></el-icon>
                  <span>解析中...</span>
               </el-tag>
               <el-tag v-else type="info" effect="plain" class="status-tag">
                  <el-icon class="tag-icon"><Clock /></el-icon>
                  <span>等待中</span>
               </el-tag>
             </div>
             <span v-else class="text-gray" style="font-size: 13px">普通存储</span>
          </template>
        </el-table-column>

        <el-table-column prop="viewScope" label="可见性" width="120" align="center">
          <template #default="{ row }">
            <div class="cell-center">
              <el-tag :type="row.viewScope === 1 ? 'success' : 'info'" effect="plain" class="scope-tag">
                {{ row.viewScope === 1 ? '🔓 公开' : '🔒 私有' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="uploaderName" label="上传人" width="140" align="center" show-overflow-tooltip>
           <template #default="{ row }">
             <div class="cell-center">
               <el-tag v-if="row.uploaderId === currentUserId" type="info" size="small" effect="light">我</el-tag>
               <span v-else class="cell-text">{{ row.uploaderName }}</span>
             </div>
           </template>
        </el-table-column>

        <el-table-column prop="createTime" label="上传时间" width="180" align="center" sortable />

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <div class="cell-center">
              <el-button type="primary" link :icon="Download" @click="handleDownload(row)">
                下载
              </el-button>
              <el-button 
                v-if="canDelete(row)"
                type="danger" 
                link 
                :icon="Delete" 
                @click="handleDelete(row)"
                style="margin-left: 12px"
              >
                删除
              </el-button>
            </div>
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

    <el-dialog 
      title="上传新文档" 
      v-model="dialogVisible" 
      width="550px" 
      destroy-on-close
      :close-on-click-modal="false"
      class="custom-dialog"
    >
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="rules" label-width="100px" status-icon>
        <el-form-item label="文档标题" prop="title">
          <el-input v-model="uploadForm.title" placeholder="自动获取文件名" />
        </el-form-item>

        <el-form-item label="可见范围" prop="viewScope">
          <el-radio-group v-model="uploadForm.viewScope">
            <el-radio :label="0" border>私有</el-radio>
            <el-radio :label="1" border>公开</el-radio>
          </el-radio-group>
          <div class="form-tip">私有文档仅自己和管理员可见；公开文档全员可见。</div>
        </el-form-item>

        <el-form-item label="AI知识库" prop="isRag">
          <el-switch
            v-model="uploadForm.isRag"
            :active-value="1"
            :inactive-value="0"
            active-text="加入知识库 (RAG)"
          />
          <div class="form-tip">开启后，AI 将解析此文档并用于智能问答。</div>
        </el-form-item>

        <el-form-item label="选择文件" prop="file">
          <el-upload
            class="upload-area"
            drag
            action="#"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="fileList"
            accept=".pdf,.doc,.docx,.md,.txt"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">拖拽或点击上传</div>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="uploadLoading" @click="submitUpload">
            确定上传
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, type UploadFile } from 'element-plus'
import { 
  Search, Refresh, Upload, Download, Delete, 
  Document, Picture, VideoPlay, Lock, Unlock, UploadFilled, Loading, Check, Close, Clock
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { fetchDocumentPage, uploadDocument, deleteDocument, getDownloadUrl } from '@/api/sysDocument'

const userStore = useUserStore()
const loading = ref(false)
const uploadLoading = ref(false)
const total = ref(0)
const tableData = ref([])
const dialogVisible = ref(false)
const uploadFormRef = ref()
const fileList = ref<UploadFile[]>([])
const timer = ref<number>()

const isAdmin = computed(() => userStore.role === 'admin')
const currentUserId = computed(() => userStore.userInfo?.id)

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  viewScope: undefined 
})

const uploadForm = reactive({
  title: '',
  viewScope: 0,
  isRag: 0, 
  rawFile: null as File | null
})

const rules = {
  title: [{ required: true, message: '请输入文档标题', trigger: 'blur' }],
  viewScope: [{ required: true, message: '请选择可见性', trigger: 'change' }],
  file: [{ 
    required: true, 
    validator: (rule: any, value: any, callback: any) => {
      if (!uploadForm.rawFile) callback(new Error('请选择文件'))
      else callback()
    }, 
    trigger: 'change' 
  }]
}

const getList = async (isSilent = false) => {
  if (!isSilent) loading.value = true
  try {
    const res: any = await fetchDocumentPage(queryParams)
    let pageData = null;
    if (res && res.data && res.data.records) pageData = res.data;
    else if (res && res.records) pageData = res;

    if (pageData) {
        tableData.value = pageData.records || []
        total.value = Number(pageData.total) || 0
    }
  } catch (error) {
    console.error("获取列表失败", error)
  } finally {
    if (!isSilent) loading.value = false
  }
}

const handleUploadClick = () => {
  uploadForm.title = ''
  uploadForm.rawFile = null
  uploadForm.isRag = 0 
  fileList.value = []
  uploadForm.viewScope = isAdmin.value ? 1 : 0
  dialogVisible.value = true
}

const handleFileChange = (uploadFile: UploadFile) => {
  if (uploadFile.raw) {
    uploadForm.rawFile = uploadFile.raw
    if (!uploadForm.title && uploadFile.name) {
      const name = uploadFile.name
      uploadForm.title = name.substring(0, name.lastIndexOf('.'))
    }
    if (uploadFormRef.value) uploadFormRef.value.validateField('file')
  }
}

const handleFileRemove = () => {
  uploadForm.rawFile = null
}

const submitUpload = () => {
  uploadFormRef.value.validate(async (valid: boolean) => {
    if (valid && uploadForm.rawFile) {
      uploadLoading.value = true
      try {
        const formData = new FormData()
        formData.append('file', uploadForm.rawFile)
        formData.append('title', uploadForm.title)
        formData.append('viewScope', String(uploadForm.viewScope))
        formData.append('isRag', String(uploadForm.isRag))
        
        await uploadDocument(formData)
        
        ElMessage.success('上传成功，后台开始解析...')
        dialogVisible.value = false
        getList() 
      } catch (e: any) {
        ElMessage.error(e.message || '上传失败')
      } finally {
        uploadLoading.value = false
      }
    }
  })
}

const handleDownload = async (row: any) => {
  try {
    const res: any = await getDownloadUrl(row.id)
    const url = (res && res.data) ? res.data : res
    if (url && typeof url === 'string' && url.startsWith('http')) {
        window.open(url, '_blank')
    } else {
        ElMessage.warning('无效的下载链接')
    }
  } catch (e) {
    ElMessage.error('无法获取下载链接')
  }
}

const canDelete = (row: any) => {
  if (isAdmin.value) return true
  return row.uploaderId === currentUserId.value
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确认删除文档 "${row.title}" 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteDocument(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

const handlePreview = (row: any) => handleDownload(row)
const handleQuery = () => { queryParams.page = 1; getList() }
const handleResetQuery = () => { queryParams.keyword = ''; queryParams.viewScope = undefined; getList() }

const formatFileSize = (bytes: number) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileIcon = (ext: string) => {
  const e = ext ? ext.toLowerCase() : ''
  if (['jpg', 'png'].includes(e)) return 'Picture'
  if (['mp4', 'avi'].includes(e)) return 'VideoPlay'
  return 'Document' 
}

const getFileIconColor = (ext: string) => {
  const e = ext ? ext.toLowerCase() : ''
  if (['pdf'].includes(e)) return '#F56C6C' 
  if (['doc', 'docx'].includes(e)) return '#409EFF' 
  if (['xls', 'xlsx'].includes(e)) return '#67C23A' 
  if (['ppt', 'pptx'].includes(e)) return '#E6A23C'
  return '#909399' 
}

onMounted(() => {
  getList()
  timer.value = window.setInterval(() => {
    getList(true) 
  }, 3000)
})

onUnmounted(() => {
  if (timer.value) clearInterval(timer.value)
})
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

/* --- 工具栏优化 --- */
.toolbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
  background: #fff;
  padding: 8px 0; /* 增加一点内边距 */
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input { width: 240px; }
.scope-select { width: 140px; }
.input-icon { margin-top: 1px; }

/* 按钮微调：保证文字图标垂直居中 */
.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.toolbar-right { display: flex; align-items: center; }
.total-tag { font-size: 14px; color: #64748b; margin-right: 16px; display: inline-flex; align-items: center; }
.num { color: #3b82f6; font-weight: bold; margin: 0 4px; font-size: 16px; }

/* --- 单元格内容垂直居中 --- */
.cell-center {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
}
.cell-text {
  font-size: 14px;
  color: #334155;
}

/* 格式列 */
.format-cell { display: flex; align-items: center; justify-content: center; gap: 6px; }
.ext-text { font-size: 11px; font-weight: 700; color: #64748b; }

/* 标题列 */
.title-wrapper { display: flex; align-items: center; gap: 8px; }
.doc-title { font-weight: 500; color: #334155; cursor: pointer; transition: color 0.2s; }
.doc-title:hover { color: #3b82f6; }
.rag-tag { font-size: 10px; height: 20px; padding: 0 6px; border-radius: 4px; }

/* 🔥 解析状态标签优化 (核心修复) 🔥 */
.status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 30px; /* 增加高度 */
  padding: 0 16px; /* 增加水平内边距 */
  border-radius: 15px; /* 圆润边角 */
  min-width: 110px; /* 确保足够宽框住文字 */
  box-sizing: border-box; /* 包含内边距 */
}
.tag-icon {
  margin-right: 6px;
  font-size: 14px;
}
/* 强制文字不换行 */
.status-tag span {
  white-space: nowrap;
  line-height: 1; /* 修正基线对齐 */
}

.scope-tag { min-width: 70px; text-align: center; }
.text-gray { color: #94a3b8; }
.form-tip { font-size: 12px; color: #94a3b8; margin-top: 4px; }
.pagination-container { margin-top: 24px; display: flex; justify-content: flex-end; }
</style>