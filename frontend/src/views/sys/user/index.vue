<template>
  <div class="app-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon size="20" color="#409EFF"><User /></el-icon>
            <span class="title">用户列表</span>
          </div>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增用户</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item>
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="搜索账号 / 姓名" 
            clearable 
            prefix-icon="Search"
            @keyup.enter="handleQuery" 
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="handleResetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table 
        v-loading="loading" 
        :data="tableData" 
        style="width: 100%" 
        border 
        stripe
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar" :icon="UserFilled" />
          </template>
        </el-table-column>

        <el-table-column prop="username" label="账号" min-width="120">
          <template #default="{ row }">
            <span style="font-weight: 600; color: #303133;">{{ row.username }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="realName" label="真实姓名" min-width="120" />

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" effect="light">正常</el-tag>
            <el-tag v-else type="danger" effect="light">禁用</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="170" sortable />

        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="warning" link icon="Key" @click="handleResetPwd(scope.row)">重置密码</el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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
      width="500px" 
      append-to-body 
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入登录账号" :disabled="!!form.id" />
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
import { 
  User, Search, Refresh, Plus, Edit, Delete, Key, UserFilled 
} from '@element-plus/icons-vue'
// 引入 API
import { 
  fetchUserPage as pageList, 
  createUser as addUser, 
  updateUser as updateUser, 
  deleteUser as deleteUser 
} from '@/api/sysUser'
import type { SysUser } from '@/api/types' // 确保你有这个类型定义，如果没有可以用 any

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
    // 兼容处理：如果 res 本身包含 records 则用 res，否则尝试 res.data
    const pageData = res.records ? res : (res.data || {})
    tableData.value = pageData.records || []
    
    // Total 兜底逻辑
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

const handleQuery = () => {
  queryParams.page = 1
  getList()
}

const handleResetQuery = () => {
  queryParams.keyword = ''
  getList()
}

// 重置表单
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
  // 编辑时不传密码，避免覆盖
  form.password = undefined 
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
}

// 模拟重置密码功能
const handleResetPwd = (row: SysUser) => {
  ElMessageBox.confirm(`确认重置用户 "${row.username}" 的密码为 123456 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 这里如果是真实环境，应该调用 resetPwd API
    // await resetPwd(row.id)
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

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
}

.box-card {
  border: none;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>