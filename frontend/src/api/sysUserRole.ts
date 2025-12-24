import { request } from '@/utils/request'
import type { SysUserRole } from './types'

export const assignRole = (data: SysUserRole) => {
  return request<boolean>({
    url: '/sys-user-role/assign',
    method: 'post',
    data,
  })
}

