export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PaginationQuery {
  page?: number
  size?: number
}

export interface OrderItem {
  column?: string
  asc?: boolean
}

export interface PageResult<T = unknown> {
  records?: T[]
  total?: number
  size?: number
  current?: number
  orders?: OrderItem[]
  pages?: number
}

export interface SysUser {
  id?: number
  createTime?: string
  updateTime?: string
  params?: Record<string, unknown>
  username?: string
  password?: string
  realName?: string
  faceFeature?: string
  avatar?: string
  status?: number
}

export interface SysRole {
  id?: number
  createTime?: string
  updateTime?: string
  params?: Record<string, unknown>
  roleName?: string
  roleCode?: string
  description?: string
}

export interface SysUserRole {
  id?: number
  createTime?: string
  params?: Record<string, unknown>
  userId?: number
  roleId?: number
}

export interface CudaItem {
  version?: string
  isDefault?: boolean
}

export interface ServerNode {
  id?: number
  createTime?: string
  updateTime?: string
  params?: Record<string, unknown>
  serverName?: string
  serverIp?: string
  publicIp?: string
  sshPort?: number
  location?: string
  gpuModel?: string
  portRange?: string
  hasAnaconda?: number
  ncclInfo?: string
  cudaInfo?: CudaItem[]
  status?: number
  lastHeartbeat?: string
}

export interface SysDocument {
  id?: number
  createTime?: string
  updateTime?: string
  params?: Record<string, unknown>
  fileName?: string
  filePath?: string
  milvusId?: string
  status?: number
  summary?: string
}

export interface GpuMetricLog {
  id?: number
  createTime?: string
  params?: Record<string, unknown>
  serverName?: string
  gpuIndex?: number
  gpuName?: string
  utilization?: number
  memoryUsed?: number
  memoryTotal?: number
  temperature?: number
  fanSpeed?: number
  powerUsage?: number
  userName?: string
}

export interface GpuReportDto {
  serverName?: string
  logs?: GpuMetricLog[]
}

