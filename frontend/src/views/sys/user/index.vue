<template>
  <div class="app-container">
    <el-card class="box-card" shadow="never">
      <div class="toolbar-container">
        <div class="toolbar-left">
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="搜索账号 / 姓名" 
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
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
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
        <el-table-column label="头像" width="100" align="center">
          <template #default="{ row }">
            <div style="display: flex; justify-content: center;">
              <el-avatar :size="36" :src="row.avatar" :icon="UserFilled" class="user-avatar" />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="username" label="账号" min-width="150">
          <template #default="{ row }">
            <span class="username-text">{{ row.username }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="realName" label="真实姓名" min-width="150" />

        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <div class="status-cell">
              <el-tag v-if="row.status === 1" type="success" effect="light" round class="status-tag">
                <el-icon><Check /></el-icon><span>正常</span>
              </el-tag>
              <el-tag v-else type="danger" effect="plain" round class="status-tag">
                <el-icon><Close /></el-icon><span>禁用</span>
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="180" align="center" sortable />

        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="warning" link :icon="Key" @click="handleResetPwd(scope.row)">重置密码</el-button>
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

    <el-dialog 
      :title="dialogTitle" 
      v-model="dialogVisible" 
      width="480px" 
      destroy-on-close
      :close-on-click-modal="false"
      class="custom-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" status-icon>
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入登录账号" :disabled="!!form.id">
             <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="请输入初始密码" show-password />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
            inline-prompt
          />
        </el-form-item>
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
// 🔥 引入所有需要的图标
import { 
  User, Search, Refresh, Plus, Edit, Delete, Key, UserFilled, Check, Close 
} from '@element-plus/icons-vue'
import { 
  fetchUserPage as pageList, 
  createUser as addUser, 
  updateUser as updateUser, 
  deleteUser as deleteUser 
} from '@/api/sysUser'
import type { SysUser } from '@/api/types'

const loading = ref(false)
const total = ref(0)
const tableData = ref<SysUser[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

const form = reactive<Partial<SysUser>>({
  id: undefined,
  username: '',
  realName: '',
  password: '',
  status: 1,
  avatar: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res: any = await pageList(queryParams)
    // 兼容不同的接口返回结构
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
  form.username = ''
  form.realName = ''
  form.password = ''
  form.status = 1
  form.avatar = ''
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

const handleEdit = (row: SysUser) => {
  resetForm()
  Object.assign(form, row)
  form.password = undefined 
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
}

const handleResetPwd = (row: SysUser) => {
  ElMessageBox.confirm(`确认重置用户 "${row.username}" 的密码为 123456 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('密码已重置为 123456')
  })
}

const handleDelete = (row: SysUser) => {
  ElMessageBox.confirm(`确认删除用户 "${row.username}" 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteUser(row.id!)
    ElMessage.success('删除成功')
    getList()
  })
}

const submitForm = () => {
  formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      if (form.id) {
        await updateUser(form as SysUser)
        ElMessage.success('修改成功')
      } else {
        await addUser(form as SysUser)
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

/* --- 工具栏 (Flex布局核心) --- */
.toolbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center; /* 垂直居中 */
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
  background: #fff;
  height: 50px;
}

.toolbar-left {
  display: flex;
  align-items: center; /* 左侧内容垂直居中 */
  gap: 12px;
}

/* 输入框样式 */
.search-input { width: 240px; }
.input-icon { margin-top: 1px; }

/* 右侧按钮区 */
.toolbar-right { display: flex; align-items: center; }

/* 表格样式 */
.username-text { font-weight: 600; color: #334155; }
.user-avatar { background-color: #f1f5f9; color: #94a3b8; }

/* 状态列居中优化 */
.status-cell {
  display: flex;
  justify-content: center;
}
.status-tag {
  min-width: 80px;
  padding: 0 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px; /* 图标和文字间距 */
  height: 28px;
}
.status-tag span {
  line-height: 1; /* 防止文字偏移 */
  display: inline-block;
  margin-top: 1px;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>