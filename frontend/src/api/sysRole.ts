import { request } from '@/utils/request'
import type { PageResult, PaginationQuery, SysRole } from './types'

export const createRole = (data: SysRole) => {
  return request<boolean>({
    url: '/sys-role',
    method: 'post',
    data,
  })
}

export const updateRole = (data: SysRole) => {
  return request<boolean>({
    url: '/sys-role',
    method: 'put',
    data,
  })
}

export const deleteRole = (id: number) => {
  return request<boolean>({
    url: `/sys-role/${id}`,
    method: 'delete',
  })
}

export const fetchRoleList = (params?: PaginationQuery) => {
  return request<PageResult<SysRole>>({
    url: '/sys-role/list',
    method: 'get',
    params,
  })
}

